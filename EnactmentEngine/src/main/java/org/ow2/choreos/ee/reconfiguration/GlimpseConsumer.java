package org.ow2.choreos.ee.reconfiguration;

import it.cnr.isti.labsedc.glimpse.utils.Manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.ow2.choreos.ee.ChoreographyContext;
import org.ow2.choreos.ee.ChoreographyContext.ConsumerShoutdownListener;
import org.ow2.choreos.ee.config.QoSManagementConfiguration;

public class GlimpseConsumer implements Runnable {

    private static Logger logger = Logger.getLogger("reconfLogger");

    private static final String namingURL = "tcp://"
            + QoSManagementConfiguration.get(QoSManagementConfiguration.RESOURCE_METRIC_AGGREGATOR) + ":61616";

    private String consumerName;
    private String rules;
    private Properties properties;
    private ChoreographyContext choreographyContext;
    private boolean running = false;
    private GlimpseRulesBuilder glimpseRulesBuilder;
    private ExecutorService e;

    private GlimpseConsumerLoader loader;

    public GlimpseConsumer(ChoreographyContext choreographyContext) {
        initialize(choreographyContext);
    }

    private void initialize(ChoreographyContext choreographyContext) {
        this.choreographyContext = choreographyContext;
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

        fileContent = glimpseRulesBuilder.assemblyGlimpseRules(this.choreographyContext.getChoreography());

        return fileContent;
    }

    public void start() {
        e.submit(this);
        while(running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void stop(ConsumerShoutdownListener consumerShoutdownListener) {
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
        
        if (!(consumerShoutdownListener == null)) {
            try {
                consumerShoutdownListener.onShutdown();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        
    }

    @Override
    public void run() {
        logger.info("Starting running glimpse consumer " + consumerName);

        loader = new GlimpseConsumerLoader(getClass().getClassLoader());
        try {
            Class<?> clazz = Class.forName(ChorGlimpseConsumer.class.getName(), true, loader);
            Class<?> superClazz = clazz.getSuperclass();

            // makes it accessible
            Field firstMessageField = superClazz.getDeclaredField("firstMessage");       
            firstMessageField.setAccessible(true);
            firstMessageField.set(null, true);
            
            Constructor<?> constructor = clazz.getDeclaredConstructor(Properties.class, String.class, ChoreographyContext.class);
            constructor.setAccessible(true);
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + clazz);
            ChorGlimpseConsumer cons = (ChorGlimpseConsumer) constructor.newInstance(properties, rules, choreographyContext);
  
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            throw new RuntimeException("Not possible to load class");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

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
}
