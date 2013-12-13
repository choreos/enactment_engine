package org.ow2.choreos;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface TravelAgency {

	/**
	 * 
	 * @return 'xx-yy', where xx is the flight ticket number, 
	 * and yy is the hotel reservation number
	 */
	@WebMethod
	public String buyTrip();
	
	@WebMethod
	public void setInvocationAddress(String role, String name, List<String> endpoint);
}
