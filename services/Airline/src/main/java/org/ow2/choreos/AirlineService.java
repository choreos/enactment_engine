package org.ow2.choreos;

import javax.jws.WebService;

import org.apache.log4j.Logger;

@WebService(targetNamespace = "http://choreos.ow2.org/", endpointInterface = "org.ow2.choreos.Airline")
public class AirlineService implements Airline {

    public static final String FLIGHT_NUMBER = "33";

    private static Logger logger = Logger.getLogger(AirlineService.class);

    static {
        LogConfigurator.configLog();
    }
    
    public AirlineService() {
        logger.info("Airline started at " + AirlineStarter.SERVICE_ADDRESS);
    }

    @Override
    public String buyFlight() {
        logger.info("Request to buy flight; response: " + FLIGHT_NUMBER);
        return FLIGHT_NUMBER;
    }

}
