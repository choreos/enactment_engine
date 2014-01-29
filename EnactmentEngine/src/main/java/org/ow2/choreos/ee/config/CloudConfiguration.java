/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.ee.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.choreos.utils.Configuration;

/**
 * Encapsulates the reading of the file clouds.properties,
 * retrieving information about cloud accounts configure in this EE instance.
 * A cloud account contains credentials to access some infrastructure provider service.
 * 
 * @author leonardo
 * 
 */
public class CloudConfiguration {

    private static Logger logger = Logger.getLogger(CloudConfiguration.class);

    public static final String DEFAULT = EEConfiguration.get("DEFAULT_CLOUD_ACCOUNT");

    private final String cloudAccount;

    private static String PROPERTIES_FILE = "clouds.properties";

    private static Map<String, CloudConfiguration> INSTANCES = new HashMap<String, CloudConfiguration>();

    private final Configuration properties = new Configuration(PROPERTIES_FILE);

    private Configuration getProperties() {
	return properties;
    }

    public static CloudConfiguration getCloudConfigurationInstance(String cloudAccount) {
	if (!INSTANCES.containsKey(cloudAccount))
	    INSTANCES.put(cloudAccount, new CloudConfiguration(cloudAccount));
	return INSTANCES.get(cloudAccount);
    }

    public static CloudConfiguration getCloudConfigurationInstance() {
	return getCloudConfigurationInstance(DEFAULT);
    }

    private CloudConfiguration(String cloudAccount) {
	this.cloudAccount = cloudAccount;
    }

    /**
     * If the value is not found, the DEFAULT value is returned. If both the
     * value of key and DEFAULT are not found, an IllegalArgumentException is
     * thrown.
     * 
     * @param key
     * @return
     * @throws IllegalArgumentException
     */
    public String get(String key) {

	key = keyForCloudAccount(key);

	String value = getProperties().get(key);

	if (value == null || value.trim().isEmpty()) {
	    logger.error("Could not retrieve the CloudConfiguration property " + key + " for cloud account " + cloudAccount
		    + ". Please, check the file " + PROPERTIES_FILE);
	    throw new IllegalArgumentException();
	}

	return value.trim();
    }

    public String[] getMultiple(String key) {
	return getProperties().getMultiple(keyForCloudAccount(key));
    }

    public void set(String key, String value) {
	if (key != null) {
	    key = keyForCloudAccount(key);
	    getProperties().set(key, value);
	}
    }

    private String keyForCloudAccount(String key) {
	return cloudAccount + "." + key;
    }

    public String getCloudAccount() {
	return cloudAccount;
    }
}
