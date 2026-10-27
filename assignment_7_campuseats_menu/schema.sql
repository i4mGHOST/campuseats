-- 1. Create the database
CREATE DATABASE IF NOT EXISTS campuseats_db;
USE campuseats_db;

-- Drop table if it exists to reset with the UNIQUE constraint
DROP TABLE IF EXISTS dishes;

-- 2. Create the dishes table with a UNIQUE constraint on name
CREATE TABLE dishes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. Insert sample seed rows
INSERT INTO dishes (name, price, available) VALUES
('Chicken Biryani', 180.00, true),
('Veg Fried Rice', 120.00, true),
('Paneer Butter Masala', 220.00, false),
('Masala Dosa', 80.00, true);