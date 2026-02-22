CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_number VARCHAR(50) NOT NULL UNIQUE,
    guest_name VARCHAR(150) NOT NULL,
    address VARCHAR(300) NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    room_type VARCHAR(80) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL
);
