package com.kb.cosmetic_wms.member.adapter.in.bootstrap;

import com.kb.cosmetic_wms.member.application.port.in.BootstrapHeadquartersCommand;
import com.kb.cosmetic_wms.member.application.port.in.BootstrapHeadquartersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(HeadquartersBootstrapProperties.class)
public class HeadquartersBootstrapRunner implements ApplicationRunner {

    private final HeadquartersBootstrapProperties properties;
    private final BootstrapHeadquartersUseCase bootstrapHeadquartersUseCase;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.isUnset()) {
            log.info("본사 관리자 부트스트랩 설정이 없어 건너뜁니다.");
            return;
        }

        if (!properties.missingKeys().isEmpty()) {
            throw new IllegalStateException(
                    "본사 관리자 부트스트랩 설정이 불완전합니다. 누락: " + properties.missingKeys());
        }

        boolean created = bootstrapHeadquartersUseCase.bootstrap(new BootstrapHeadquartersCommand(
                properties.loginId(),
                properties.password(),
                properties.memberName(),
                properties.email(),
                properties.phoneNumber()
        ));

        log.info(created ? "본사 관리자 계정을 생성했습니다." : "본사 관리자 계정이 이미 있어 건너뜁니다.");
    }
}
