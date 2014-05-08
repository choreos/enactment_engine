package org.ow2.choreos.ee.reconfiguration;

import it.cnr.isti.labse.glimpse.xml.complexEventResponse.ComplexEventResponse;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

public class ComplexEventResponseHandler {

	private ComplexEventHandler handler;

	private Logger logger = Logger.getLogger("reconfLogger");
	private ComplexEventResponse response;
	private HandlingEvent event;

	public void handle(ComplexEventResponse responseFromMonitoring)
			throws JMSException {
		setUp(responseFromMonitoring);
		loadHandler();
		handles();
	}

	private void handles() {
		logger.debug("Handling event " + event.getRule()
				+ " on host = " + event.getChorId());
		handler.handleEvent(event);
		logger.debug("Event handled!");
	}

	private void setUp(ComplexEventResponse responseFromMonitoring)
			throws JMSException {
		response = responseFromMonitoring;
		String ruleMatched = response.getRuleName();
		String chorId = response.getResponseKey();
		String serviceId = response.getResponseValue();
		event = new HandlingEvent(ruleMatched, chorId, serviceId);
	}

	@SuppressWarnings("unchecked")
	private void loadHandler() {
		logger.debug("Loading handler " + "(" + event.getRule() + ", "
				+ event.getChorId() + ", " + event.getServiceId() + ")");
		Class<ComplexEventHandler> clazz;
		try {
			clazz = (Class<ComplexEventHandler>) Class
					.forName("org.ow2.choreos.ee.reconfiguration.events."
							+ event.getRule());
			handler = clazz.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}
