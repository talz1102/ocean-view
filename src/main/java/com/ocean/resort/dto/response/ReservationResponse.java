package com.ocean.resort.dto.response;

import java.time.LocalDate;

public record ReservationResponse(
		String reservationNumber,
		String guestName,
		String address,
		String contactNumber,
		String roomType,
		LocalDate checkInDate,
		LocalDate checkOutDate
) {
}
