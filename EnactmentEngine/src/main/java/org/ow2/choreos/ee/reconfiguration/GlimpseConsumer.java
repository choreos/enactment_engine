package org.ow2.choreos.ee.reconfiguration;

import it.cnr.isti.labsedc.glimpse.utils.Manager;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.ow2.choreos.ee.config.QoSManagementConfiguration;

public class GlimpseConsumer implements Runnable {

    private static Logger logger = Logger.getLogger("reconfLogger");

    private static final String namingURL = "tcp://"
            + QoSManagementConfiguration.get(QoSManagementConfiguration.RESOURCE_METRIC_AGGREGATOR) + ":61616";

    private String consumerName;
    private String rules;
    private Properties properties;
    private boolean running = false;
    private GlimpseRulesBuilder glimpseRulesBuilder;
    private ExecutorService e;

    public GlimpseConsumer() {
        initialize();
    }

    private void initialize() {
        this.consumerName = "consumer_" + UUID.randomUUID();

        e = Executors.newFixedThreadPool(1, new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });

        this.glimpseRulesBuilder = new GlimpseRulesBuilder(this.consumerName);
        this.properties = getConsumerProperties();
        this.rules = getConsumerRules();
    }

    private Properties getConsumerProperties() {
        Properties consumerProperties = Manager.createConsumerSettingsPropertiesObject(
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory", namingURL, "system", "manager", "TopicCF",
                "jms.serviceTopic", true, consumerName);
        return consumerProperties;
    }

    private String getConsumerRules() {
        String fileContent = null;

        fileContent = glimpseRulesBuilder.assemblyGlimpseRules();

        return fileContent;
    }

    public void start() {
        running = true;
        e.submit(this);
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void stop() {
        // TODO: should delete unused rules
        running = false;
        e.shutdown();
        try {
            if (!e.awaitTermination(60, TimeUnit.SECONDS)) {
                e.shutdownNow();
                if (!e.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            e.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        logger.info("Starting running glimpse consumer " + consumerName);
        new ChorGlimpseConsumer(properties, rules);
        logger.info("Glimpse consumer " + consumerName + " started!");
        running = true;

        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Glimpse consumer " + consumerName + " stoped!");
    }
    
    public static void main(String[] args) {
        new GlimpseConsumer().start();
    }
}
