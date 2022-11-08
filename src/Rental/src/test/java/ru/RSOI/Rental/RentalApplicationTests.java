package ru.RSOI.Rental;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.RSOI.Rental.Controller.CRental;
import ru.RSOI.Rental.Model.MRental;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RentalApplicationTests {

	@Autowired
	private CRental rentController;

	@Test
	@Order(1)
	void testGetNonExistingUserRents()
	{
		List<MRental> rents = rentController.getUserRents("UnknownUser");
		assertEquals(0, rents.size());
	}

	@Test
	@Order(2)
	void testAddRent()
	{
		UUID carUid = UUID.randomUUID();
		UUID paymentUid = UUID.randomUUID();

		Map<String, String> values = new HashMap<>();
		values.put("carUid", carUid.toString());
		values.put("paymentUid", paymentUid.toString());
		values.put("dateFrom", "2021-10-08");
		values.put("dateTo", "2021-10-09");
		MRental rent = rentController.tryRenting("TestUser", values);
		assertEquals(carUid, rent.v4_car_uid);
	}

	@Test
	@Order(3)
	void testGetExisting()
	{
		List<MRental> rents = rentController.getUserRents("TestUser");
		assertNotEquals(0, rents.size());
	}

	@Test
	@Order(4)
	void testFinishExisting()
	{
		MRental rent = rentController.getUserRents("TestUser").get(0);
		rentController.finish(rent.v1_rental_uid.toString(), "TestUser");
		rent = rentController.getUserRents("TestUser").get(0);
		assertEquals("FINISHED", rent.v7_status);
	}
}
