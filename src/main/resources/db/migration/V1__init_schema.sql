CREATE TABLE member (
    member_id    BIGINT       NOT NULL AUTO_INCREMENT,
    login_id     VARCHAR(50)  NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(50)  NOT NULL,
    member_name  VARCHAR(50)  NOT NULL,
    email        VARCHAR(50)  NOT NULL,
    phone_number VARCHAR(50)  NOT NULL,
    created_by   BIGINT       NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_by   BIGINT,
    updated_at   DATETIME(6),
    PRIMARY KEY (member_id),
    CONSTRAINT uq_login_id     UNIQUE (login_id),
    CONSTRAINT uq_email        UNIQUE (email),
    CONSTRAINT uq_phone_number UNIQUE (phone_number)
);

CREATE TABLE partner (
    partner_id      BIGINT       NOT NULL AUTO_INCREMENT,
    partner_name    VARCHAR(100) NOT NULL,
    partner_type    VARCHAR(30)  NOT NULL,
    business_number VARCHAR(20),
    created_by      BIGINT       NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_by      BIGINT,
    updated_at      DATETIME(6),
    PRIMARY KEY (partner_id),
    CONSTRAINT uq_partner_business_number UNIQUE (business_number)
);

CREATE TABLE store (
    store_id   BIGINT        NOT NULL AUTO_INCREMENT,
    store_name VARCHAR(100)  NOT NULL,
    address    VARCHAR(100)  NOT NULL,
    latitude   DECIMAL(10,7) NOT NULL,
    longitude  DECIMAL(10,7) NOT NULL,
    created_by BIGINT        NOT NULL,
    created_at DATETIME(6)   NOT NULL,
    updated_by BIGINT,
    updated_at DATETIME(6),
    PRIMARY KEY (store_id),
    CONSTRAINT uq_store_name_address UNIQUE (store_name, address)
);

CREATE TABLE warehouse (
    warehouse_id   BIGINT        NOT NULL AUTO_INCREMENT,
    warehouse_name VARCHAR(100)  NOT NULL,
    address        VARCHAR(255)  NOT NULL,
    latitude       DECIMAL(10,7) NOT NULL,
    longitude      DECIMAL(10,7) NOT NULL,
    target_temp    VARCHAR(20)   NOT NULL,
    capacity       INT           NOT NULL,
    created_by     BIGINT        NOT NULL,
    created_at     DATETIME(6)   NOT NULL,
    updated_by     BIGINT,
    updated_at     DATETIME(6),
    PRIMARY KEY (warehouse_id)
);

CREATE TABLE section (
    section_id        BIGINT      NOT NULL AUTO_INCREMENT,
    warehouse_id      BIGINT      NOT NULL,
    section_code      VARCHAR(20) NOT NULL,
    section_name      VARCHAR(50) NOT NULL,
    section_type      VARCHAR(20) NOT NULL,
    quality_status    VARCHAR(10) NOT NULL,
    allocation_status VARCHAR(10) NOT NULL,
    temperature_type  VARCHAR(10) NOT NULL,
    max_capacity      INT         NOT NULL,
    current_capacity  INT         NOT NULL,
    created_by        BIGINT      NOT NULL,
    created_at        DATETIME(6) NOT NULL,
    updated_by        BIGINT,
    updated_at        DATETIME(6),
    PRIMARY KEY (section_id),
    CONSTRAINT uq_section_code UNIQUE (warehouse_id, section_code)
);

CREATE TABLE category (
    category_id   BIGINT      NOT NULL AUTO_INCREMENT,
    category_code CHAR(3)     NOT NULL,
    category_name VARCHAR(50) NOT NULL,
    created_by    BIGINT      NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    updated_by    BIGINT,
    updated_at    DATETIME(6),
    PRIMARY KEY (category_id)
);

CREATE TABLE product_type (
    type_id    BIGINT      NOT NULL AUTO_INCREMENT,
    type_code  CHAR(3)     NOT NULL,
    type_name  VARCHAR(50) NOT NULL,
    created_by BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_by BIGINT,
    updated_at DATETIME(6),
    PRIMARY KEY (type_id)
);

