package com.kb.cosmetic_wms.integration;

import com.kb.cosmetic_wms.inbound.application.exception.InboundCapacityExceededException;
import com.kb.cosmetic_wms.inbound.application.port.in.*;
import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inspection.application.port.in.FindInspectionUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionLifecycleUseCase;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionResult;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inventory.application.port.in.FindInventoryUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.InventoryResult;
import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.lot.application.port.in.FindLotUseCase;
import com.kb.cosmetic_wms.lot.application.port.in.LotResult;
import com.kb.cosmetic_wms.storage.application.port.in.*;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * 입고(Inbound) → 로트(Lot) 생성 → 도킹 용량 확보 → 품질 검사(Inspection) → 재고(Inventory) 생성까지
 * 전체 이벤트 드리븐 플로우를 검증하는 통합 테스트.
 *
 * <pre>
 * [입고 수령 - receive()]
 *   ├─ BEFORE_COMMIT (동기): LotService → Lot 자동 생성
 *   ├─ BEFORE_COMMIT (동기): DockingCapacityService → DOCKING 섹션 용량 증가
 *   └─ AFTER_COMMIT + @Async (비동기): CreateInspectionService → 검사 대기 전표 생성
 *
 * [검사 완료 - complete()]
 *   └─ BEFORE_COMMIT (동기): InventoryEventAdapter
 *       ├─ SectionAssignmentAdapter: DOCKING 해제 → STORAGE/QUARANTINE 섹션 배정
 *       └─ ApplyInspectionResultUseCase: 합격/불합격 재고 생성
 * </pre>
 */
@SpringBootTest
class InboundToInventoryIntegrationTest {

    @Autowired private RegisterWarehouseUseCase registerWarehouseUseCase;
    @Autowired private AddSectionUseCase addSectionUseCase;

    @Autowired private InboundLifecycleUseCase inboundLifecycleUseCase;
    @Autowired private FindInboundUseCase findInboundUseCase;
    @Autowired private FindLotUseCase findLotUseCase;
    @Autowired private FindWarehouseUseCase findWarehouseUseCase;
    @Autowired private InspectionLifecycleUseCase inspectionLifecycleUseCase;
    @Autowired private FindInspectionUseCase findInspectionUseCase;
    @Autowired private FindInventoryUseCase findInventoryUseCase;

    @Autowired private JdbcTemplate jdbcTemplate;

    private Long warehouseId;
    private Long partnerId;
    private Long productId;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        /*
         * AuditorAwareImpl이 항상 1L을 반환하므로,
         * inventory_transaction.member_id FK 제약 충족을 위해 member ID=1 레코드를 보장한다.
         * INSERT IGNORE로 중복 삽입을 방지한다.
         */
        jdbcTemplate.update(
                "INSERT IGNORE INTO member (member_id, login_id, password, role, member_name, email, phone_number, created_by, created_at)" +
                " VALUES (1, 'testadmin', 'no_password', 'ADMIN', '테스트관리자', 'admin@wms-test.com', '010-0000-0000', 1, ?)",
                now);

        // Category 직접 삽입 (ProductService의 2-phase SKU 할당이 H2 NOT NULL 제약과 충돌하므로
        // Category/ProductType/Partner/Product는 JdbcTemplate으로 직접 삽입)
        jdbcTemplate.update(
                "INSERT INTO category (category_code, category_name, created_by, created_at) VALUES (?, ?, ?, ?)",
                "SKC", "스킨케어", 1L, now);
        Long categoryId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        jdbcTemplate.update(
                "INSERT INTO product_type (type_code, type_name, created_by, created_at) VALUES (?, ?, ?, ?)",
                "TON", "토너", 1L, now);
        Long productTypeId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        jdbcTemplate.update(
                "INSERT INTO partner (partner_name, partner_type, business_number, created_by, created_at) VALUES (?, ?, ?, ?, ?)",
                "(주)테스트코스메틱", "VENDOR", "123-45-67890", 1L, now);
        partnerId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        jdbcTemplate.update(
                "INSERT INTO product (sku_code, brand_name, product_name, product_price, temperature_type," +
                " category_id, type_id, skin_type, volume, unit, created_by, created_at)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "P-TEST-001", "테스트브랜드", "테스트토너", 15000, "ROOM",
                categoryId, productTypeId, "ALL", 150, "ml", 1L, now);
        productId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Warehouse + Section은 UseCase로 생성 (SectionCode 자동 생성 로직 포함)
        warehouseId = registerWarehouseUseCase
                .register(new RegisterWarehouseCommand("테스트창고", "서울시 강남구", "15~25도", 10000))
                .warehouseId();

