package com.ocean.resort.dto.request;

import java.time.LocalDate;

public record ReservationRequest(
		String reservationNumber,
		String guestName,
		String address,
		String contactNumber,
		String roomType,
		LocalDate checkInDate,
		LocalDate checkOutDate
) {
}
