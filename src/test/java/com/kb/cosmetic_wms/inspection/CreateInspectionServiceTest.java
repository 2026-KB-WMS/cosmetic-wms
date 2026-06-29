package com.kb.cosmetic_wms.inspection;

import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionCommand;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionPort;
import com.kb.cosmetic_wms.inspection.application.port.out.LotQueryPort;
import com.kb.cosmetic_wms.inspection.application.service.CreateInspectionService;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionLotNotFoundException;
import com.kb.cosmetic_wms.inspection.domain.exception.InspectionSourceIdRequiredException;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;
import com.kb.cosmetic_wms.inspection.fixture.InspectionTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateInspectionServiceTest {

    @InjectMocks
    private CreateInspectionService createInspectionService;

    @Mock
    private InspectionPort inspectionPort;

    @Mock
    private LotQueryPort lotQueryPort;

    @Test
    void 품질_검사_의뢰_커맨드가_들어오면_로트를_조회하여_대기_상태의_전표가_생성된다() {
        // given
        Inspection waitingInspection = new InspectionTestBuilder().buildPending();
        when(lotQueryPort.findLotIdByInboundAndManufacturerLot(1L, "LOT-001")).thenReturn(100L);
        when(inspectionPort.save(any(Inspection.class))).thenReturn(waitingInspection);

        CreateInspectionCommand command = new CreateInspectionCommand(
                InspectionSourceType.INBOUND, 10L, 10,
                1L, 1L, "LOT-001", 300L, LocalDate.of(2026, 12, 31));

        // when
        createInspectionService.create(command);

        // then
        ArgumentCaptor<Inspection> captor = ArgumentCaptor.forClass(Inspection.class);
        verify(inspectionPort).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InspectionStatus.WAITING);
        assertThat(captor.getValue().getLotId()).isEqualTo(100L);
        assertThat(captor.getValue().getSourceId()).isEqualTo(10L);
        assertThat(captor.getValue().getSectionId()).isNull();
    }

    @Test
    void 유효하지_않은_sourceId가_null이면_품질_검사_전표_생성에_실패한다() {
        // given
        when(lotQueryPort.findLotIdByInboundAndManufacturerLot(1L, "LOT-001")).thenReturn(100L);

        CreateInspectionCommand command = new CreateInspectionCommand(
                InspectionSourceType.INBOUND, null, 10,
                1L, 1L, "LOT-001", 300L, LocalDate.of(2026, 12, 31));

        // when & then
        assertThatThrownBy(() -> createInspectionService.create(command))
                .isInstanceOf(InspectionSourceIdRequiredException.class);
    }

    @Test
    void 대응하는_로트가_없으면_InspectionLotNotFoundException을_던진다() {
        // given
        when(lotQueryPort.findLotIdByInboundAndManufacturerLot(99L, "UNKNOWN"))
                .thenThrow(new InspectionLotNotFoundException());

        CreateInspectionCommand command = new CreateInspectionCommand(
                InspectionSourceType.INBOUND, 10L, 10,
                1L, 99L, "UNKNOWN", 300L, LocalDate.of(2026, 12, 31));

        // when & then
        assertThatThrownBy(() -> createInspectionService.create(command))
                .isInstanceOf(InspectionLotNotFoundException.class);

        verify(inspectionPort, never()).save(any());
    }
}
