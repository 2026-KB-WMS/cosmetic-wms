CREATE TABLE failed_inspection_event (
    failed_event_id       BIGINT       NOT NULL AUTO_INCREMENT,
    inbound_id            BIGINT       NOT NULL,
    line_id               BIGINT       NOT NULL,
    product_id            BIGINT       NOT NULL,
    warehouse_id          BIGINT       NOT NULL,
    received_quantity     INT          NOT NULL,
    manufacturer_lot_number VARCHAR(100),
    expiration_date       DATE,
    error_message         TEXT,
    failed_at             DATETIME(6)  NOT NULL,
    created_by            BIGINT       NOT NULL,
    created_at            DATETIME(6)  NOT NULL,
    updated_by            BIGINT,
    updated_at            DATETIME(6),
    PRIMARY KEY (failed_event_id)
);