CREATE TABLE product (
    product_id        BIGINT       NOT NULL AUTO_INCREMENT,
    sku_code          VARCHAR(50)  NOT NULL,
    brand_name        VARCHAR(50)  NOT NULL,
    product_name      VARCHAR(100) NOT NULL,
    product_price     INT          NOT NULL,
    temperature_type  VARCHAR(20)  NOT NULL,
    category_id       BIGINT       NOT NULL,
    type_id           BIGINT       NOT NULL,
    skin_type         VARCHAR(50),
    function_type     VARCHAR(100),
    volume            INT          NOT NULL,
    unit              VARCHAR(10)  NOT NULL,
    ingredients       TEXT,
    cautions          TEXT,
    storage_condition VARCHAR(255),
    created_by        BIGINT       NOT NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_by        BIGINT,
    updated_at        DATETIME(6),
    PRIMARY KEY (product_id),
    CONSTRAINT uq_product_sku_code            UNIQUE (sku_code),
    CONSTRAINT chk_product_price_non_negative CHECK  (product_price >= 0),
    CONSTRAINT chk_volume_positive            CHECK  (volume > 0)
);

CREATE TABLE lot (
    lot_id              BIGINT      NOT NULL AUTO_INCREMENT,
    inbound_id          BIGINT      NOT NULL,
    manufacturer_lot_no VARCHAR(50) NOT NULL,
    manufacturing_date  DATETIME(6) NOT NULL,
    expiration_date     DATETIME(6) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    product_id          BIGINT      NOT NULL,
    created_by          BIGINT      NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_by          BIGINT,
    updated_at          DATETIME(6),
    PRIMARY KEY (lot_id),
    CONSTRAINT uq_lot_inbound_manufacturer UNIQUE (inbound_id, manufacturer_lot_no),
    CONSTRAINT chk_lot_date                CHECK  (manufacturing_date <= expiration_date)
);

CREATE TABLE inventory (
    inventory_id       BIGINT      NOT NULL AUTO_INCREMENT,
    product_id         BIGINT      NOT NULL,
    lot_id             BIGINT      NOT NULL,
    section_id         BIGINT      NOT NULL,
    warehouse_id       BIGINT      NOT NULL,
    quantity           INT         NOT NULL,
    available_quantity INT         NOT NULL,
    alloc_status       VARCHAR(20) NOT NULL,
    quality_status     VARCHAR(20) NOT NULL,
    loc_status         VARCHAR(20) NOT NULL,
    expiry_date        DATE        NOT NULL DEFAULT '9999-12-31',
    created_by         BIGINT      NOT NULL,
    created_at         DATETIME(6) NOT NULL,
    updated_by         BIGINT,
    updated_at         DATETIME(6),
    PRIMARY KEY (inventory_id),
    CONSTRAINT uk_inventory_unit                UNIQUE (product_id, lot_id, section_id, alloc_status, quality_status, loc_status),
    CONSTRAINT chk_inventory_quantity           CHECK  (quantity >= 0),
    CONSTRAINT chk_inventory_available_quantity CHECK  (available_quantity >= 0 AND available_quantity <= quantity)
);

CREATE TABLE inventory_transaction (
    transaction_id      BIGINT      NOT NULL AUTO_INCREMENT,
    inventory_id        BIGINT      NOT NULL,
    transaction_type    VARCHAR(20) NOT NULL,
    transaction_qty     INT         NOT NULL,
    balance_qty         INT         NOT NULL,
    reference_id        BIGINT,
    prev_alloc_status   VARCHAR(20),
    prev_quality_status VARCHAR(20),
    prev_loc_status     VARCHAR(20),
    curr_alloc_status   VARCHAR(20) NOT NULL,
    curr_quality_status VARCHAR(20) NOT NULL,
    curr_loc_status     VARCHAR(20) NOT NULL,
    member_id           BIGINT      NOT NULL,
    change_reason       VARCHAR(255),
    created_by          BIGINT      NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_by          BIGINT,
    updated_at          DATETIME(6),
    PRIMARY KEY (transaction_id)
);

CREATE TABLE inbound (
    inbound_id   BIGINT      NOT NULL AUTO_INCREMENT,
    version      BIGINT,
    status       VARCHAR(20) NOT NULL,
    inbound_date DATETIME(6) NOT NULL,
    warehouse_id BIGINT      NOT NULL,
    partner_id   BIGINT      NOT NULL,
    created_by   BIGINT      NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_by   BIGINT,
    updated_at   DATETIME(6),
    PRIMARY KEY (inbound_id)
);

