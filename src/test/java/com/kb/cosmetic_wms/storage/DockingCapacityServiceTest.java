package com.kb.cosmetic_wms.storage;

import com.kb.cosmetic_wms.storage.application.port.in.ApplyInspectionCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.SectionAssignmentResult;
import com.kb.cosmetic_wms.storage.application.port.in.UpdateDockingCapacityCommand;
import com.kb.cosmetic_wms.storage.domain.model.Section;
import com.kb.cosmetic_wms.storage.application.port.out.ProductTemperatureQueryPort;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.application.service.DockingCapacityService;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import com.kb.cosmetic_wms.storage.fixture.SectionTestBuilder;
import com.kb.cosmetic_wms.storage.fixture.WarehouseTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DockingCapacityServiceTest {

    @InjectMocks
    private DockingCapacityService dockingCapacityService;

    @Mock
    private StoragePort storagePort;

    @Mock
    private ProductTemperatureQueryPort productTemperatureQueryPort;

    @Test
    void 입고_품목의_온도대별_수령_수량이_DOCKING_섹션에_반영된다() {
        Long warehouseId = 1L;
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(warehouseId).capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(3000).build();

        given(storagePort.findByIdForUpdate(warehouseId)).willReturn(Optional.of(warehouse));
        given(storagePort.save(any(Warehouse.class))).willAnswer(inv -> inv.getArgument(0));
        given(productTemperatureQueryPort.findTemperatureZonesByIds(any()))
                .willReturn(Map.of(1L, TemperatureZone.ROOM));

        UpdateDockingCapacityCommand command = new UpdateDockingCapacityCommand(
                warehouseId,
                List.of(new UpdateDockingCapacityCommand.LineItem(1L, 500))
        );

        dockingCapacityService.updateDockingCapacity(command);

        verify(storagePort).findByIdForUpdate(warehouseId);
        verify(storagePort).save(warehouse);
        assertThat(warehouse.getSections().get(0).getCurrentCapacity()).isEqualTo(500);
    }

    @Test
    void 수령_수량이_0인_품목만_있으면_창고_조회를_하지_않고_즉시_반환한다() {
        UpdateDockingCapacityCommand command = new UpdateDockingCapacityCommand(
                1L,
                List.of(new UpdateDockingCapacityCommand.LineItem(1L, 0))
        );

        dockingCapacityService.updateDockingCapacity(command);

        verify(storagePort, org.mockito.Mockito.never()).findByIdForUpdate(any());
    }

    @Test
    void 창고가_존재하지_않으면_WarehouseNotFoundException이_발생한다() {
        given(storagePort.findByIdForUpdate(99L)).willReturn(Optional.empty());
        given(productTemperatureQueryPort.findTemperatureZonesByIds(any()))
                .willReturn(Map.of(1L, TemperatureZone.ROOM));

        UpdateDockingCapacityCommand command = new UpdateDockingCapacityCommand(
                99L,
                List.of(new UpdateDockingCapacityCommand.LineItem(1L, 100))
        );

        assertThatThrownBy(() -> dockingCapacityService.updateDockingCapacity(command))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    void 검사_완료_후_합격_수량은_STORAGE_섹션에_불합격_수량은_QUARANTINE_섹션에_배정된다() {
        Long warehouseId = 1L;
        Long productId = 1L;
        Warehouse warehouse = new WarehouseTestBuilder().warehouseId(warehouseId).capacity(10000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-DOCK-R-01").sectionType(SectionType.DOCKING)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(3000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-STR-R-01").sectionType(SectionType.STORAGE)
                .temperatureType(TemperatureZone.ROOM).maxCapacity(5000).build();
        new SectionTestBuilder().warehouse(warehouse)
                .sectionCode("WH01-QUAR-R-01").sectionType(SectionType.QUARANTINE)
                .maxCapacity(1000).build();
        warehouse.receiveToDocking(Map.of(TemperatureZone.ROOM, 300));

        given(storagePort.findByIdForUpdate(warehouseId)).willReturn(Optional.of(warehouse));
        given(storagePort.save(any(Warehouse.class))).willAnswer(inv -> inv.getArgument(0));
        given(productTemperatureQueryPort.findTemperatureZonesByIds(List.of(productId)))
                .willReturn(Map.of(productId, TemperatureZone.ROOM));

        ApplyInspectionCapacityCommand command = new ApplyInspectionCapacityCommand(warehouseId, productId, 80, 20);

        dockingCapacityService.applyInspectionCapacity(command);

        verify(storagePort).save(warehouse);

        Section docking = warehouse.getSections().stream()
                .filter(s -> s.getSectionType() == SectionType.DOCKING).findFirst().orElseThrow();
        Section storage = warehouse.getSections().stream()
                .filter(s -> s.getSectionType() == SectionType.STORAGE).findFirst().orElseThrow();
        Section quarantine = warehouse.getSections().stream()
                .filter(s -> s.getSectionType() == SectionType.QUARANTINE).findFirst().orElseThrow();

        assertThat(docking.getCurrentCapacity()).isEqualTo(200);
        assertThat(storage.getCurrentCapacity()).isEqualTo(80);
        assertThat(quarantine.getCurrentCapacity()).isEqualTo(20);
    }

    @Test
    void 합격_불합격_수량이_모두_0이면_창고_조회_없이_빈_결과를_반환한다() {
        ApplyInspectionCapacityCommand command = new ApplyInspectionCapacityCommand(1L, 1L, 0, 0);

        SectionAssignmentResult result = dockingCapacityService.applyInspectionCapacity(command);

        assertThat(result.storageSectionId()).isNull();
        assertThat(result.quarantineSectionId()).isNull();
        verify(storagePort, org.mockito.Mockito.never()).findByIdForUpdate(any());
    }
}
