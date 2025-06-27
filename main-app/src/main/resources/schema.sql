DROP TABLE IF EXISTS cart_items, order_items, items, orders, images, users CASCADE;

CREATE TABLE IF NOT EXISTS images (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    image_bytes BYTEA
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(100) NOT NULL,
    image_id INT,
    price NUMERIC(10, 2) NOT NULL,
    amount INT,
    CONSTRAINT items_images FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    email VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    roles VARCHAR(255) DEFAULT 'ROLE_USER'
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT cart_items_items FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT cart_items_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    total_sum NUMERIC(10, 2),
    paid BOOLEAN DEFAULT FALSE NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT orders_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id INT,
    item_id INT NOT NULL,
    item_amount INT NOT NULL,
    CONSTRAINT order_items_orders FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT order_items_items FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Создаем тестовых пользователей с паролем "password"
INSERT INTO users (username, password, email, enabled, roles)
VALUES 
('user', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'user@example.com', true, 'ROLE_USER'),
('admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'admin@example.com', true, 'ROLE_USER,ROLE_ADMIN');