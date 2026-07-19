ALTER TABLE credential
    ADD CONSTRAINT fk_credential_member FOREIGN KEY (member_id) REFERENCES member (member_id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_store FOREIGN KEY (store_id) REFERENCES store (store_id);

ALTER TABLE orders_item
    ADD CONSTRAINT fk_orders_item_orders FOREIGN KEY (orders_id) REFERENCES orders (orders_id);

ALTER TABLE failed_assignment_event
    ADD CONSTRAINT fk_failed_assignment_orders FOREIGN KEY (orders_id) REFERENCES orders (orders_id);
