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
