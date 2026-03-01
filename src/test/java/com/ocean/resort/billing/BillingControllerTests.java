package com.ocean.resort.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BillingControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldCreateInvoice() throws Exception {
		String invoiceNumber = "INV-9001";
		mockMvc.perform(post("/api/v1/billing/invoices")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "invoiceNumber": "%s",
								  "reservationNumber": "RES-1001",
								  "issueDate": "2026-04-01",
								  "dueDate": "2026-04-05",
								  "roomRate": 150.00,
								  "nights": 4,
								  "extraCharges": 50.00,
								  "taxRate": 10.00,
								  "discountAmount": 20.00
								}
								""".formatted(invoiceNumber)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.invoiceNumber").value(invoiceNumber))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.totalAmount").value(693.00));
	}

	@Test
	void shouldFindSeededInvoice() throws Exception {
		mockMvc.perform(get("/api/v1/billing/invoices/INV-1001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceNumber").value("INV-1001"))
				.andExpect(jsonPath("$.reservationNumber").value("RES-1001"))
				.andExpect(jsonPath("$.status").value(anyOf(equalTo("PARTIAL"), equalTo("PAID"))))
				.andExpect(jsonPath("$.amountPaid").value(greaterThanOrEqualTo(200.00)));
	}

	@Test
	void shouldAddPaymentAndMarkInvoicePaid() throws Exception {
		mockMvc.perform(post("/api/v1/billing/invoices/INV-1001/payments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 364.00,
								  "paymentDate": "2026-03-04",
								  "paymentMethod": "CASH",
								  "notes": "Final settlement"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PAID"))
				.andExpect(jsonPath("$.amountPaid").value(564.00))
				.andExpect(jsonPath("$.balanceAmount").value(0.00));
	}
}
