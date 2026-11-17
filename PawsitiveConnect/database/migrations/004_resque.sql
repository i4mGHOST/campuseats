-- Migration: 004_rescue.sql
-- Description: Creates the rescue dispatches and incident logging tables for Member 4

CREATE TABLE IF NOT EXISTS rescue_dispatches (
                                                 id SERIAL PRIMARY KEY,
                                                 reporter_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20),
    animal_description VARCHAR(255) NOT NULL,
    location_coordinates VARCHAR(100) NOT NULL,
    urgency_level VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'REPORTED',
    report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Insert sample initial dispatch data
INSERT INTO rescue_dispatches (reporter_name, contact_number, animal_description, location_coordinates, urgency_level, status) VALUES   ('Marcus Chen', '555-0192', 'Stray Golden Retriever mix, visibly limping', '40.7128, -74.0060', 'High', 'REPORTED'),
                                                                                                                                        ('Elena Rodriguez', '555-0834', 'Kitten trapped inside storm drain', '34.0522, -118.2437', 'Critical', 'DISPATCHED'),
                                                                                                                                        ('David Kim', '555-0211', 'Tethered horse without water', '41.8781, -87.6298', 'Medium', 'REPORTED'),
                                                                                                                                        ('Sarah Jenkins', '555-0999', 'Injured hawk on roadside', '29.7604, -95.3698', 'High', 'DISPATCHED');