        addSectionUseCase.addSection(warehouseId,
                new AddSectionCommand(SectionType.DOCKING, "도킹 구역", TemperatureZone.ROOM, 1000));
        addSectionUseCase.addSection(warehouseId,
                new AddSectionCommand(SectionType.STORAGE, "상온 보관 구역", TemperatureZone.ROOM, 5000));
        addSectionUseCase.addSection(warehouseId,
                new AddSectionCommand(SectionType.QUARANTINE, "격리 구역", TemperatureZone.ROOM, 500));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM inventory_transaction");
        jdbcTemplate.execute("DELETE FROM inventory");
        jdbcTemplate.execute("DELETE FROM quality_inspection");
        jdbcTemplate.execute("DELETE FROM failed_inspection_event");
        jdbcTemplate.execute("DELETE FROM lot");
        jdbcTemplate.execute("DELETE FROM inbound_line");
        jdbcTemplate.execute("DELETE FROM inbound");
        jdbcTemplate.execute("DELETE FROM section");
        jdbcTemplate.execute("DELETE FROM warehouse");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM product_type");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM partner");
    }

    // =========================================================
    // 입고 수령 플로우
    // =========================================================

    @Nested
    class 입고_수령_플로우 {

        @Test
        void 수령_시_로트와_도킹_섹션_용량이_동기적으로_업데이트된다() {
            // given
            InboundResult registered = registerInbound(100);
            Long inboundId = registered.id();
            Long lineId = registered.lines().get(0).id();

            // when
            InboundResult received = inboundLifecycleUseCase.receive(inboundId,
                    new ReceiveInboundCommand(List.of(
                            new ReceiveInboundCommand.LineItem(lineId, 100, "MFG-LOT-001",
                                    LocalDateTime.of(2025, 6, 1, 0, 0),
                                    LocalDateTime.of(2027, 6, 1, 0, 0)))));

            // then - 입고 상태 RECEIVED 전환
            assertThat(received.inboundStatus()).isEqualTo(InboundStatus.RECEIVED);

            // then - Lot 자동 생성 (BEFORE_COMMIT 동기)
            List<LotResult> lots = findLotUseCase.findByProductId(productId);
            assertThat(lots).hasSize(1);
            LotResult lot = lots.get(0);
            assertThat(lot.inboundId()).isEqualTo(inboundId);
            assertThat(lot.manufacturerLotNumber()).isEqualTo("MFG-LOT-001");
            assertThat(lot.productId()).isEqualTo(productId);

            // then - DOCKING 섹션 현재 용량 100 증가 (BEFORE_COMMIT 동기)
            assertThat(getDockingSection().currentCapacity()).isEqualTo(100);
        }

        @Test
        void 수령_시_비동기로_품질검사_대기_전표가_생성된다() {
            // given
            InboundResult registered = registerInbound(80);
            Long inboundId = registered.id();
            Long lineId = registered.lines().get(0).id();

            // when - 입고 수령
            inboundLifecycleUseCase.receive(inboundId,
                    new ReceiveInboundCommand(List.of(
                            new ReceiveInboundCommand.LineItem(lineId, 80, "MFG-LOT-002",
                                    LocalDateTime.of(2025, 6, 1, 0, 0),
                                    LocalDateTime.of(2027, 6, 1, 0, 0)))));

            // then - 비동기 검사 전표 생성 대기 (AFTER_COMMIT + @Async)
            await().atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(200))
                    .until(() -> countInspectionsBySourceId(lineId) > 0);

            Long inspectionId = findInspectionIdBySourceId(lineId);
            InspectionResult inspection = findInspectionUseCase.findById(inspectionId);

            assertThat(inspection.status()).isEqualTo(InspectionStatus.WAITING);
            assertThat(inspection.inspectionQuantity()).isEqualTo(80);
            assertThat(inspection.sourceId()).isEqualTo(lineId);
        }

        @Test
        void 도킹_섹션_용량_초과_시_수령이_거절된다() {
            // given - DOCKING 최대 용량 1000을 초과하는 1001개 수령 시도
            InboundResult registered = registerInbound(1001);
            Long inboundId = registered.id();
            Long lineId = registered.lines().get(0).id();

            // when & then - 용량 초과 예외 발생
            assertThatThrownBy(() ->
                    inboundLifecycleUseCase.receive(inboundId,
                            new ReceiveInboundCommand(List.of(
                                    new ReceiveInboundCommand.LineItem(lineId, 1001, "MFG-LOT-003",
                                            LocalDateTime.of(2025, 6, 1, 0, 0),
                                            LocalDateTime.of(2027, 6, 1, 0, 0))))))
                    .isInstanceOf(InboundCapacityExceededException.class);

            // then - 입고 상태 SCHEDULED 유지 (롤백)
            assertThat(findInboundUseCase.findById(inboundId).inboundStatus())
                    .isEqualTo(InboundStatus.SCHEDULED);

            // then - 롤백으로 인해 Lot 미생성, DOCKING 용량 변동 없음
            assertThat(findLotUseCase.findByProductId(productId)).isEmpty();
            assertThat(getDockingSection().currentCapacity()).isEqualTo(0);
        }
    }

    // =========================================================
    // 검사 완료 플로우
    // =========================================================

    @Nested
    class 검사_완료_플로우 {

        @Test
        void 합격_불합격_혼재_시_각_섹션에_재고가_생성된다() {
            // given - 입고 수령 → 검사 대기 전표 생성 완료
            long lotId = executeReceiveAndWaitForInspection(100, "MFG-LOT-004");
            Long inspectionId = findInspectionIdByLotId(lotId);
            inspectionLifecycleUseCase.start(inspectionId, 1L);

            // when - 합격 80 / 불합격 20
            InspectionResult completed = inspectionLifecycleUseCase.complete(inspectionId, 80, 20, "외관 불량");

            // then - 검사 상태 COMPLETED
            assertThat(completed.status()).isEqualTo(InspectionStatus.COMPLETED);
            assertThat(completed.passedQuantity()).isEqualTo(80);
            assertThat(completed.failedQuantity()).isEqualTo(20);

            // then - NORMAL 재고 (합격분, STORED, UNALLOCATED)
            List<InventoryResult> inventories = findInventoryUseCase.findByLotId(lotId);
            assertThat(inventories).hasSize(2);

            InventoryResult normalInv = findByQuality(inventories, QualityStatus.NORMAL);
            assertThat(normalInv.quantity()).isEqualTo(80);
            assertThat(normalInv.availableQuantity()).isEqualTo(80);
            assertThat(normalInv.allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
            assertThat(normalInv.locStatus()).isEqualTo(LocStatus.STORED);
            assertThat(normalInv.warehouseId()).isEqualTo(warehouseId);

            // then - HOLD 재고 (불합격분, STORED, UNALLOCATED, availableQuantity=0)
            InventoryResult holdInv = findByQuality(inventories, QualityStatus.HOLD);
            assertThat(holdInv.quantity()).isEqualTo(20);
            assertThat(holdInv.availableQuantity()).isEqualTo(0);
            assertThat(holdInv.allocStatus()).isEqualTo(AllocStatus.UNALLOCATED);
            assertThat(holdInv.locStatus()).isEqualTo(LocStatus.STORED);

            // then - 섹션 용량 재배치: DOCKING 0, STORAGE +80, QUARANTINE +20
            WarehouseResult warehouse = findWarehouseUseCase.findById(warehouseId);
            assertThat(getSectionByType(warehouse, SectionType.DOCKING).currentCapacity()).isEqualTo(0);
            assertThat(getSectionByType(warehouse, SectionType.STORAGE).currentCapacity()).isEqualTo(80);
            assertThat(getSectionByType(warehouse, SectionType.QUARANTINE).currentCapacity()).isEqualTo(20);
        }

        @Test
        void 전량_합격_시_보관_섹션에만_재고가_생성된다() {
            // given
            long lotId = executeReceiveAndWaitForInspection(50, "MFG-LOT-005");
            Long inspectionId = findInspectionIdByLotId(lotId);
            inspectionLifecycleUseCase.start(inspectionId, 1L);

            // when - 전량 합격
            inspectionLifecycleUseCase.complete(inspectionId, 50, 0, null);

            // then - NORMAL 재고 1개만 생성
            List<InventoryResult> inventories = findInventoryUseCase.findByLotId(lotId);
            assertThat(inventories).hasSize(1);
            assertThat(inventories.get(0).qualityStatus()).isEqualTo(QualityStatus.NORMAL);
            assertThat(inventories.get(0).quantity()).isEqualTo(50);

            // then - QUARANTINE 섹션 용량 변동 없음
            WarehouseResult warehouse = findWarehouseUseCase.findById(warehouseId);
            assertThat(getSectionByType(warehouse, SectionType.QUARANTINE).currentCapacity()).isEqualTo(0);
            assertThat(getSectionByType(warehouse, SectionType.STORAGE).currentCapacity()).isEqualTo(50);
        }

        @Test
        void 전량_불합격_시_격리_섹션에만_재고가_생성된다() {
            // given
            long lotId = executeReceiveAndWaitForInspection(30, "MFG-LOT-006");
            Long inspectionId = findInspectionIdByLotId(lotId);
            inspectionLifecycleUseCase.start(inspectionId, 1L);

            // when - 전량 불합격
            inspectionLifecycleUseCase.complete(inspectionId, 0, 30, "원료 오염");

            // then - HOLD 재고 1개만 생성
            List<InventoryResult> inventories = findInventoryUseCase.findByLotId(lotId);
            assertThat(inventories).hasSize(1);
            assertThat(inventories.get(0).qualityStatus()).isEqualTo(QualityStatus.HOLD);
            assertThat(inventories.get(0).quantity()).isEqualTo(30);
            assertThat(inventories.get(0).availableQuantity()).isEqualTo(0);

            // then - STORAGE 섹션 용량 변동 없음
            WarehouseResult warehouse = findWarehouseUseCase.findById(warehouseId);
            assertThat(getSectionByType(warehouse, SectionType.STORAGE).currentCapacity()).isEqualTo(0);
            assertThat(getSectionByType(warehouse, SectionType.QUARANTINE).currentCapacity()).isEqualTo(30);
        }
    }

    // =========================================================
    // E2E 플로우
    // =========================================================

    @Test
    void 다중_로트_입고부터_재고_생성까지_전체_E2E_플로우가_정상_동작한다() {
        // given - 2개 라인(로트) 입고 전표 등록
        InboundResult registered = inboundLifecycleUseCase.register(
                new RegisterInboundCommand(warehouseId, partnerId, LocalDateTime.now().plusDays(1),
                        List.of(
                                new RegisterInboundCommand.LineItem(productId, 100),
                                new RegisterInboundCommand.LineItem(productId, 100)
                        )));
        Long inboundId = registered.id();
        Long lineId1 = registered.lines().get(0).id();
        Long lineId2 = registered.lines().get(1).id();

        // when - 입고 수령 (제조사 로트 2종)
        inboundLifecycleUseCase.receive(inboundId,
                new ReceiveInboundCommand(List.of(
                        new ReceiveInboundCommand.LineItem(lineId1, 100, "MFG-LOT-E2E-A",
                                LocalDateTime.of(2025, 6, 1, 0, 0),
                                LocalDateTime.of(2027, 6, 1, 0, 0)),
                        new ReceiveInboundCommand.LineItem(lineId2, 100, "MFG-LOT-E2E-B",
                                LocalDateTime.of(2025, 7, 1, 0, 0),
                                LocalDateTime.of(2027, 7, 1, 0, 0))
                )));

        // then - Lot 2개 자동 생성
        List<LotResult> lots = findLotUseCase.findByProductId(productId);
        assertThat(lots).hasSize(2);

        // then - DOCKING 용량 200 증가 (두 라인 합산)
        assertThat(getDockingSection().currentCapacity()).isEqualTo(200);

        // then - 비동기 검사 전표 2개 생성 대기
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(300))
                .until(() -> jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM quality_inspection WHERE source_type = 'INBOUND'",
                        Integer.class) >= 2);

        List<Long> inspectionIds = jdbcTemplate.queryForList(
                "SELECT inspection_id FROM quality_inspection WHERE source_type = 'INBOUND' ORDER BY inspection_id",
                Long.class);
        assertThat(inspectionIds).hasSize(2);

        // when - 검사 1: 합격 90 / 불합격 10
        inspectionLifecycleUseCase.start(inspectionIds.get(0), 1L);
        inspectionLifecycleUseCase.complete(inspectionIds.get(0), 90, 10, "포장 불량");

        // when - 검사 2: 전량 합격 100
        inspectionLifecycleUseCase.start(inspectionIds.get(1), 1L);
        inspectionLifecycleUseCase.complete(inspectionIds.get(1), 100, 0, null);

        // then - 로트별 재고 검증
        Long lotAId = findLotId(lots, "MFG-LOT-E2E-A");
        Long lotBId = findLotId(lots, "MFG-LOT-E2E-B");

        // 로트 A: NORMAL 90 + HOLD 10
        List<InventoryResult> lotAInv = findInventoryUseCase.findByLotId(lotAId);
        assertThat(lotAInv).hasSize(2);
        assertThat(sumQty(lotAInv, QualityStatus.NORMAL)).isEqualTo(90);
        assertThat(sumQty(lotAInv, QualityStatus.HOLD)).isEqualTo(10);

        // 로트 B: NORMAL 100
        List<InventoryResult> lotBInv = findInventoryUseCase.findByLotId(lotBId);
        assertThat(lotBInv).hasSize(1);
        assertThat(lotBInv.get(0).qualityStatus()).isEqualTo(QualityStatus.NORMAL);
        assertThat(lotBInv.get(0).quantity()).isEqualTo(100);

        // then - 최종 섹션 용량 (DOCKING 0, STORAGE 190, QUARANTINE 10)
        WarehouseResult finalWarehouse = findWarehouseUseCase.findById(warehouseId);
        assertThat(getSectionByType(finalWarehouse, SectionType.DOCKING).currentCapacity()).isEqualTo(0);
        assertThat(getSectionByType(finalWarehouse, SectionType.STORAGE).currentCapacity()).isEqualTo(190);
        assertThat(getSectionByType(finalWarehouse, SectionType.QUARANTINE).currentCapacity()).isEqualTo(10);
    }

    // =========================================================
    // 헬퍼 메서드
    // =========================================================

    private InboundResult registerInbound(int quantity) {
        return inboundLifecycleUseCase.register(
                new RegisterInboundCommand(warehouseId, partnerId, LocalDateTime.now().plusDays(1),
                        List.of(new RegisterInboundCommand.LineItem(productId, quantity))));
    }

    /**
     * 입고 수령 후 비동기 검사 전표 생성까지 기다린 뒤 lotId를 반환하는 헬퍼.
     * 검사 완료 플로우 테스트에서 공통으로 사용.
     */
    private long executeReceiveAndWaitForInspection(int quantity, String manufacturerLotNumber) {
        InboundResult registered = registerInbound(quantity);
        Long lineId = registered.lines().get(0).id();

        inboundLifecycleUseCase.receive(registered.id(),
                new ReceiveInboundCommand(List.of(
                        new ReceiveInboundCommand.LineItem(lineId, quantity, manufacturerLotNumber,
                                LocalDateTime.of(2025, 6, 1, 0, 0),
                                LocalDateTime.of(2027, 6, 1, 0, 0)))));

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(200))
                .until(() -> countInspectionsBySourceId(lineId) > 0);

        return findLotUseCase.findByProductId(productId).stream()
                .filter(l -> manufacturerLotNumber.equals(l.manufacturerLotNumber()))
                .findFirst()
                .orElseThrow()
                .id();
    }

    private SectionResult getDockingSection() {
        return findWarehouseUseCase.findById(warehouseId).sections().stream()
                .filter(s -> s.sectionType() == SectionType.DOCKING)
                .findFirst()
                .orElseThrow();
    }

    private SectionResult getSectionByType(WarehouseResult warehouse, SectionType type) {
        return warehouse.sections().stream()
                .filter(s -> s.sectionType() == type)
                .findFirst()
                .orElseThrow();
    }

    private int countInspectionsBySourceId(Long sourceId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM quality_inspection WHERE source_type = 'INBOUND' AND source_id = ?",
                Integer.class, sourceId);
    }

    private Long findInspectionIdBySourceId(Long sourceId) {
        return jdbcTemplate.queryForObject(
                "SELECT inspection_id FROM quality_inspection WHERE source_type = 'INBOUND' AND source_id = ?",
                Long.class, sourceId);
    }

    private Long findInspectionIdByLotId(Long lotId) {
        return jdbcTemplate.queryForObject(
                "SELECT inspection_id FROM quality_inspection WHERE lot_id = ?",
                Long.class, lotId);
    }

    private InventoryResult findByQuality(List<InventoryResult> inventories, QualityStatus status) {
        return inventories.stream()
                .filter(i -> i.qualityStatus() == status)
                .findFirst()
                .orElseThrow();
    }

    private int sumQty(List<InventoryResult> inventories, QualityStatus status) {
        return inventories.stream()
                .filter(i -> i.qualityStatus() == status)
                .mapToInt(InventoryResult::quantity)
                .sum();
    }

    private Long findLotId(List<LotResult> lots, String manufacturerLotNumber) {
        return lots.stream()
                .filter(l -> manufacturerLotNumber.equals(l.manufacturerLotNumber()))
                .findFirst()
                .orElseThrow()
                .id();
    }
}
