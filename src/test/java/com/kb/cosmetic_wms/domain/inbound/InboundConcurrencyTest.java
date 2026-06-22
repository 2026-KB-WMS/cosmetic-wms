package com.kb.cosmetic_wms.domain.inbound;

import com.kb.cosmetic_wms.domain.inbound.dto.InboundDetailResponseDto;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.entity.InboundItem;
import com.kb.cosmetic_wms.domain.inbound.enums.InboundStatus;
import com.kb.cosmetic_wms.domain.inbound.fixture.InboundTestBuilder;
import com.kb.cosmetic_wms.domain.inbound.repository.InboundRepository;
import com.kb.cosmetic_wms.domain.inbound.service.InboundService;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InboundConcurrencyTest {

    @Autowired
    private InboundService inboundService;

    @Autowired
    private InboundRepository inboundRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private EventPublisher eventPublisher;

    private Long inboundId;

    @BeforeEach
    void setUp() {
        inboundRepository.deleteAll();
        insertFixtures();

        Inbound inbound = new InboundTestBuilder().build();
        InboundItem item = inbound.addItem(
                new InboundLine(1L, 100, LocalDate.now().minusDays(1), LocalDate.now().plusYears(2))
        );
        inbound.startExecution();
        item.completePutaway(1L, 1L);
        item.changeToNormal();

        inboundId = inboundRepository.save(inbound).getId();
    }

    private void insertFixtures() {
        jdbcTemplate.execute("INSERT INTO warehouse (warehouse_id, warehouse_name, address, target_temp, capacity, created_by, created_at) VALUES (1, '테스트창고', '서울시', 'ROOM', 1000, 1, NOW())");
        jdbcTemplate.execute("INSERT INTO partner (partner_id, partner_name, partner_type, created_by, created_at) VALUES (1, '테스트협력사', 'SUPPLIER', 1, NOW())");
        jdbcTemplate.execute("INSERT INTO category (category_id, category_code, category_name, created_by, created_at) VALUES (1, 'SKI', '스킨케어', 1, NOW())");
        jdbcTemplate.execute("INSERT INTO product_type (type_id, type_code, type_name, created_by, created_at) VALUES (1, 'CRM', '크림', 1, NOW())");
        jdbcTemplate.execute("INSERT INTO product (product_id, sku_code, brand_name, product_name, product_price, temperature_type, category_id, type_id, volume, unit, created_by, created_at) VALUES (1, 'SKI-CRM-050-001', '테스트브랜드', '테스트상품', 10000, 'ROOM', 1, 1, 50, 'ML', 1, NOW())");
        jdbcTemplate.execute("INSERT INTO lot (lot_id, lot_number, manufacturing_date, expiration_date, status, product_id, created_by, created_at) VALUES (1, 'LOT-TEST-001', NOW(), DATEADD('YEAR', 2, NOW()), 'AVAILABLE', 1, 1, NOW())");
        jdbcTemplate.execute("INSERT INTO section (section_id, warehouse_id, section_code, section_name, section_type, quality_status, allocation_status, temperature_type, max_capacity, current_capacity, created_by, created_at) VALUES (1, 1, 'WH01-HIGH-R-01', '테스트구역', 'HIGH_ROT', 'NORMAL', 'AVAILABLE', 'ROOM', 1000, 0, 1, NOW())");
    }

    @AfterEach
    void tearDown() {
        inboundRepository.deleteAll();
        jdbcTemplate.execute("DELETE FROM section WHERE section_id = 1");
        jdbcTemplate.execute("DELETE FROM lot WHERE lot_id = 1");
        jdbcTemplate.execute("DELETE FROM product WHERE product_id = 1");
        jdbcTemplate.execute("DELETE FROM product_type WHERE type_id = 1");
        jdbcTemplate.execute("DELETE FROM category WHERE category_id = 1");
        jdbcTemplate.execute("DELETE FROM partner WHERE partner_id = 1");
        jdbcTemplate.execute("DELETE FROM warehouse WHERE warehouse_id = 1");
    }

    @Test
    void 동시에_입고_완료를_요청하면_하나만_성공하고_나머지는_예외가_발생한다() throws InterruptedException {
        int threadCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<InboundDetailResponseDto> successes = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    InboundDetailResponseDto result = inboundService.completeInbound(inboundId);
                    successes.add(result);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // when - 모든 스레드 동시 출발
        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(finished).as("10초 내 모든 스레드가 완료되어야 한다").isTrue();
        assertThat(successes).as("단 하나의 요청만 성공해야 한다").hasSize(1);
        assertThat(successes.get(0).inboundStatus()).isEqualTo(InboundStatus.COMPLETED);
        assertThat(errors).as("나머지 요청은 예외가 발생해야 한다").hasSize(1);
        assertThat(errors.get(0)).isInstanceOf(IllegalStateException.class);

        // DB 최종 상태 검증
        Inbound saved = inboundRepository.findById(inboundId).orElseThrow();
        assertThat(saved.getInboundStatus()).isEqualTo(InboundStatus.COMPLETED);
    }
}
