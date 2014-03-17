package org.ow2.choreos;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

public class AirlineServiceTest {

    @Before
    public void setUp() {
        AirlineStarter.start();
    }

    @Test
    public void shouldRetriveFlightTicketNumberFromAirlineService() throws MalformedURLException {

        final String EXPECTED_FLIGHT_TICKET_NUMBER_BEGINNING = "33";

        String wsdl = AirlineStarter.SERVICE_ADDRESS + "?wsdl";
        AirlineClientFactory factory = new AirlineClientFactory(wsdl);
        Airline client = factory.getClient();
        
        String flightTicketNumber = client.buyFlight();

        System.out.println("flightTicketNumber: " + flightTicketNumber);
        assertTrue(flightTicketNumber.startsWith(EXPECTED_FLIGHT_TICKET_NUMBER_BEGINNING));
    }
}
