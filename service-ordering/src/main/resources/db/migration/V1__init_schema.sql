CREATE TABLE member (
    member_id    BIGINT      NOT NULL AUTO_INCREMENT,
    role         VARCHAR(50) NOT NULL,
    member_name  VARCHAR(50) NOT NULL,
    email        VARCHAR(50) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    created_by   BIGINT      NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_by   BIGINT,
    updated_at   DATETIME(6),
    PRIMARY KEY (member_id),
    CONSTRAINT uq_email        UNIQUE (email),
    CONSTRAINT uq_phone_number UNIQUE (phone_number)
);

CREATE TABLE credential (
    credential_id BIGINT       NOT NULL AUTO_INCREMENT,
    member_id     BIGINT       NOT NULL,
    login_id      VARCHAR(50)  NOT NULL,
    password      VARCHAR(255) NOT NULL,
    created_by    BIGINT       NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_by    BIGINT,
    updated_at    DATETIME(6),
    PRIMARY KEY (credential_id),
    CONSTRAINT uq_login_id          UNIQUE (login_id),
    CONSTRAINT uq_credential_member UNIQUE (member_id)
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
    sku_code          VARCHAR(50),
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

CREATE TABLE orders (
    orders_id    BIGINT      NOT NULL AUTO_INCREMENT,
    order_status VARCHAR(20) NOT NULL,
    store_id     BIGINT      NOT NULL,
    warehouse_id BIGINT,
    created_by   BIGINT      NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_by   BIGINT,
    updated_at   DATETIME(6),
    PRIMARY KEY (orders_id),
    CONSTRAINT chk_order_status CHECK (order_status IN ('PENDING','CONFIRMED','PREPARING','SHIPPED','DELIVERED','CANCELED'))
);

CREATE TABLE orders_item (
    orders_item_id BIGINT NOT NULL AUTO_INCREMENT,
    orders_id      BIGINT NOT NULL,
    product_id     BIGINT NOT NULL,
    quantity       INT    NOT NULL,
    created_by     BIGINT      NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    updated_by     BIGINT,
    updated_at     DATETIME(6),
    PRIMARY KEY (orders_item_id),
    CONSTRAINT chk_orders_item_quantity CHECK (quantity > 0)
);

CREATE TABLE failed_assignment_event (
    failed_event_id BIGINT      NOT NULL AUTO_INCREMENT,
    orders_id       BIGINT      NOT NULL,
    store_id        BIGINT      NOT NULL,
    error_message   TEXT,
    failed_at       DATETIME(6) NOT NULL,
    created_by      BIGINT      NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_by      BIGINT,
    updated_at      DATETIME(6),
    PRIMARY KEY (failed_event_id)
);
