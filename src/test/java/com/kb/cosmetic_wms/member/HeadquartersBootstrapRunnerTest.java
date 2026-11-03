package com.kb.cosmetic_wms.member;

import com.kb.cosmetic_wms.member.adapter.in.bootstrap.HeadquartersBootstrapProperties;
import com.kb.cosmetic_wms.member.adapter.in.bootstrap.HeadquartersBootstrapRunner;
import com.kb.cosmetic_wms.member.application.port.in.BootstrapHeadquartersCommand;
import com.kb.cosmetic_wms.member.application.port.in.BootstrapHeadquartersUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeadquartersBootstrapRunnerTest {

    @Mock
    private BootstrapHeadquartersUseCase useCase;

    private HeadquartersBootstrapRunner runnerWith(HeadquartersBootstrapProperties properties) {
        return new HeadquartersBootstrapRunner(properties, useCase);
    }

    @Test
    void 모든_설정값이_비어있으면_부트스트랩을_건너뛴다() throws Exception {
        // given
        HeadquartersBootstrapProperties properties = new HeadquartersBootstrapProperties("", "", "", "", "");

        // when
        runnerWith(properties).run(new DefaultApplicationArguments());

        // then
        verify(useCase, never()).bootstrap(any());
    }

    @Test
    void 설정값이_일부만_있으면_누락된_항목을_알리고_기동에_실패한다() {
        // given
        HeadquartersBootstrapProperties properties =
                new HeadquartersBootstrapProperties("hq-admin", "", "본사관리자", "hq@example.com", "010-1111-2222");

        // when & then
        assertThatThrownBy(() -> runnerWith(properties).run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("password")
                .hasMessageNotContaining("hq-admin");
        verify(useCase, never()).bootstrap(any());
    }

    @Test
    void 모든_설정값이_있으면_설정값으로_부트스트랩을_요청한다() throws Exception {
        // given
        HeadquartersBootstrapProperties properties =
                new HeadquartersBootstrapProperties("hq-admin", "Password123!", "본사관리자", "hq@example.com", "010-1111-2222");

        // when
        runnerWith(properties).run(new DefaultApplicationArguments());

        // then
        ArgumentCaptor<BootstrapHeadquartersCommand> captor = ArgumentCaptor.forClass(BootstrapHeadquartersCommand.class);
        verify(useCase).bootstrap(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new BootstrapHeadquartersCommand(
                "hq-admin", "Password123!", "본사관리자", "hq@example.com", "010-1111-2222"));
    }
}
