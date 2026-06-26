ALTER TABLE quality_inspection ADD COLUMN product_id   BIGINT NOT NULL;
ALTER TABLE quality_inspection ADD COLUMN lot_id       BIGINT NOT NULL;
ALTER TABLE quality_inspection ADD COLUMN section_id   BIGINT NOT NULL;
ALTER TABLE quality_inspection ADD COLUMN warehouse_id BIGINT NOT NULL;