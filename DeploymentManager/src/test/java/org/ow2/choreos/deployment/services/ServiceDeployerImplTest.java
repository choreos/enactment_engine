/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.deployment.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.ow2.choreos.deployment.DeploymentManagerConfiguration;
import org.ow2.choreos.deployment.services.preparer.PrepareDeploymentFailedException;
import org.ow2.choreos.deployment.services.preparer.ServiceDeploymentPreparer;
import org.ow2.choreos.deployment.services.preparer.ServiceDeploymentPreparerFactory;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.services.ServiceNotCreatedException;
import org.ow2.choreos.services.ServicesManager;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.utils.LogConfigurator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceDeployerImplTest {

    private ServicesManager servicesManager;
    private DeployableServiceSpec serviceSpec;
    private CloudNode node;

    @Before
    public void setUp() throws PrepareDeploymentFailedException {
        LogConfigurator.configLog();
        DeploymentManagerConfiguration.set("TESTING", "true");
        setNode();
        setUpServiceDeployer();
    }

    private void setNode() {
        node = new CloudNode();
        node.setId("1");
        node.setIp("192.168.56.101");
        node.setUser("ubuntu");
        node.setPrivateKey("key.pem");
    }
    
    private void setUpServiceDeployer() throws PrepareDeploymentFailedException {
        serviceSpec = new DeployableServiceSpec();
        serviceSpec.setPackageUri("http://choreos.eu/services/airline.jar");
        serviceSpec.setPackageType(PackageType.COMMAND_LINE);
        serviceSpec.setEndpointName("airline");
        serviceSpec.setPort(8042);
        servicesManager = new ServicesManagerImpl();
        
        ServiceDeploymentPreparer preparerMock = mock(ServiceDeploymentPreparer.class);
        when(preparerMock.prepareDeployment()).thenReturn(Collections.singleton(node));
        ServiceDeploymentPreparerFactory.preparerForTest = preparerMock;
        ServiceDeploymentPreparerFactory.testing = true;
    }

    @Test
    public void shouldReturnAValidService() throws PrepareDeploymentFailedException, ServiceNotCreatedException {
        DeployableService service = servicesManager.createService(serviceSpec);
        assertEquals(serviceSpec.getName(), service.getSpec().getName());
        String uuid = service.getUUID();
        assertTrue(uuid != null && !uuid.isEmpty());
        assertEquals(service.getSelectedNodes().iterator().next(), node);
        ServiceDeploymentPreparerFactory.testing = false;
    }

}
