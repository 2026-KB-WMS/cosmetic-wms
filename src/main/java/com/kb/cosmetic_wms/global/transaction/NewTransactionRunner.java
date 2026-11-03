package com.kb.cosmetic_wms.global.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 바깥 트랜잭션과 독립된 새 트랜잭션에서 작업을 실행한다.
 * 작업이 실패해 롤백되어도 바깥 트랜잭션(예: 실패 이력 기록)은 계속 진행할 수 있다.
 */
@Component
public class NewTransactionRunner {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void run(Runnable action) {
        action.run();
    }
}
