package br.usp.ime.tomcat.interceptors;

import it.cnr.isti.labsedc.glimpse.event.GlimpseBaseEvent;
import it.cnr.isti.labsedc.glimpse.event.GlimpseBaseEventChoreos;
import it.cnr.isti.labsedc.glimpse.probe.GlimpseAbstractProbe;
import it.cnr.isti.labsedc.glimpse.utils.Manager;

import java.util.Date;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class GlimpseProbe extends GlimpseAbstractProbe {

    private static Log log = LogFactory.getLog(GlimpseProbe.class);

    private static String glimpseHost = "tcp://81.200.35.154:61616";

    private static GlimpseProbe INSTANCE;

    // public GlimpseProbe() {
    // super(Manager.createProbeSettingsPropertiesObject("org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    // glimpseHost, "system", "manager", "TopicCF", "jms.probeTopic", false,
    // "probeName", "probeTopic"));
    // }

    public GlimpseProbe(String gh) {
	super(Manager.createProbeSettingsPropertiesObject("org.apache.activemq.jndi.ActiveMQInitialContextFactory", gh,
		"system", "manager", "TopicCF", "jms.probeTopic", false, "probeName", "probeTopic"));
    }

    public void reportQoSMetric(String metricName, String metricValue, String service, String ip) {

	boolean isException = false;
	GlimpseBaseEventChoreos<String> event = new GlimpseBaseEventChoreos<String>(metricValue, new Date().getTime(),
		metricName, isException, "", service, ip);
	try {
	    sendEventMessage(event, false);
	} catch (JMSException e) {
	    log.error("JMS comm error");
	    return;
	} catch (NamingException e) {
	    log.error("Error with Jms naming");
	    return;
	}
    }

    public void reportQoSMetric(String metricName, String metricValue, String service) {
	this.reportQoSMetric(metricName, metricValue, service, "");
    }

    @Override
    public void sendMessage(GlimpseBaseEvent<?> message, boolean debug) {

    }

    public static GlimpseProbe getInstance(String string) {
	if (INSTANCE == null)
	    INSTANCE = new GlimpseProbe(string);
	return INSTANCE;
    }
}
