package org.ow2.choreos;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TravelAgencyServiceTest {

	@Before
	public void setUp() {
		AirlineStarter.start();
		TravelAgencyStarter.start();
	}

	@Test
	public void shouldBuyTrip() throws MalformedURLException {

		final String EXPECTED_BEGINNIG = AirlineService.FLIGHT_NUMBER;
		final String EXPECTED_END = TravelAgencyService.HOTEL_RESERVATION_NUMBER;

		String wsdl = TravelAgencyStarter.SERVICE_ADDRESS + "?wsdl";
		TravelAgencyClientFactory factory = new TravelAgencyClientFactory(wsdl);
		TravelAgency client = factory.getClient();

		List<String> airlineEndpoints = Collections
				.singletonList(AirlineStarter.SERVICE_ADDRESS);
		client.setInvocationAddress("airline", "airline", airlineEndpoints);
		String result = client.buyTrip();

		assertTrue(result.startsWith(EXPECTED_BEGINNIG));
		assertTrue(result.endsWith(EXPECTED_END));
	}
}
