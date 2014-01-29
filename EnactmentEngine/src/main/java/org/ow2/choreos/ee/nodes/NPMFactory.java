package org.ow2.choreos.ee.nodes;

import org.ow2.choreos.ee.config.CloudConfiguration;
import org.ow2.choreos.nodes.NodePoolManager;

public class NPMFactory {

    public static NodePoolManager npmForTest;
    public static boolean testing = false;

    public static NodePoolManager getNewNPMInstance(String cloudAccount) {
	if (testing) {
	    return npmForTest;
	} else {
	    CloudConfiguration cloudConfiguration = getCloudConfiguration(cloudAccount);
	    return new NPMImpl(cloudConfiguration);
	}
    }

    private static CloudConfiguration getCloudConfiguration(String cloudAccount) {
	CloudConfiguration cloudConfiguration = null;
	if (cloudAccount == null || cloudAccount.isEmpty()) {
	    cloudConfiguration = CloudConfiguration.getCloudConfigurationInstance();
	} else {
	    cloudConfiguration = CloudConfiguration.getCloudConfigurationInstance(cloudAccount);
	}
	return cloudConfiguration;
    }

    public static NodePoolManager getNewNPMInstance(String cloudAccount, Reservoir idlePool) {
	if (testing) {
	    return npmForTest;
	} else {
	    CloudConfiguration cloudConfiguration = getCloudConfiguration(cloudAccount);
	    return new NPMImpl(cloudConfiguration, idlePool);
	}
    }
}
