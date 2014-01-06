package org.ow2.choreos.ee.nodes;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.choreos.ee.config.CloudConfiguration;
import org.ow2.choreos.ee.config.EEConfiguration;

public class ReservoirFactory {

    private static final int DEFAULT_POOL_SIZE = 0;
    private static final int DEFAULT_POOL_THRESHOLD = -1;

    public static Reservoir reservoirForTesting;
    public static boolean testing;

    private static Map<String, Reservoir> INSTANCES = new HashMap<String, Reservoir>();
    private static Logger logger = Logger.getLogger(ReservoirFactory.class);

    /**
     * Thread safe
     * 
     * @param poolSize
     * @param nodeCreator
     * @return
     */
    public static Reservoir getInstance(CloudConfiguration cloudConfiguration, int poolSize, int threshold) {
	String cloudAccount = cloudConfiguration.getOwner();
	synchronized (ReservoirFactory.class) {
	    if (!INSTANCES.containsKey(cloudAccount)) {
		INSTANCES.put(cloudAccount, new Reservoir(cloudConfiguration, poolSize, threshold));
	    }
	}
	return INSTANCES.get(cloudAccount);
    }

    /**
     * Not thread safe. To test purposes
     * 
     * @param poolSize
     * @param nodeCreator
     * @return
     */
    public static Reservoir getCleanInstance(CloudConfiguration cloudConfiguration, int poolSize, int threshold) {
	return new Reservoir(cloudConfiguration, poolSize, threshold);
    }

    public Reservoir getReservoir(CloudConfiguration cloudConfiguration) {
	if (testing) {
	    return reservoirForTesting;
	} else {
	    return loadReservoir(cloudConfiguration);
	}
    }

    private Reservoir loadReservoir(CloudConfiguration cloudConfiguration) {
	int reservoirSize = getValue("RESERVOIR_INITIAL_SIZE", DEFAULT_POOL_SIZE);
	int threshold = getValue("RESERVOIR_THRESHOLD", DEFAULT_POOL_THRESHOLD);
	return getInstance(cloudConfiguration, reservoirSize, threshold);
    }

    private int getValue(String property, int defaultValue) {
	int value = defaultValue;
	try {
	    value = Integer.parseInt(EEConfiguration.get(property));
	} catch (NumberFormatException e) {
	    logger.warn(property + " not integer. Going to use default " + defaultValue);
	} catch (IllegalArgumentException e) {
	    logger.warn(property + " not found. Going to use default " + defaultValue);
	}
	return value;
    }

}
