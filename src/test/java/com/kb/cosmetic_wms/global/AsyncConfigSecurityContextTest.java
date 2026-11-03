package com.kb.cosmetic_wms.global;

import com.kb.cosmetic_wms.global.config.AsyncConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigSecurityContextTest {

    private ThreadPoolTaskExecutor executor;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        if (executor != null) {
            executor.shutdown();
        }
    }

    private AtomicReference<Authentication> runAsyncAndCaptureAuthentication() throws InterruptedException {
        executor = (ThreadPoolTaskExecutor) new AsyncConfig().eventTaskExecutor();
        AtomicReference<Authentication> captured = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        executor.execute(() -> {
            captured.set(SecurityContextHolder.getContext().getAuthentication());
            done.countDown();
        });

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        return captured;
    }

    @Test
    void 비동기_작업은_호출한_스레드의_인증_정보를_이어받는다() throws InterruptedException {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "pw", List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(runAsyncAndCaptureAuthentication().get()).isSameAs(authentication);
    }

    @Test
    void 인증_정보가_없으면_비동기_작업에서도_없다() throws InterruptedException {
        assertThat(runAsyncAndCaptureAuthentication().get()).isNull();
    }
}
