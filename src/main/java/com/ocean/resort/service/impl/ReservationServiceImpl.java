package com.ocean.resort.service.impl;

import com.ocean.resort.dto.request.ReservationRequest;
import com.ocean.resort.dto.response.ReservationResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ReservationServiceImpl {
	private static final Pattern RESERVATION_NUMBER_PATTERN = Pattern.compile("^[A-Za-z0-9-]{3,50}$");
	private static final Pattern CONTACT_NUMBER_PATTERN = Pattern.compile("^\\+?[0-9\\-\\s]{7,30}$");
	private final JdbcTemplate jdbcTemplate;

	public ReservationServiceImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<ReservationResponse> findAll() {
		String sql = """
				SELECT reservation_number, guest_name, address, contact_number, room_type, check_in_date, check_out_date
				FROM reservations
				ORDER BY check_in_date ASC
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> new ReservationResponse(
				rs.getString("reservation_number"),
				rs.getString("guest_name"),
				rs.getString("address"),
				rs.getString("contact_number"),
				rs.getString("room_type"),
				rs.getDate("check_in_date").toLocalDate(),
				rs.getDate("check_out_date").toLocalDate()
		));
	}

	public Optional<ReservationResponse> findByReservationNumber(String reservationNumber) {
		String sql = """
				SELECT reservation_number, guest_name, address, contact_number, room_type, check_in_date, check_out_date
				FROM reservations
				WHERE reservation_number = ?
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> new ReservationResponse(
						rs.getString("reservation_number"),
						rs.getString("guest_name"),
						rs.getString("address"),
						rs.getString("contact_number"),
						rs.getString("room_type"),
						rs.getDate("check_in_date").toLocalDate(),
						rs.getDate("check_out_date").toLocalDate()
				), reservationNumber)
				.stream()
				.findFirst();
	}

	public ReservationResponse create(ReservationRequest request) {
		String sql = """
				INSERT INTO reservations (
				    reservation_number, guest_name, address, contact_number, room_type, check_in_date, check_out_date
				) VALUES (?, ?, ?, ?, ?, ?, ?)
				""";
		try {
			jdbcTemplate.update(
					sql,
					request.reservationNumber().trim(),
					request.guestName().trim(),
					request.address().trim(),
					request.contactNumber().trim(),
					request.roomType().trim(),
					Date.valueOf(request.checkInDate()),
					Date.valueOf(request.checkOutDate())
			);
			return new ReservationResponse(
					request.reservationNumber().trim(),
					request.guestName().trim(),
					request.address().trim(),
					request.contactNumber().trim(),
					request.roomType().trim(),
					request.checkInDate(),
					request.checkOutDate()
			);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("Reservation number already exists.");
		}
	}

	public Optional<ReservationResponse> update(String reservationNumber, ReservationRequest request) {
		String sql = """
				UPDATE reservations
				SET guest_name = ?, address = ?, contact_number = ?, room_type = ?, check_in_date = ?, check_out_date = ?
				WHERE reservation_number = ?
				""";
		int updated = jdbcTemplate.update(
				sql,
				request.guestName().trim(),
				request.address().trim(),
				request.contactNumber().trim(),
				request.roomType().trim(),
				Date.valueOf(request.checkInDate()),
				Date.valueOf(request.checkOutDate()),
				reservationNumber
		);
		if (updated == 0) {
			return Optional.empty();
		}
		return findByReservationNumber(reservationNumber);
	}

	public boolean delete(String reservationNumber) {
		String sql = "DELETE FROM reservations WHERE reservation_number = ?";
		return jdbcTemplate.update(sql, reservationNumber) > 0;
	}

	public String validate(ReservationRequest request, boolean requireReservationNumber) {
		if (request == null) {
			return "Request body is required.";
		}
		if (requireReservationNumber) {
			if (isBlank(request.reservationNumber())) {
				return "Reservation number is required.";
			}
			String reservationNumber = request.reservationNumber().trim();
			if (!RESERVATION_NUMBER_PATTERN.matcher(reservationNumber).matches()) {
				return "Reservation number format is invalid.";
			}
		}
		if (isBlank(request.guestName()) || isBlank(request.address()) || isBlank(request.contactNumber()) || isBlank(request.roomType())) {
			return "Guest name, address, contact number, and room type are required.";
		}
		String guestName = request.guestName().trim();
		String address = request.address().trim();
		String contactNumber = request.contactNumber().trim();
		String roomType = request.roomType().trim();
		if (guestName.length() > 150 || address.length() > 300 || contactNumber.length() > 30 || roomType.length() > 80) {
			return "One or more fields exceed maximum allowed length.";
		}
		if (!CONTACT_NUMBER_PATTERN.matcher(contactNumber).matches()) {
			return "Contact number format is invalid.";
		}
		LocalDate checkInDate = request.checkInDate();
		LocalDate checkOutDate = request.checkOutDate();
		if (checkInDate == null || checkOutDate == null) {
			return "Check-in and check-out dates are required.";
		}
		if (!checkOutDate.isAfter(checkInDate)) {
			return "Check-out date must be after check-in date.";
		}
		return null;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
