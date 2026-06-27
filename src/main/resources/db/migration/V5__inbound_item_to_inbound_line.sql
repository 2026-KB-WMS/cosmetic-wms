-- InboundItem → InboundLine 전환 (ADR: 입고 BC 책임 분리)
-- inspectionStatus / lotId / sectionId 제거, orderedQuantity / receivedQuantity 추가

CREATE TABLE inbound_line (
    inbound_line_id   BIGINT      NOT NULL AUTO_INCREMENT,
    inbound_id        BIGINT      NOT NULL,
    product_id        BIGINT      NOT NULL,
    ordered_quantity  INT         NOT NULL,
    received_quantity INT         NOT NULL DEFAULT 0,
    manufacture_date  DATE        NOT NULL,
    expiration_date   DATE        NOT NULL,
    created_by        BIGINT      NOT NULL,
    created_at        DATETIME(6) NOT NULL,
    updated_by        BIGINT,
    updated_at        DATETIME(6),
    PRIMARY KEY (inbound_line_id)
);

DROP TABLE IF EXISTS inbound_item;
