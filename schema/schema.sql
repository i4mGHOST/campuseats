CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(20),
    user_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE delivery_addresses (
    address_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    building_name VARCHAR(100) NOT NULL,
    room_number VARCHAR(50),
    campus_zone VARCHAR(50),
    delivery_instructions TEXT,
    is_default BOOLEAN NOT NULL,
    CONSTRAINT fk_delivery_addresses_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE TABLE merchants (
    merchant_id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    campus_location VARCHAR(150) NOT NULL,
    is_active BOOLEAN NOT NULL,
    opening_time TIME,
    closing_time TIME,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE menu_items (
    item_id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    price_cents INT NOT NULL,
    category VARCHAR(50),
    is_available BOOLEAN NOT NULL,
    CONSTRAINT fk_menu_items_merchant
        FOREIGN KEY (merchant_id) REFERENCES merchants(merchant_id)
);
CREATE TABLE orders (
    order_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    merchant_id UUID NOT NULL,
    delivery_address_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    subtotal_cents INT NOT NULL,
    delivery_fee_cents INT NOT NULL,
    total_cents INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE order_items (
    order_item_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    item_id UUID NOT NULL,
    item_name_snapshot VARCHAR(120) NOT NULL,
    unit_price_cents INT NOT NULL,
    quantity INT NOT NULL,
    total_price_cents INT NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY,
    order_id UUID UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    amount_cents INT NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    transaction_reference VARCHAR,
    processed_at TIMESTAMP
);
CREATE TABLE deliveries (
    delivery_id UUID PRIMARY KEY,
    order_id UUID UNIQUE NOT NULL,
    courier_id UUID NOT NULL,
    pickup_location VARCHAR(150) NOT NULL,
    dropoff_location VARCHAR(150) NOT NULL,
    delivery_status VARCHAR(30) NOT NULL,
    estimated_arrival TIMESTAMP,
    delivered_at TIMESTAMP
);

CREATE TABLE message_log (
    message_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    message_content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP
);

CREATE TABLE carts (
    cart_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    merchant_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE cart_items (
    cart_item_id UUID PRIMARY KEY,
    cart_id UUID NOT NULL,
    item_id UUID NOT NULL,
    item_name_snapshot VARCHAR(120) NOT NULL,
    unit_price_cents INT NOT NULL,
    quantity INT NOT NULL,
    total_price_cents INT NOT NULL,
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts(cart_id)
);

CREATE TABLE refunds (
    refund_id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    order_id UUID NOT NULL,
    amount_cents INT NOT NULL,
    refund_status VARCHAR(30) NOT NULL,
    refund_reason VARCHAR(255),
    transaction_reference VARCHAR,
    processed_at TIMESTAMP,
    CONSTRAINT fk_refunds_payment
        FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
);

CREATE TABLE riders (
    rider_id UUID PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    phone_number VARCHAR(20),
    rider_status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE rider_assignments (
    assignment_id UUID PRIMARY KEY,
    delivery_id UUID NOT NULL,
    rider_id UUID NOT NULL,
    assignment_status VARCHAR(30) NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT fk_rider_assignments_delivery
        FOREIGN KEY (delivery_id) REFERENCES deliveries(delivery_id),
    CONSTRAINT fk_rider_assignments_rider
        FOREIGN KEY (rider_id) REFERENCES riders(rider_id)
);
