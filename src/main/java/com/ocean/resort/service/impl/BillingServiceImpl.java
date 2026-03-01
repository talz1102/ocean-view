package com.ocean.resort.service.impl;

import com.ocean.resort.dto.request.InvoiceRequest;
import com.ocean.resort.dto.request.PaymentRequest;
import com.ocean.resort.dto.response.InvoicePaymentResponse;
import com.ocean.resort.dto.response.InvoiceResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class BillingServiceImpl {
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private static final String STATUS_PENDING = "PENDING";
	private static final String STATUS_PARTIAL = "PARTIAL";
	private static final String STATUS_PAID = "PAID";
	private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile("^[A-Za-z0-9-]{3,50}$");
	private static final Pattern RESERVATION_NUMBER_PATTERN = Pattern.compile("^[A-Za-z0-9-]{3,50}$");
	private static final Set<String> ALLOWED_PAYMENT_METHODS = Set.of("CASH", "CARD", "BANK_TRANSFER");

	private final JdbcTemplate jdbcTemplate;

	public BillingServiceImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<InvoiceResponse> findAll() {
		String sql = """
				SELECT i.invoice_number, i.reservation_number, r.guest_name, i.issue_date, i.due_date,
				       i.room_rate, i.nights, i.extra_charges, i.tax_rate, i.discount_amount,
				       i.total_amount, i.amount_paid, i.status
				FROM invoices i
				JOIN reservations r ON r.reservation_number = i.reservation_number
				ORDER BY i.issue_date DESC, i.invoice_number DESC
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> toInvoiceResponse(
				rs.getString("invoice_number"),
				rs.getString("reservation_number"),
				rs.getString("guest_name"),
				rs.getDate("issue_date").toLocalDate(),
				rs.getDate("due_date").toLocalDate(),
				rs.getBigDecimal("room_rate"),
				rs.getInt("nights"),
				rs.getBigDecimal("extra_charges"),
				rs.getBigDecimal("tax_rate"),
				rs.getBigDecimal("discount_amount"),
				rs.getBigDecimal("total_amount"),
				rs.getBigDecimal("amount_paid"),
				rs.getString("status")
		));
	}

	public Optional<InvoiceResponse> findByInvoiceNumber(String invoiceNumber) {
		String sql = """
				SELECT i.invoice_number, i.reservation_number, r.guest_name, i.issue_date, i.due_date,
				       i.room_rate, i.nights, i.extra_charges, i.tax_rate, i.discount_amount,
				       i.total_amount, i.amount_paid, i.status
				FROM invoices i
				JOIN reservations r ON r.reservation_number = i.reservation_number
				WHERE i.invoice_number = ?
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> toInvoiceResponse(
				rs.getString("invoice_number"),
				rs.getString("reservation_number"),
				rs.getString("guest_name"),
				rs.getDate("issue_date").toLocalDate(),
				rs.getDate("due_date").toLocalDate(),
				rs.getBigDecimal("room_rate"),
				rs.getInt("nights"),
				rs.getBigDecimal("extra_charges"),
				rs.getBigDecimal("tax_rate"),
				rs.getBigDecimal("discount_amount"),
				rs.getBigDecimal("total_amount"),
				rs.getBigDecimal("amount_paid"),
				rs.getString("status")
		), invoiceNumber).stream().findFirst();
	}

	public InvoiceResponse createInvoice(InvoiceRequest request) {
		BigDecimal roomRate = normalizeMoney(request.roomRate());
		BigDecimal extraCharges = normalizeMoney(request.extraCharges());
		BigDecimal discount = normalizeMoney(request.discountAmount());
		BigDecimal taxRate = normalizePercent(request.taxRate());

		BigDecimal subtotal = roomRate.multiply(BigDecimal.valueOf(request.nights()))
				.add(extraCharges)
				.subtract(discount);
		if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
			subtotal = BigDecimal.ZERO;
		}
		BigDecimal tax = subtotal.multiply(taxRate).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
		BigDecimal totalAmount = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

		String sql = """
				INSERT INTO invoices (
				    invoice_number, reservation_number, issue_date, due_date,
				    room_rate, nights, extra_charges, tax_rate, discount_amount,
				    total_amount, amount_paid, status
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";
		try {
			jdbcTemplate.update(sql,
					request.invoiceNumber().trim(),
					request.reservationNumber().trim(),
					Date.valueOf(request.issueDate()),
					Date.valueOf(request.dueDate()),
					roomRate,
					request.nights(),
					extraCharges,
					taxRate,
					discount,
					totalAmount,
					BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
					STATUS_PENDING
			);
		} catch (DataIntegrityViolationException ex) {
			if (existsInvoiceNumber(request.invoiceNumber())) {
				throw new IllegalArgumentException("Invoice number already exists.");
			}
			throw new IllegalArgumentException("Reservation number not found.");
		}

		return findByInvoiceNumber(request.invoiceNumber().trim())
				.orElseThrow(() -> new IllegalStateException("Invoice created but unable to fetch it."));
	}

	@Transactional
	public Optional<InvoiceResponse> addPayment(String invoiceNumber, PaymentRequest request) {
		String normalizedInvoiceNumber = invoiceNumber == null ? "" : invoiceNumber.trim();
		Optional<InvoicePaymentContext> context = findInvoicePaymentContext(normalizedInvoiceNumber);
		if (context.isEmpty()) {
			return Optional.empty();
		}

		InvoicePaymentContext current = context.get();
		BigDecimal paymentAmount = normalizeMoney(request.amount());
		BigDecimal nextAmountPaid = current.amountPaid().add(paymentAmount).setScale(2, RoundingMode.HALF_UP);
		if (nextAmountPaid.compareTo(current.totalAmount()) > 0) {
			throw new IllegalArgumentException("Payment exceeds outstanding balance.");
		}
		if (request.paymentDate().isBefore(current.issueDate())) {
			throw new IllegalArgumentException("Payment date cannot be before invoice issue date.");
		}
		String paymentMethod = request.paymentMethod().trim().toUpperCase();

		String insertPaymentSql = """
				INSERT INTO invoice_payments (invoice_number, payment_date, amount, payment_method, notes)
				VALUES (?, ?, ?, ?, ?)
				""";
		jdbcTemplate.update(
				insertPaymentSql,
				normalizedInvoiceNumber,
				Date.valueOf(request.paymentDate()),
				paymentAmount,
				paymentMethod,
				normalizeNotes(request.notes())
		);

		String nextStatus = resolveStatus(nextAmountPaid, current.totalAmount());
		String updateInvoiceSql = """
				UPDATE invoices
				SET amount_paid = ?, status = ?
				WHERE invoice_number = ?
				""";
		jdbcTemplate.update(updateInvoiceSql, nextAmountPaid, nextStatus, normalizedInvoiceNumber);

		return findByInvoiceNumber(normalizedInvoiceNumber);
	}

	public String validateInvoiceRequest(InvoiceRequest request) {
		if (request == null) {
			return "Request body is required.";
		}
		if (isBlank(request.invoiceNumber()) || isBlank(request.reservationNumber())) {
			return "Invoice number and reservation number are required.";
		}
		String invoiceNumber = request.invoiceNumber().trim();
		String reservationNumber = request.reservationNumber().trim();
		if (!INVOICE_NUMBER_PATTERN.matcher(invoiceNumber).matches() || !RESERVATION_NUMBER_PATTERN.matcher(reservationNumber).matches()) {
			return "Invoice number or reservation number format is invalid.";
		}
		if (request.issueDate() == null || request.dueDate() == null) {
			return "Issue date and due date are required.";
		}
		if (request.dueDate().isBefore(request.issueDate())) {
			return "Due date cannot be before issue date.";
		}
		if (request.nights() == null || request.nights() <= 0) {
			return "Nights must be greater than 0.";
		}
		if (request.roomRate() == null || request.extraCharges() == null || request.taxRate() == null || request.discountAmount() == null) {
			return "Room rate, extra charges, tax rate, and discount amount are required.";
		}
		if (request.roomRate().compareTo(BigDecimal.ZERO) < 0 ||
				request.extraCharges().compareTo(BigDecimal.ZERO) < 0 ||
				request.discountAmount().compareTo(BigDecimal.ZERO) < 0 ||
				request.taxRate().compareTo(BigDecimal.ZERO) < 0) {
			return "Rates and amounts cannot be negative.";
		}
		if (request.taxRate().compareTo(ONE_HUNDRED) > 0) {
			return "Tax rate cannot be greater than 100.";
		}
		return null;
	}

	public String validatePaymentRequest(PaymentRequest request) {
		if (request == null) {
			return "Request body is required.";
		}
		if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
			return "Payment amount must be greater than 0.";
		}
		if (request.paymentDate() == null) {
			return "Payment date is required.";
		}
		if (isBlank(request.paymentMethod())) {
			return "Payment method is required.";
		}
		String paymentMethod = request.paymentMethod().trim().toUpperCase();
		if (!ALLOWED_PAYMENT_METHODS.contains(paymentMethod)) {
			return "Unsupported payment method.";
		}
		if (request.notes() != null && request.notes().trim().length() > 250) {
			return "Notes cannot exceed 250 characters.";
		}
		return null;
	}

	private Optional<InvoicePaymentContext> findInvoicePaymentContext(String invoiceNumber) {
		String sql = "SELECT total_amount, amount_paid, issue_date FROM invoices WHERE invoice_number = ?";
		return jdbcTemplate.query(sql, (rs, rowNum) -> new InvoicePaymentContext(
				rs.getBigDecimal("total_amount"),
				rs.getBigDecimal("amount_paid"),
				rs.getDate("issue_date").toLocalDate()
		), invoiceNumber).stream().findFirst();
	}

	private InvoiceResponse toInvoiceResponse(
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
			String status
	) {
		BigDecimal normalizedTotal = normalizeMoney(totalAmount);
		BigDecimal normalizedPaid = normalizeMoney(amountPaid);
		BigDecimal balance = normalizedTotal.subtract(normalizedPaid).setScale(2, RoundingMode.HALF_UP);
		return new InvoiceResponse(
				invoiceNumber,
				reservationNumber,
				guestName,
				issueDate,
				dueDate,
				normalizeMoney(roomRate),
				nights,
				normalizeMoney(extraCharges),
				normalizePercent(taxRate),
				normalizeMoney(discountAmount),
				normalizedTotal,
				normalizedPaid,
				balance,
				status,
				findPayments(invoiceNumber)
		);
	}

	private List<InvoicePaymentResponse> findPayments(String invoiceNumber) {
		String sql = """
				SELECT payment_date, amount, payment_method, notes
				FROM invoice_payments
				WHERE invoice_number = ?
				ORDER BY payment_date DESC, id DESC
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> new InvoicePaymentResponse(
				rs.getDate("payment_date").toLocalDate(),
				normalizeMoney(rs.getBigDecimal("amount")),
				rs.getString("payment_method"),
				rs.getString("notes")
		), invoiceNumber);
	}

	private boolean existsInvoiceNumber(String invoiceNumber) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM invoices WHERE invoice_number = ?",
				Integer.class,
				invoiceNumber.trim()
		);
		return count != null && count > 0;
	}

	private String resolveStatus(BigDecimal amountPaid, BigDecimal totalAmount) {
		if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
			return STATUS_PENDING;
		}
		if (amountPaid.compareTo(totalAmount) >= 0) {
			return STATUS_PAID;
		}
		return STATUS_PARTIAL;
	}

	private String normalizeNotes(String notes) {
		if (notes == null) {
			return null;
		}
		String trimmed = notes.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private BigDecimal normalizeMoney(BigDecimal amount) {
		return amount.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal normalizePercent(BigDecimal rate) {
		return rate.setScale(2, RoundingMode.HALF_UP);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private record InvoicePaymentContext(BigDecimal totalAmount, BigDecimal amountPaid, LocalDate issueDate) {
	}
}
