DROP TABLE IF EXISTS warehouse_product CASCADE;
DROP TABLE IF EXISTS booking CASCADE;

CREATE TABLE IF NOT EXISTS warehouse_product (
    product_id UUID PRIMARY KEY,
    width NUMERIC(19, 4) NOT NULL,
    height NUMERIC(19, 4) NOT NULL,
    depth NUMERIC(19, 4) NOT NULL,
    weight NUMERIC(19, 4) NOT NULL,
    fragile BOOLEAN NOT NULL,
    quantity BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    order_id UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS booking_products (
    booking_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT,
    CONSTRAINT booking_products_pk PRIMARY KEY (booking_id, product_id),
    CONSTRAINT booking_products_booking_fk FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);