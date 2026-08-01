ALTER TABLE orders_item
    ADD CONSTRAINT fk_orders_item_orders FOREIGN KEY (orders_id) REFERENCES orders (orders_id);

ALTER TABLE failed_assignment_event
    ADD CONSTRAINT fk_failed_assignment_orders FOREIGN KEY (orders_id) REFERENCES orders (orders_id);
