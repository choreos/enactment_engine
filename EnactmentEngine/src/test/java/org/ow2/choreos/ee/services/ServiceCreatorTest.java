/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.ee.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ow2.choreos.ee.config.EEConfiguration;
import org.ow2.choreos.ee.services.preparer.PrepareDeploymentFailedException;
import org.ow2.choreos.services.ServiceNotCreatedException;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.utils.LogConfigurator;

public class ServiceCreatorTest {

    private ServiceCreator serviceCreator;
    private DeployableServiceSpec serviceSpec;

    @Before
    public void setUp() throws PrepareDeploymentFailedException {
	LogConfigurator.configLog();
	EEConfiguration.set("TESTING", "true");
	setUpServiceDeployer();
    }

    private void setUpServiceDeployer() throws PrepareDeploymentFailedException {
	serviceSpec = new DeployableServiceSpec();
	serviceSpec.setPackageUri("http://choreos.eu/services/airline.jar");
	serviceSpec.setPackageType(PackageType.COMMAND_LINE);
	serviceSpec.setEndpointName("airline");
	serviceSpec.setPort(8042);
	serviceCreator = new ServiceCreator();
    }

    @Test
    public void shouldReturnAValidService() throws PrepareDeploymentFailedException, ServiceNotCreatedException {
	DeployableService service = serviceCreator.createService(serviceSpec);
	assertEquals(serviceSpec.getName(), service.getSpec().getName());
	String uuid = service.getUUID();
	assertTrue(uuid != null && !uuid.isEmpty());
    }

}
