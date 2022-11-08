package ru.RSOI.Rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalApplication.class, args);

		/*
		String date = new String("2022-10-20T21:00:00.000+00:00");
		date = date.substring(0, date.indexOf('T'));
		Date d = Date.valueOf(date);
		Timestamp ts = new Timestamp(d.getTime());
		System.out.println(ts);
		 */
	}

}
