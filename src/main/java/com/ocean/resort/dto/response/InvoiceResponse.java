package com.ocean.resort.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(
		String invoiceNumber,
		String reservationNumber,
		String guestName,
		LocalDate issueDate,
		LocalDate dueDate,
		BigDecimal roomRate,
		Integer nights,
		BigDecimal extraCharges,
		BigDecimal taxRate,
		BigDecimal discountAmount,
		BigDecimal totalAmount,
		BigDecimal amountPaid,
		BigDecimal balanceAmount,
		String status,
		List<InvoicePaymentResponse> payments
) {
}
