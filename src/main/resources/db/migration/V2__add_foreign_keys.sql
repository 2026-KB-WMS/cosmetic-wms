ALTER TABLE section
    ADD CONSTRAINT fk_section_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (warehouse_id);

ALTER TABLE product
    ADD CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES category (category_id);

ALTER TABLE product
    ADD CONSTRAINT fk_product_type
        FOREIGN KEY (type_id) REFERENCES product_type (type_id);

ALTER TABLE lot
    ADD CONSTRAINT fk_lot_product
        FOREIGN KEY (product_id) REFERENCES product (product_id);

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id) REFERENCES product (product_id);

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_lot
        FOREIGN KEY (lot_id) REFERENCES lot (lot_id);

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_section
        FOREIGN KEY (section_id) REFERENCES section (section_id);

ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (warehouse_id);

ALTER TABLE inventory_transaction
    ADD CONSTRAINT fk_inv_tx_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id);

ALTER TABLE inventory_transaction
    ADD CONSTRAINT fk_inv_tx_member
        FOREIGN KEY (member_id) REFERENCES member (member_id);

ALTER TABLE inbound
    ADD CONSTRAINT fk_inbound_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (warehouse_id);

ALTER TABLE inbound
    ADD CONSTRAINT fk_inbound_partner
        FOREIGN KEY (partner_id) REFERENCES partner (partner_id);

ALTER TABLE inbound_item
    ADD CONSTRAINT fk_inbound_item_inbound
        FOREIGN KEY (inbound_id) REFERENCES inbound (inbound_id);

ALTER TABLE inbound_item
    ADD CONSTRAINT fk_inbound_item_product
        FOREIGN KEY (product_id) REFERENCES product (product_id);

ALTER TABLE inbound_item
    ADD CONSTRAINT fk_inbound_item_lot
        FOREIGN KEY (lot_id) REFERENCES lot (lot_id);

ALTER TABLE inbound_item
    ADD CONSTRAINT fk_inbound_item_section
        FOREIGN KEY (section_id) REFERENCES section (section_id);

ALTER TABLE quality_inspection
    ADD CONSTRAINT fk_quality_inspection_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id);

ALTER TABLE quality_inspection
    ADD CONSTRAINT fk_quality_inspection_inspector
        FOREIGN KEY (inspector_id) REFERENCES member (member_id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_store
        FOREIGN KEY (store_id) REFERENCES store (store_id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (warehouse_id);

ALTER TABLE orders_item
    ADD CONSTRAINT fk_orders_item_orders
        FOREIGN KEY (orders_id) REFERENCES orders (id) ON DELETE CASCADE;

ALTER TABLE orders_item
    ADD CONSTRAINT fk_orders_item_product
        FOREIGN KEY (product_id) REFERENCES product (product_id);

ALTER TABLE outbound
    ADD CONSTRAINT fk_outbound_orders
        FOREIGN KEY (orders_id) REFERENCES orders (id);

ALTER TABLE outbound
    ADD CONSTRAINT fk_outbound_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (warehouse_id);

ALTER TABLE outbound_item
    ADD CONSTRAINT fk_outbound_item_outbound
        FOREIGN KEY (outbound_id) REFERENCES outbound (id);

ALTER TABLE outbound_item
    ADD CONSTRAINT fk_outbound_item_orders_item
        FOREIGN KEY (orders_item_id) REFERENCES orders_item (id);

ALTER TABLE outbound_item
    ADD CONSTRAINT fk_outbound_item_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id);
