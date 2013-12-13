package org.ow2.choreos;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jws.WebService;

import org.ow2.choreos.log.SimpleLogger;
import org.ow2.choreos.log.SimpleLoggerImpl;

@WebService(targetNamespace = "http://choreos.ow2.org/", endpointInterface = "org.ow2.choreos.TravelAgency")
public class TravelAgencyService implements TravelAgency {

	public static final String HOTEL_RESERVATION_NUMBER = "22";
	private static final String AIRLINE_ERROR_MESSAGE = "Not possible to buy now: Airline service is not working";
	private static final String AIRLINE_NOT_SET_ERROR_MESSAGE = "Not possible to buy now: I don't know the Airline service yet";

	private static List<String> airlineEndpoints = new ArrayList<String>();
	// airlineClients: key is the service wsdl
	private static Map<String, Airline> airlineClients = new HashMap<String, Airline>();

	private final SimpleLogger logger = new SimpleLoggerImpl(
			"/tmp/travel_agency.log");

	private AtomicInteger counter = new AtomicInteger();

	public TravelAgencyService() {
		logger.info("Travel Agency started at "
				+ TravelAgencyStarter.SERVICE_ADDRESS);
	}

	@Override
	public synchronized void setInvocationAddress(String role, String name,
			List<String> endpoints) {
		logger.info("setting inv. addrr to " + endpoints);
		if (role.equals("airline")) {
			airlineEndpoints.clear();
			for (String endpoint : endpoints) {
				try {
					generateClient(endpoint);
				} catch (MalformedURLException e) {
					logger.error("Invalid airline endpoint URL: " + endpoint);
				}
			}
		} else {
			logger.warn("Invalid role (" + role + ") in setInvocationAddress");
		}
	}

	private void generateClient(String endpoint) throws MalformedURLException {
		airlineEndpoints.add(endpoint);
		String wsdl = endpoint + "?wsdl";
		AirlineClientFactory factory = new AirlineClientFactory(wsdl);
		Airline airlineClient = factory.getClient();
		airlineClients.put(endpoint, airlineClient);
	}

	@Override
	public String buyTrip() {

		if (airlineEndpoints.size() == 0) {
			logger.error(AIRLINE_NOT_SET_ERROR_MESSAGE);
			return AIRLINE_NOT_SET_ERROR_MESSAGE;
		}

		Airline airlineClient = getAirlineClientInRoundRobin();
		String flightTicketNumber = null;
		try {
			flightTicketNumber = airlineClient.buyFlight(); // "33"
		} catch (Exception e) {
			logger.error(AIRLINE_ERROR_MESSAGE);
			return AIRLINE_ERROR_MESSAGE;
		}

		String result = flightTicketNumber + "--" + HOTEL_RESERVATION_NUMBER;
		logger.info("request to buy trip; response: " + result);
		return result;
	}

	private Airline getAirlineClientInRoundRobin() {
		int index = counter.getAndIncrement();
		index %= airlineEndpoints.size();
		String airlineEndpoint = airlineEndpoints.get(index).toString();
		Airline airlineClient = airlineClients.get(airlineEndpoint);
		logger.debug("Using airline index " + index + ", URL: "
				+ airlineEndpoint);
		return airlineClient;
	}

}
