ALTER TABLE quality_inspection
    ADD COLUMN product_id   BIGINT NOT NULL COMMENT '검사 대상 상품 ID',
    ADD COLUMN lot_id       BIGINT NOT NULL COMMENT '검사 대상 로트 ID',
    ADD COLUMN section_id   BIGINT NOT NULL COMMENT '검사 대상 구역 ID',
    ADD COLUMN warehouse_id BIGINT NOT NULL COMMENT '검사 대상 창고 ID';
