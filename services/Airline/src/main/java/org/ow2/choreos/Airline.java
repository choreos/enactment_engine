package org.ow2.choreos;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface Airline {

    /**
     * 
     * @return the flight ticket number
     */
    @WebMethod
    public String buyFlight();

}
