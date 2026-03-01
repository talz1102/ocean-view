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

CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    reservation_number VARCHAR(50) NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    room_rate DECIMAL(12,2) NOT NULL,
    nights INT NOT NULL,
    extra_charges DECIMAL(12,2) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    amount_paid DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_invoices_reservation
        FOREIGN KEY (reservation_number) REFERENCES reservations (reservation_number)
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS invoice_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(50) NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    notes VARCHAR(250),
    CONSTRAINT fk_payments_invoice
        FOREIGN KEY (invoice_number) REFERENCES invoices (invoice_number)
        ON DELETE CASCADE
);
