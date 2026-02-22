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
