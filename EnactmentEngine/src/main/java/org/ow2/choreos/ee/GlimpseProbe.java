package org.ow2.choreos.ee;

import it.cnr.isti.labsedc.glimpse.event.GlimpseBaseEvent;
import it.cnr.isti.labsedc.glimpse.event.GlimpseBaseEventChoreos;
import it.cnr.isti.labsedc.glimpse.probe.GlimpseAbstractProbe;
import it.cnr.isti.labsedc.glimpse.utils.Manager;

import java.util.Date;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.ow2.choreos.ee.config.QoSManagementConfiguration;

public class GlimpseProbe extends GlimpseAbstractProbe {

    Logger logger = Logger.getLogger("reconfLogger");

    private static String glimpseHost = "tcp://"
            + QoSManagementConfiguration.get(QoSManagementConfiguration.RESOURCE_METRIC_AGGREGATOR) + ":61616";

    private static GlimpseProbe INSTANCE;

    public GlimpseProbe() {
        super(Manager.createProbeSettingsPropertiesObject("org.apache.activemq.jndi.ActiveMQInitialContextFactory",
                glimpseHost, "system", "manager", "TopicCF", "jms.probeTopic", false, "probeName", "probeTopic"));
    }

    public void publishDeployStatus(String metric, String chor, String service, String instance, String ip) {
        boolean isException = false;

        GlimpseBaseEventChoreos<String> event = new GlimpseBaseEventChoreos<String>(instance, new Date().getTime(),
                metric, isException, chor, service, ip);
        
        try {
            sendEventMessage(event, false);
        } catch (JMSException e) {
            logger.error("JMS comm error");
            return;
        } catch (NamingException e) {
            logger.error("Error with Jms naming");
            return;
        }
    }

    @Override
    public void sendMessage(GlimpseBaseEvent<?> message, boolean debug) {

    }

    public static GlimpseProbe getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GlimpseProbe();
        return INSTANCE;
    }
}
