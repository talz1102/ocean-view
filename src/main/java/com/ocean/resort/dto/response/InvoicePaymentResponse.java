package com.ocean.resort.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoicePaymentResponse(
		LocalDate paymentDate,
		BigDecimal amount,
		String paymentMethod,
		String notes
) {
}
