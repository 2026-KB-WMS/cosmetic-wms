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

    @MockitoBean
    private EventPublisher eventPublisher;

    private Long inboundId;

    @BeforeEach
    void setUp() {
        inboundRepository.deleteAll();

        Inbound inbound = new InboundTestBuilder().build();
        InboundItem item = inbound.addItem(
                new InboundLine(1L, 100, LocalDate.now().minusDays(1), LocalDate.now().plusYears(2))
        );
        inbound.startExecution();
        item.completePutaway(100L, 200L);
        item.changeToNormal();

        inboundId = inboundRepository.save(inbound).getId();
    }

    @AfterEach
    void tearDown() {
        inboundRepository.deleteAll();
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
