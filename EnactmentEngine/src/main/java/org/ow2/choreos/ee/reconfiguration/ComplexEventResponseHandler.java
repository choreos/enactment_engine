package org.ow2.choreos.ee.reconfiguration;

import it.cnr.isti.labse.glimpse.xml.complexEventResponse.ComplexEventResponse;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.ChoreographyNotFoundException;
import org.ow2.choreos.chors.datamodel.Choreography;

public class ComplexEventResponseHandler {

    private ComplexEventHandler handler;

    private Logger logger = Logger.getLogger("reconfLogger");
    private ComplexEventResponse response;
    private HandlingEvent event;

    public ComplexEventResponseHandler() {

    }

    public void handle(ComplexEventResponse responseFromMonitoring) throws JMSException, ChoreographyNotFoundException {
        setUp(responseFromMonitoring);
        loadHandler();
        handles();
    }

    private void handles() {
        logger.debug("Handling event " + event.getRule() + " for chor " + event.getChor() + "; service is "
                + event.getServiceId());
        handler.handleEvent(event);
        logger.debug("Event handled!");
    }

    private void setUp(ComplexEventResponse responseFromMonitoring) throws JMSException, ChoreographyNotFoundException {
        response = responseFromMonitoring;
        String ruleMatched = response.getRuleName();
        String serviceId = response.getResponseValue();
        String chorId = response.getResponseKey();
        
        // get chor
        Choreography chor = (new  ChoreographyRegistryHelper()).getChorClient().getChoreography(chorId);
        
        event = new HandlingEvent(ruleMatched, chor, serviceId);
    }

    @SuppressWarnings("unchecked")
    private void loadHandler() {
        logger.debug("Loading handler " + "(" + event.getRule() + ", " + event.getChor() + ", " + event.getServiceId()
                + ")");
        Class<ComplexEventHandler> clazz;
        try {
            clazz = (Class<ComplexEventHandler>) Class.forName("org.ow2.choreos.ee.reconfiguration.events."
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
