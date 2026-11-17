-- Migration: 002_pets.sql
-- Description: Creates the pets catalog table for Member 2's module

CREATE TABLE IF NOT EXISTS pets (
                                    id SERIAL PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    status VARCHAR(50) DEFAULT 'Available',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Insert sample initial data
INSERT INTO pets (name, species, breed, age, status) VALUES
                                                         ('Max', 'Dog', 'Golden Retriever', 2, 'Available'),
                                                         ('Luna', 'Cat', 'Siamese', 1, 'Available');