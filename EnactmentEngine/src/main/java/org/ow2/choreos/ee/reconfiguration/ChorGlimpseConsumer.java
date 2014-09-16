package org.ow2.choreos.ee.reconfiguration;

/*
 * 972349511 bitten
 */

import it.cnr.isti.labse.glimpse.xml.complexEventException.ComplexEventException;
import it.cnr.isti.labse.glimpse.xml.complexEventResponse.ComplexEventResponse;
import it.cnr.isti.labsedc.glimpse.consumer.GlimpseAbstractConsumer;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.ChoreographyNotFoundException;

class ChorGlimpseConsumer extends GlimpseAbstractConsumer {

    Logger logger = Logger.getLogger("reconfLogger");

    private ComplexEventExceptionHandler complexEventExceptionHandler;
    private ComplexEventResponseHandler complexEventResponseHandler;

    public ChorGlimpseConsumer(Properties settings, String plainTextRule) {
        super(settings, plainTextRule);
        complexEventExceptionHandler = new ComplexEventExceptionHandler();
        complexEventResponseHandler = new ComplexEventResponseHandler();
    }

    @Override
    public void messageReceived(Message arg0) throws JMSException {
        try {
            ObjectMessage responseFromMonitoring = (ObjectMessage) arg0;
            if (responseFromMonitoring.getObject() instanceof ComplexEventException) {
                complexEventExceptionHandler.handle(responseFromMonitoring);
            } else {
                ComplexEventResponse respObject = (ComplexEventResponse) responseFromMonitoring.getObject();
                if (isQoSComplexEvent(respObject))
                    complexEventResponseHandler.handle(respObject);
            }
        } catch (ClassCastException asd) {
            logger.error("Error while casting message received. It is not a ObjectMessage instance");
            logger.error("Message is : " + arg0);
            logger.fatal(asd);
        } catch (ChoreographyNotFoundException e) {
            logger.error("Choreography not found");
        }
    }

    private boolean isQoSComplexEvent(ComplexEventResponse respObject) {
        boolean result = false;
        String ruleName = respObject.getRuleName();

        logger.debug("isQoSComplexEvent() : Rule name = " + ruleName);

        if (ruleName.startsWith("RemoveReplica")) {
            result = true;
            respObject.setRuleName("RemoveReplica");
        }

        else if (ruleName.startsWith("AddReplica")) {
            result = true;
            respObject.setRuleName("AddReplica");
        }
        
        else if (ruleName.startsWith("MigrateUp")) {
            result = true;
            respObject.setRuleName("MigrateUp");
        }
        
        else if (ruleName.startsWith("MigrateDown")) {
            result = true;
            respObject.setRuleName("MigrateDown");
        }

        logger.debug("isQoSComplexEvent() : return value = " + result);

        return result;
    }
}