package org.ow2.choreos;

import javax.jws.WebService;

import org.ow2.choreos.log.SimpleLogger;
import org.ow2.choreos.log.SimpleLoggerImpl;

@WebService(targetNamespace = "http://choreos.ow2.org/", endpointInterface = "org.ow2.choreos.Airline")
public class AirlineService implements Airline {

    private static final String FLIGHT_NUMBER = "33";

    private final SimpleLogger logger = new SimpleLoggerImpl("/tmp/airline.log");

    public AirlineService() {

        logger.info("Airline started at " + AirlineStarter.SERVICE_ADDRESS);
    }

    @Override
    public String buyFlight() {
        logger.info("Request to buy flight; response: " + FLIGHT_NUMBER);
        return FLIGHT_NUMBER;
    }

}
