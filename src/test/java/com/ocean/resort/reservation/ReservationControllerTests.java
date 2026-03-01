package com.ocean.resort.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldCreateReservation() throws Exception {
		String reservationNumber = "RES-9001";
		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reservationNumber": "%s",
								  "guestName": "Alice Silva",
								  "address": "Unawatuna, Galle",
								  "contactNumber": "+94770000001",
								  "roomType": "Suite",
								  "checkInDate": "2026-04-10",
								  "checkOutDate": "2026-04-15"
								}
								""".formatted(reservationNumber)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.reservationNumber").value(reservationNumber));
	}

	@Test
	void shouldRejectDuplicateReservationNumber() throws Exception {
		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reservationNumber": "RES-1001",
								  "guestName": "Duplicate",
								  "address": "Galle",
								  "contactNumber": "+94770000002",
								  "roomType": "Deluxe",
								  "checkInDate": "2026-04-10",
								  "checkOutDate": "2026-04-12"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void shouldFindExistingReservation() throws Exception {
		mockMvc.perform(get("/api/v1/reservations/RES-1001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reservationNumber").value("RES-1001"))
				.andExpect(jsonPath("$.guestName").value("John Fernando"))
				.andExpect(jsonPath("$.address").value("Galle Fort, Galle"))
				.andExpect(jsonPath("$.contactNumber").value("+94771234567"))
				.andExpect(jsonPath("$.roomType").value("Deluxe"))
				.andExpect(jsonPath("$.checkInDate").value("2026-03-01"))
				.andExpect(jsonPath("$.checkOutDate").value("2026-03-05"));
	}

	@Test
	void shouldUpdateReservation() throws Exception {
		mockMvc.perform(put("/api/v1/reservations/RES-1001")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "guestName": "John Fernando Updated",
								  "address": "Galle City",
								  "contactNumber": "+94771111111",
								  "roomType": "Suite",
								  "checkInDate": "2026-03-02",
								  "checkOutDate": "2026-03-06"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.guestName").value("John Fernando Updated"));
	}

	@Test
	void shouldDeleteReservation() throws Exception {
		String reservationNumber = "RES-9010";
		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reservationNumber": "%s",
								  "guestName": "Delete Me",
								  "address": "Galle",
								  "contactNumber": "+94770000003",
								  "roomType": "Standard",
								  "checkInDate": "2026-04-20",
								  "checkOutDate": "2026-04-22"
								}
								""".formatted(reservationNumber)))
				.andExpect(status().isCreated());

		mockMvc.perform(delete("/api/v1/reservations/" + reservationNumber))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Reservation deleted successfully."));
	}

	@Test
	void shouldRejectInvalidContactNumber() throws Exception {
		mockMvc.perform(post("/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reservationNumber": "RES-9022",
								  "guestName": "Invalid Contact",
								  "address": "Galle",
								  "contactNumber": "ABC123",
								  "roomType": "Suite",
								  "checkInDate": "2026-04-10",
								  "checkOutDate": "2026-04-12"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Contact number format is invalid."));
	}
}
