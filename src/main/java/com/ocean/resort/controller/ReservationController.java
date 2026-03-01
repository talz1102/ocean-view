package com.ocean.resort.controller;

import com.ocean.resort.dto.request.ReservationRequest;
import com.ocean.resort.dto.response.ReservationResponse;
import com.ocean.resort.service.impl.ReservationServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
	private final ReservationServiceImpl reservationService;

	public ReservationController(ReservationServiceImpl reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping
	public ResponseEntity<?> create(@RequestBody ReservationRequest request) {
		String validationError = reservationService.validate(request, true);
		if (validationError != null) {
			return ResponseEntity.badRequest().body(Map.of("message", validationError));
		}

		try {
			ReservationResponse created = reservationService.create(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
		}
	}

	@GetMapping
	public List<ReservationResponse> findAll() {
		return reservationService.findAll();
	}

	@GetMapping("/{reservationNumber}")
	public ResponseEntity<?> findByReservationNumber(@PathVariable String reservationNumber) {
		Optional<ReservationResponse> reservation = reservationService.findByReservationNumber(reservationNumber);
		if (reservation.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Reservation not found."));
		}
		return ResponseEntity.ok(reservation.get());
	}

	@PutMapping("/{reservationNumber}")
	public ResponseEntity<?> update(@PathVariable String reservationNumber, @RequestBody ReservationRequest request) {
		String validationError = reservationService.validate(request, false);
		if (validationError != null) {
			return ResponseEntity.badRequest().body(Map.of("message", validationError));
		}

		Optional<ReservationResponse> updated = reservationService.update(reservationNumber, request);
		if (updated.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Reservation not found."));
		}
		return ResponseEntity.ok(updated.get());
	}

	@DeleteMapping("/{reservationNumber}")
	public ResponseEntity<?> delete(@PathVariable String reservationNumber) {
		boolean removed = reservationService.delete(reservationNumber);
		if (!removed) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Reservation not found."));
		}
		return ResponseEntity.ok(Map.of("message", "Reservation deleted successfully."));
	}
}
