package com.ocean.resort.controller;

import com.ocean.resort.dto.request.InvoiceRequest;
import com.ocean.resort.dto.request.PaymentRequest;
import com.ocean.resort.dto.response.InvoiceResponse;
import com.ocean.resort.service.impl.BillingServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {
	private final BillingServiceImpl billingService;

	public BillingController(BillingServiceImpl billingService) {
		this.billingService = billingService;
	}

	@PostMapping("/invoices")
	public ResponseEntity<?> createInvoice(@RequestBody InvoiceRequest request) {
		String validationError = billingService.validateInvoiceRequest(request);
		if (validationError != null) {
			return ResponseEntity.badRequest().body(Map.of("message", validationError));
		}

		try {
			InvoiceResponse created = billingService.createInvoice(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (IllegalArgumentException ex) {
			String message = ex.getMessage();
			HttpStatus status = "Invoice number already exists.".equals(message)
					? HttpStatus.CONFLICT
					: HttpStatus.BAD_REQUEST;
			return ResponseEntity.status(status).body(Map.of("message", message));
		}
	}

	@GetMapping("/invoices")
	public List<InvoiceResponse> findAllInvoices() {
		return billingService.findAll();
	}

	@GetMapping("/invoices/{invoiceNumber}")
	public ResponseEntity<?> findInvoiceByNumber(@PathVariable String invoiceNumber) {
		Optional<InvoiceResponse> invoice = billingService.findByInvoiceNumber(invoiceNumber);
		if (invoice.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Invoice not found."));
		}
		return ResponseEntity.ok(invoice.get());
	}

	@PostMapping("/invoices/{invoiceNumber}/payments")
	public ResponseEntity<?> addPayment(@PathVariable String invoiceNumber, @RequestBody PaymentRequest request) {
		String validationError = billingService.validatePaymentRequest(request);
		if (validationError != null) {
			return ResponseEntity.badRequest().body(Map.of("message", validationError));
		}

		try {
			Optional<InvoiceResponse> updated = billingService.addPayment(invoiceNumber, request);
			if (updated.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Invoice not found."));
			}
			return ResponseEntity.ok(updated.get());
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
		}
	}
}
