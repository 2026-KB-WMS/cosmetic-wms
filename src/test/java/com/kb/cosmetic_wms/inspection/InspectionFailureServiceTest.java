package com.kb.cosmetic_wms.inspection;

import ch.qos.logback.classic.Level;
import com.kb.cosmetic_wms.inspection.application.port.in.RecordInspectionFailureCommand;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort;
import com.kb.cosmetic_wms.inspection.application.service.InspectionFailureService;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.model.FailedInspection;
import com.kb.cosmetic_wms.support.LogCapture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InspectionFailureServiceTest {

    @InjectMocks
    private InspectionFailureService service;

    @Mock
    private InspectionCreationFailurePort failurePort;

    private final RecordInspectionFailureCommand command = new RecordInspectionFailureCommand(
            10L, 20L, 30L, 40L, 50, "LOT0001", LocalDate.of(2027, 1, 1), "DB 연결 실패");

    @Test
    void 실패를_기록하면_처리_대기_상태의_실패_건으로_저장한다() {
        // when
        service.record(command);

        // then
        ArgumentCaptor<FailedInspection> captor = ArgumentCaptor.forClass(FailedInspection.class);
        verify(failurePort).save(captor.capture());
        FailedInspection saved = captor.getValue();
        assertThat(saved.getInboundId()).isEqualTo(10L);
        assertThat(saved.getLineId()).isEqualTo(20L);
        assertThat(saved.getStatus()).isEqualTo(FailedEventStatus.PENDING);
        assertThat(saved.getErrorMessage()).isEqualTo("DB 연결 실패");
        assertThat(saved.getFailedAt()).isNotNull();
    }

    @Test
    void 실패를_기록하면_입고_라인과_사유를_WARN_로그로_남긴다() {
        try (LogCapture logs = LogCapture.of(InspectionFailureService.class)) {
            service.record(command);

            assertThat(logs.messages(Level.WARN)).singleElement().satisfies(message ->
                    assertThat(message).contains("inboundId=10").contains("lineId=20").contains("DB 연결 실패"));
        }
    }
}
