package com.ocean.resort.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
		BigDecimal amount,
		LocalDate paymentDate,
		String paymentMethod,
		String notes
) {
}
