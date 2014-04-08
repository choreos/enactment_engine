package org.ow2.choreos.ee.reconfiguration;

import it.cnr.isti.labse.glimpse.xml.complexEventResponse.ComplexEventResponse;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.datamodel.Choreography;

public class ComplexEventResponseHandler {

	private ComplexEventHandler handler;

	private Logger logger = Logger.getLogger("reconfLogger");
	private ComplexEventResponse response;
	private HandlingEvent event;

	public void handle(ComplexEventResponse responseFromMonitoring, Choreography chor)
			throws JMSException {
		setUp(responseFromMonitoring);
		loadHandler();
		handles(chor);
	}

	private void handles(Choreography chor) {
		logger.debug("Handling event\n\n>>>\n " + event.getRule()
				+ " on host = " + event.getNode());
		handler.handleEvent(event, chor);
		try {
			Thread.sleep(1000 * 60);
			logger.debug("Sleeping one minute");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.debug("Event handled!\n\n>>>");
	}

	private void setUp(ComplexEventResponse responseFromMonitoring)
			throws JMSException {
		response = responseFromMonitoring;
		String ruleMatched = response.getRuleName();
		String node = response.getResponseKey();
		String serviceId = response.getResponseValue();
		event = new HandlingEvent(ruleMatched, node, serviceId);
	}

	@SuppressWarnings("unchecked")
	private void loadHandler() {
		logger.debug("Loading handler...\n>>> " + "(" + event.getRule() + ", "
				+ event.getNode() + ", " + event.getServiceId() + ")");
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
