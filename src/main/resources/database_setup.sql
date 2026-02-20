-- ============================================
-- BUS BOOKING DATABASE SCHEMA
-- Run this in MySQL before starting the app
-- ============================================

CREATE DATABASE IF NOT EXISTS bus_booking;
USE bus_booking;

-- ✅ BUS TABLE
CREATE TABLE IF NOT EXISTS bus (
    bus_id INT AUTO_INCREMENT PRIMARY KEY,
    bus_name VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    available_seats INT DEFAULT 50
);

-- ✅ BUS ROUTE STOPS TABLE
CREATE TABLE IF NOT EXISTS bus_route (
    route_id INT AUTO_INCREMENT PRIMARY KEY,
    bus_id INT NOT NULL,
    city_en VARCHAR(100) NOT NULL,
    city_hi VARCHAR(100),
    stop_order INT NOT NULL,
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id) ON DELETE CASCADE
);

-- ✅ BUS SEAT TYPES TABLE
CREATE TABLE IF NOT EXISTS bus_seat_type (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bus_id INT NOT NULL,
    seat_type ENUM('SLEEPER_AC', 'CHAIR_AC') NOT NULL,
    available_seats INT DEFAULT 25,
    base_fare DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id) ON DELETE CASCADE
);

-- ✅ BOOKING TABLE
CREATE TABLE IF NOT EXISTS booking (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    bus_id INT NOT NULL,
    pnr VARCHAR(50) UNIQUE NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    from_city VARCHAR(100) NOT NULL,
    to_city VARCHAR(100) NOT NULL,
    travel_date DATE NOT NULL,
    seat_type ENUM('SLEEPER_AC', 'CHAIR_AC') NOT NULL,
    seats_booked INT NOT NULL,
    fare DECIMAL(10, 2) NOT NULL,
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id)
);

-- ============================================
-- SAMPLE DATA
-- ============================================

-- Sample Buses
INSERT INTO bus (bus_name, source, destination, available_seats) VALUES
('Punjab Express', 'Ludhiana', 'Darbhanga', 50),
('Delhi Patna Superfast', 'Delhi', 'Patna', 50),
('Lucknow Darbhanga Express', 'Lucknow', 'Darbhanga', 50);

-- Bus 1 Route: Punjab → Bihar
INSERT INTO bus_route (bus_id, city_en, city_hi, stop_order) VALUES
(1, 'Ludhiana', 'लुधियाना', 1),
(1, 'Sonipat', 'सोनीपत', 2),
(1, 'Delhi', 'दिल्ली', 3),
(1, 'Lucknow', 'लखनऊ', 4),
(1, 'Muzaffarpur', 'मुजफ्फरपुर', 5),
(1, 'Darbhanga', 'दरभंगा', 6);

-- Bus 2 Route: Delhi → Patna
INSERT INTO bus_route (bus_id, city_en, city_hi, stop_order) VALUES
(2, 'Delhi', 'दिल्ली', 1),
(2, 'Lucknow', 'लखनऊ', 2),
(2, 'Muzaffarpur', 'मुजफ्फरपुर', 3),
(2, 'Patna', 'पटना', 4);

-- Bus 3 Route: Lucknow → Darbhanga
INSERT INTO bus_route (bus_id, city_en, city_hi, stop_order) VALUES
(3, 'Lucknow', 'लखनऊ', 1),
(3, 'Muzaffarpur', 'मुजफ्फरपुर', 2),
(3, 'Jhanjharpur', 'झंझारपुर', 3),
(3, 'Darbhanga', 'दरभंगा', 4);

-- Seat Types & Fares
INSERT INTO bus_seat_type (bus_id, seat_type, available_seats, base_fare) VALUES
(1, 'SLEEPER_AC', 25, 2000.00),
(1, 'CHAIR_AC', 25, 1400.00),
(2, 'SLEEPER_AC', 25, 1500.00),
(2, 'CHAIR_AC', 25, 1000.00),
(3, 'SLEEPER_AC', 25, 1200.00),
(3, 'CHAIR_AC', 25, 800.00);