CREATE TABLE inbound_line (
    inbound_line_id     BIGINT      NOT NULL AUTO_INCREMENT,
    inbound_id          BIGINT      NOT NULL,
    product_id          BIGINT      NOT NULL,
    ordered_quantity    INT         NOT NULL,
    received_quantity   INT         NOT NULL DEFAULT 0,
    manufacturer_lot_no VARCHAR(50) NULL     COMMENT '실물 수령 시 확정된 제조사 로트 번호',
    manufacturing_date  DATETIME(6) NULL     COMMENT '실물 수령 시 확정된 제조일자',
    expiration_date     DATETIME(6) NULL     COMMENT '실물 수령 시 확정된 유통기한',
    created_by          BIGINT      NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_by          BIGINT,
    updated_at          DATETIME(6),
    PRIMARY KEY (inbound_line_id)
);

CREATE TABLE quality_inspection (
    inspection_id       BIGINT      NOT NULL AUTO_INCREMENT,
    source_type         VARCHAR(20) NOT NULL,
    source_id           BIGINT      NOT NULL,
    product_id          BIGINT      NOT NULL,
    lot_id              BIGINT      NOT NULL,
    warehouse_id        BIGINT      NOT NULL,
    inspector_id        BIGINT,
    status              VARCHAR(20) NOT NULL,
    inspection_quantity INT         NOT NULL,
    passed_quantity     INT         NOT NULL,
    failed_quantity     INT         NOT NULL,
    defect_reason       VARCHAR(100),
    expiry_date         DATE,
    created_by          BIGINT      NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_by          BIGINT,
    updated_at          DATETIME(6),
    PRIMARY KEY (inspection_id)
);

CREATE TABLE failed_inspection_event (
    failed_event_id         BIGINT       NOT NULL AUTO_INCREMENT,
    inbound_id              BIGINT       NOT NULL,
    line_id                 BIGINT       NOT NULL,
    product_id              BIGINT       NOT NULL,
    warehouse_id            BIGINT       NOT NULL,
    received_quantity       INT          NOT NULL,
    manufacturer_lot_number VARCHAR(100),
    expiration_date         DATE,
    error_message           TEXT,
    failed_at               DATETIME(6)  NOT NULL,
    created_by              BIGINT       NOT NULL,
    created_at              DATETIME(6)  NOT NULL,
    updated_by              BIGINT,
    updated_at              DATETIME(6),
    PRIMARY KEY (failed_event_id)
);

CREATE TABLE orders (
    orders_id    BIGINT      NOT NULL AUTO_INCREMENT,
    order_status VARCHAR(50) NOT NULL,
    store_id     BIGINT      NOT NULL,
    warehouse_id BIGINT      NOT NULL,
    created_by   BIGINT      NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_by   BIGINT,
    updated_at   DATETIME(6),
    PRIMARY KEY (orders_id)
);

CREATE TABLE orders_item (
    orders_item_id BIGINT      NOT NULL AUTO_INCREMENT,
    orders_id      BIGINT      NOT NULL,
    product_id     BIGINT      NOT NULL,
    quantity       INT         NOT NULL,
    created_by     BIGINT      NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    updated_by     BIGINT,
    updated_at     DATETIME(6),
    PRIMARY KEY (orders_item_id),
    CONSTRAINT chk_orders_item_quantity CHECK (quantity > 0)
);

CREATE TABLE outbound (
    outbound_id     BIGINT      NOT NULL AUTO_INCREMENT,
    orders_id       BIGINT      NOT NULL,
    warehouse_id    BIGINT      NOT NULL,
    outbound_type   VARCHAR(20) NOT NULL,
    outbound_status VARCHAR(20) NOT NULL,
    outbound_date   DATETIME(6),
    created_by      BIGINT      NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_by      BIGINT,
    updated_at      DATETIME(6),
    PRIMARY KEY (outbound_id)
);

CREATE TABLE outbound_item (
    outbound_item_id BIGINT NOT NULL AUTO_INCREMENT,
    outbound_id      BIGINT NOT NULL,
    orders_item_id   BIGINT NOT NULL,
    inventory_id     BIGINT NOT NULL,
    target_quantity  INT    NOT NULL,
    picked_quantity  INT    NOT NULL,
    PRIMARY KEY (outbound_item_id)
);
