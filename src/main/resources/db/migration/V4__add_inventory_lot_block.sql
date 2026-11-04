CREATE TABLE inventory_lot_block (
    lot_id     BIGINT      NOT NULL COMMENT '출고가 차단된 로트 ID (lot의 논리적 참조, FK 없음)',
    status     VARCHAR(20) NOT NULL COMMENT '차단 사유인 로트 상태 (HOLD/RECALLED/DAMAGED/EXPIRED/DISPOSED)',
    created_by BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_by BIGINT,
    updated_at DATETIME(6),
    PRIMARY KEY (lot_id)
);

-- 이미 정상(AVAILABLE)이 아닌 로트를 반영한다. 반영하지 않으면 배포 후에도 그 로트의 재고가 계속 출고된다.
INSERT INTO inventory_lot_block (lot_id, status, created_by, created_at)
SELECT lot_id, status, 0, NOW(6)
FROM lot
WHERE status <> 'AVAILABLE';
