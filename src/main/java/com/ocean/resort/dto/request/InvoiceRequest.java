package com.ocean.resort.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceRequest(
		String invoiceNumber,
		String reservationNumber,
		LocalDate issueDate,
		LocalDate dueDate,
		BigDecimal roomRate,
		Integer nights,
		BigDecimal extraCharges,
		BigDecimal taxRate,
		BigDecimal discountAmount
) {
}
