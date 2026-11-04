package com.kb.cosmetic_wms.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    /**
     * 유통기한처럼 날짜로 판단하는 업무 규칙의 기준 시간대. 컨테이너 JVM의 기본 시간대(UTC)와 무관하게 고정한다.
     */
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(BUSINESS_ZONE);
    }
}
