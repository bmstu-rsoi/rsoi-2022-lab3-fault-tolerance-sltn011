package ru.RSOI.Cars;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.RSOI.Cars.Controller.CCar;
import ru.RSOI.Cars.Error.EBadRequestError;
import ru.RSOI.Cars.Error.ENotFoundError;
import ru.RSOI.Cars.Model.MCar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
class CarsApplicationTests {
	@Autowired
	private CCar carsController;

	@Test
	void testPost()
	{
		Map<String, String> values = new HashMap<String, String>();
		values.put("brand", "Mercedes Benz");
		values.put("model", "GLA 250");
		values.put("registration_number", "ЛО777Х799");
		values.put("power", "249");
		values.put("price", "3500");
		values.put("type", "SEDAN");
		values.put("availability", "true");
		ResponseEntity<Object> response = carsController.addCar(values);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	void testGetAvailableOnly()
	{
		Iterator<MCar> it = carsController.getCars(1, 5, false).iterator();
		boolean onlyAvailable = true;
		while (it.hasNext())
		{
			if (it.next().v8_availability == false)
			{
				onlyAvailable = false;
			}
		}
		assertEquals(onlyAvailable, true);
	}

	@Test
	void testGetNonExisting()
	{
		assertThrows(EBadRequestError.class, () -> carsController.updateAvailableCar(
				"284f4ce6-04c2-4b2c-872f-971df9eb5599", true));
	}

}
