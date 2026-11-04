package com.kb.cosmetic_wms.oms;

import com.kb.cosmetic_wms.oms.application.port.in.RecordAssignmentFailureCommand;
import com.kb.cosmetic_wms.oms.application.port.out.AssignmentFailurePort;
import com.kb.cosmetic_wms.oms.application.service.AssignmentFailureService;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;
import com.kb.cosmetic_wms.support.LogCapture;
import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssignmentFailureServiceTest {

    @InjectMocks
    private AssignmentFailureService service;

    @Mock
    private AssignmentFailurePort assignmentFailurePort;

    @Test
    void 실패를_기록하면_처리_대기_상태의_실패_건으로_저장한다() {
        // when
        service.record(new RecordAssignmentFailureCommand(100L, 10L, "Routing API timeout"));

        // then
        ArgumentCaptor<FailedAssignment> captor = ArgumentCaptor.forClass(FailedAssignment.class);
        verify(assignmentFailurePort).save(captor.capture());
        FailedAssignment saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(100L);
        assertThat(saved.getStoreId()).isEqualTo(10L);
        assertThat(saved.getErrorMessage()).isEqualTo("Routing API timeout");
        assertThat(saved.getStatus()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(saved.getFailedAt()).isNotNull();
    }

    @Test
    void 실패를_기록하면_발주와_사유를_WARN_로그로_남긴다() {
        try (LogCapture logs = LogCapture.of(AssignmentFailureService.class)) {
            service.record(new RecordAssignmentFailureCommand(100L, 10L, "Routing API timeout"));

            assertThat(logs.messages(Level.WARN)).singleElement().satisfies(message ->
                    assertThat(message).contains("orderId=100").contains("storeId=10").contains("Routing API timeout"));
        }
    }
}
