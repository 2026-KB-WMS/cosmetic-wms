ALTER TABLE inventory
    ADD COLUMN expiry_date DATE NOT NULL DEFAULT '9999-12-31';

ALTER TABLE quality_inspection
    ADD COLUMN expiry_date DATE;