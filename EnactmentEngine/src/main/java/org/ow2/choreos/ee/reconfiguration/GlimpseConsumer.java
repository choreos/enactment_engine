package org.ow2.choreos.ee.reconfiguration;

import it.cnr.isti.labsedc.glimpse.utils.Manager;

import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ow2.choreos.ee.config.QoSManagementConfiguration;

public class GlimpseConsumer {

	private static final String namingURL = "tcp://"
			+ QoSManagementConfiguration
					.get(QoSManagementConfiguration.RESOURCE_METRIC_AGGREGATOR)
			+ ":61616";

	String rules;
	Properties properties;

	static Logger logger = Logger.getLogger("reconfLogger");

	public GlimpseConsumer() {
		
		this.properties = getConsumerProperties();
		this.rules = getConsumerRules();
	}

	private Properties getConsumerProperties() {
		return Manager.createConsumerSettingsPropertiesObject(
				"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
				namingURL, "system", "manager", "TopicCF", "jms.serviceTopic",
				false, "eeConsumer"+ UUID.randomUUID().toString().replace("-", ""));
	}

	private String getConsumerRules() {
		String fileContent = null;

		fileContent = new GlimpseRulesBuilder().assemblyGlimpseRules();
		
		logger.debug(fileContent);

		return fileContent;
	}

	public void run() {

		new ChorGlimpseConsumer(this.properties, this.rules);
		logger.info("Glimpse consumer stated!");
	}	
	
	public static void main(String[] args) {
		new GlimpseConsumer().run();
	}


}
