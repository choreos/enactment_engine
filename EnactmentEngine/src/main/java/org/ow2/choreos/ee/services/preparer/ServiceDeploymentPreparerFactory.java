package org.ow2.choreos.ee.services.preparer;

import org.ow2.choreos.services.datamodel.DeployableService;

public class ServiceDeploymentPreparerFactory {

    public static boolean testing = false;
    public static ServiceDeploymentPreparer preparerForTest;

    public static ServiceDeploymentPreparer getNewInstance(DeployableService service) {
	if (!testing)
	    return new ServiceDeploymentPreparer(service);
	else
	    return preparerForTest;
    }

}
