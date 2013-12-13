package org.ow2.choreos;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.ow2.choreos.log.SimpleLogger;
import org.ow2.choreos.log.SimpleLoggerImpl;

@WebService
public class AirlineService implements Airline {
	
	private static final String FLIGHT_NUMBER = "33";

    private final SimpleLogger logger = new SimpleLoggerImpl("/tmp/airline.log");

    public AirlineService() {

        logger.info("Airline started at " + AirlineStarter.SERVICE_ADDRESS);
    }

    @WebMethod
    @Override
    public String buyFlight() {
        logger.info("Request to buy flight; response: " + FLIGHT_NUMBER);
        return FLIGHT_NUMBER;
    }

}
