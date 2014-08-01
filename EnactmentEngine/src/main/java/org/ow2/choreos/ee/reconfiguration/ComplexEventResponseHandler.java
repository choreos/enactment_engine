package org.ow2.choreos.ee.reconfiguration;

import it.cnr.isti.labse.glimpse.xml.complexEventResponse.ComplexEventResponse;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.ow2.choreos.ee.ChoreographyContext;
import org.ow2.choreos.ee.GlimpseProbe;

public class ComplexEventResponseHandler {

    private ComplexEventHandler handler;

    private Logger logger = Logger.getLogger("reconfLogger");
    private ComplexEventResponse response;
    private HandlingEvent event;
    private ChoreographyContext choreographyContext;

    public ComplexEventResponseHandler(ChoreographyContext choreographyContext) {
        this.choreographyContext = choreographyContext;
    }

    public void handle(ComplexEventResponse responseFromMonitoring) throws JMSException {
        setUp(responseFromMonitoring);
        loadHandler();
        handles();
        GlimpseProbe.getInstance().sendUpdateEvent("", "finished_update", choreographyContext.getChoreography().getId(), response.getResponseValue(), "all");
    }

    private void handles() {
        logger.debug("Handling event " + event.getRule() + " for chor " + event.getChor() + "; service is "
                + event.getServiceId());
        handler.handleEvent(event);
        logger.debug("Event handled!");
    }

    private void setUp(ComplexEventResponse responseFromMonitoring) throws JMSException {
        response = responseFromMonitoring;
        String ruleMatched = response.getRuleName();
        String serviceId = response.getResponseValue();
        
        //GlimpseProbe.getInstance().sendUpdateEvent("", "updating", choreographyContext.getChoreography().getId(), serviceId, "all");
        
        event = new HandlingEvent(ruleMatched, choreographyContext.getChoreography(), serviceId);
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
