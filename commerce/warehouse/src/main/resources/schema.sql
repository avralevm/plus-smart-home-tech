DROP TABLE IF EXISTS warehouse_product CASCADE;

CREATE TABLE IF NOT EXISTS warehouse_product (
    product_id UUID PRIMARY KEY,
    width NUMERIC(19, 4) NOT NULL,
    height NUMERIC(19, 4) NOT NULL,
    depth NUMERIC(19, 4) NOT NULL,
    weight NUMERIC(19, 4) NOT NULL,
    fragile BOOLEAN NOT NULL,
    quantity BIGINT NOT NULL
);