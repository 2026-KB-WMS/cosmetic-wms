CREATE TABLE putaway_order (
    putaway_order_id  BIGINT       NOT NULL AUTO_INCREMENT,
    inspection_id     BIGINT       NOT NULL,
    lot_id            BIGINT       NOT NULL,
    product_id        BIGINT       NOT NULL,
    warehouse_id      BIGINT       NOT NULL,
    source_section_id BIGINT       NOT NULL,
    target_section_id BIGINT       NOT NULL,
    quantity          INT          NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    created_by        BIGINT       NOT NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_by        BIGINT,
    updated_at        DATETIME(6),
    PRIMARY KEY (putaway_order_id)
);
