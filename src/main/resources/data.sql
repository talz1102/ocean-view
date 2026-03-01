INSERT INTO app_users (username, password_hash)
SELECT 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE username = 'admin'
);

INSERT INTO reservations (
    reservation_number,
    guest_name,
    address,
    contact_number,
    room_type,
    check_in_date,
    check_out_date
)
SELECT 'RES-1001', 'John Fernando', 'Galle Fort, Galle', '+94771234567', 'Deluxe', '2026-03-01', '2026-03-05'
WHERE NOT EXISTS (
    SELECT 1 FROM reservations WHERE reservation_number = 'RES-1001'
);

INSERT INTO invoices (
    invoice_number,
    reservation_number,
    issue_date,
    due_date,
    room_rate,
    nights,
    extra_charges,
    tax_rate,
    discount_amount,
    total_amount,
    amount_paid,
    status
)
SELECT
    'INV-1001',
    'RES-1001',
    '2026-03-01',
    '2026-03-05',
    120.00,
    4,
    60.00,
    10.00,
    20.00,
    564.00,
    200.00,
    'PARTIAL'
WHERE NOT EXISTS (
    SELECT 1 FROM invoices WHERE invoice_number = 'INV-1001'
);

INSERT INTO invoice_payments (
    invoice_number,
    payment_date,
    amount,
    payment_method,
    notes
)
SELECT
    'INV-1001',
    '2026-03-02',
    200.00,
    'CARD',
    'Advance payment'
WHERE NOT EXISTS (
    SELECT 1 FROM invoice_payments WHERE invoice_number = 'INV-1001' AND amount = 200.00
);
