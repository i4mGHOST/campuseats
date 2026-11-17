-- Migration: 003_medical.sql
-- Description: Creates the medical records table for Member 3's module

CREATE TABLE IF NOT EXISTS medical_records (
                                               id SERIAL PRIMARY KEY,
                                               pet_name VARCHAR(100) NOT NULL,
    treatment VARCHAR(150) NOT NULL,
    date_administered DATE NOT NULL,
    veterinarian VARCHAR(100) NOT NULL,
    notes TEXT
    );

-- Insert sample initial medical data
INSERT INTO medical_records (pet_name, treatment, date_administered, veterinarian, notes) VALUES    ('Max', 'Rabies Vaccine', '2026-08-15', 'Dr. Sarah Jenkins', 'Annual booster. Healthy condition.'),
                                                                                                    ('Luna', 'Flea & Tick Treatment', '2026-09-10', 'Dr. Mark Sloan', 'Applied topical treatment.'),
                                                                                                    ('Milo', 'X-Ray & Leg Splint', '2026-09-22', 'Dr. Sarah Jenkins', 'Fractured front left paw. Resting.'),
                                                                                                    ('Bella', 'Spay Surgery', '2026-09-05', 'Dr. Emily Chen', 'Routine surgery, recovered beautifully.'),
                                                                                                    ('Rocky', 'Deworming', '2026-09-18', 'Dr. Mark Sloan', 'Standard intake procedure.');