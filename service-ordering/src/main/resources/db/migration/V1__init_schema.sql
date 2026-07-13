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
