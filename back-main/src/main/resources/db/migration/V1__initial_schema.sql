CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE roles (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_code VARCHAR(50) NOT NULL REFERENCES roles(code),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_role ON users(role_code);

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    specification_schema JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE brands (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    brand_id UUID REFERENCES brands(id),
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    price NUMERIC(12, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    embedding vector(1024),
    specifications JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_brand ON products(brand_id);

CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_product_images_product ON product_images(product_id);

CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL CHECK (type IN ('ENTRY', 'EXIT', 'ADJUSTMENT')),
    quantity INTEGER NOT NULL,
    stock_before INTEGER NOT NULL,
    stock_after INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    reference_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_type ON stock_movements(type);

CREATE TABLE carts (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(user_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id)
);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);

CREATE TABLE order_statuses (
    code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status_code VARCHAR(50) NOT NULL REFERENCES order_statuses(code),
    total NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    shipping_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status_code);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    product_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    payment_method VARCHAR(50) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_events_session ON events(session_id);
CREATE INDEX idx_events_user ON events(user_id);
CREATE INDEX idx_events_type ON events(event_type);

CREATE TABLE search_queries (
    normalized_query VARCHAR(100) PRIMARY KEY,
    search_term VARCHAR(100) NOT NULL,
    search_count BIGINT NOT NULL DEFAULT 0,
    searches_with_click BIGINT NOT NULL DEFAULT 0,
    products_found BIGINT NOT NULL DEFAULT 0,
    first_searched_at TIMESTAMPTZ NOT NULL,
    last_searched_at TIMESTAMPTZ NOT NULL,
    embedding vector(1024)
);
CREATE INDEX idx_search_queries_frequency
    ON search_queries (search_count DESC, last_searched_at DESC);
CREATE INDEX idx_search_queries_pending_embeddings
    ON search_queries (search_count DESC, last_searched_at DESC)
    WHERE embedding IS NULL;

CREATE TABLE application_logs (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    occurred_at TIMESTAMPTZ NOT NULL,
    level VARCHAR(10) NOT NULL,
    logger_name TEXT NOT NULL,
    thread_name TEXT NOT NULL,
    message TEXT,
    mdc JSONB NOT NULL DEFAULT '{}'::jsonb,
    throwable JSONB,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_application_logs_occurred_at ON application_logs (occurred_at DESC);
CREATE INDEX idx_application_logs_level_occurred_at ON application_logs (level, occurred_at DESC);

INSERT INTO roles (code, name) VALUES
    ('ROLE_ADMIN', 'Administrador'),
    ('ROLE_CUSTOMER', 'Cliente');

INSERT INTO order_statuses (code, name) VALUES
    ('PENDING', 'Pendiente'),
    ('PAID', 'Pagado'),
    ('PROCESSING', 'En Proceso'),
    ('SHIPPED', 'Enviado'),
    ('DELIVERED', 'Entregado'),
    ('CANCELLED', 'Cancelado');
