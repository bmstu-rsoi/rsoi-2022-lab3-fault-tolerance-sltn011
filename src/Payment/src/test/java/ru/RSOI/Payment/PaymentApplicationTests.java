package ru.RSOI.Payment;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.RSOI.Payment.Controller.CPayment;
import ru.RSOI.Payment.Error.ENotFoundError;
import ru.RSOI.Payment.Model.MPayment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentApplicationTests {

	@Autowired
	private CPayment paymentController;

	@Test
	@Order(1)
	void testGetNonexistingPayment()
	{
		assertThrows(ENotFoundError.class,
				() -> paymentController.getPayments("11111111-2222-3333-4444-000000000000"));
	}

	@Test
	@Order(2)
	void testCreatePayment()
	{
		MPayment payment = paymentController.createPayment(100);
		assertEquals(100, payment.v3_price);
	}

	@Test
	@Order(3)
	void testGetAll()
	{
		List<MPayment> payments = paymentController.getAll();
		assertNotEquals(0, payments.size());
	}

}
