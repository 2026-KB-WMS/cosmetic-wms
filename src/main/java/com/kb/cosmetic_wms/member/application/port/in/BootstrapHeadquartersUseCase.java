package com.kb.cosmetic_wms.member.application.port.in;

public interface BootstrapHeadquartersUseCase {

    /**
     * @return 본사 관리자 계정을 새로 만들었으면 true, 이미 있어 건너뛰었으면 false
     */
    boolean bootstrap(BootstrapHeadquartersCommand command);
}
