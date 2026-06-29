ALTER TABLE lot DROP CONSTRAINT uq_lot_number;
ALTER TABLE lot DROP COLUMN lot_number;
ALTER TABLE lot ADD COLUMN inbound_id BIGINT NOT NULL DEFAULT 0;
ALTER TABLE lot ADD COLUMN manufacturer_lot_no VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE lot ADD CONSTRAINT uq_lot_inbound_manufacturer UNIQUE (inbound_id, manufacturer_lot_no);
