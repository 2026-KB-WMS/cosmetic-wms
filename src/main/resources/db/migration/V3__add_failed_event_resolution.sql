ALTER TABLE failed_inspection_event
    ADD COLUMN status      VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태 (PENDING/RESOLVED/DISCARDED)',
    ADD COLUMN retry_count INT         NOT NULL DEFAULT 0 COMMENT '수동 재처리 시도 횟수',
    ADD COLUMN resolved_at DATETIME(6) NULL COMMENT '해결 또는 폐기 시각';

CREATE INDEX idx_failed_inspection_event_status ON failed_inspection_event (status, failed_at);

ALTER TABLE failed_assignment_event
    ADD COLUMN status      VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태 (PENDING/RESOLVED/DISCARDED)',
    ADD COLUMN retry_count INT         NOT NULL DEFAULT 0 COMMENT '수동 재처리 시도 횟수',
    ADD COLUMN resolved_at DATETIME(6) NULL COMMENT '해결 또는 폐기 시각';

CREATE INDEX idx_failed_assignment_event_status ON failed_assignment_event (status, failed_at);
