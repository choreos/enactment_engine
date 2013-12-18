package org.ow2.choreos.ee;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.ee.nodes.cm.NodeUpdater;
import org.ow2.choreos.ee.nodes.cm.NodeUpdaters;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.tests.ModelsForTest;


public class NodesUpdaterTest {

    private Choreography chor;

    @Before
    public void setUp() {
        ModelsForTest models = new ModelsForTest(ServiceType.SOAP, PackageType.COMMAND_LINE);
        chor = models.getChoreography();
        models = new ModelsForTest(ServiceType.SOAP, PackageType.COMMAND_LINE, 3);
    }

    @Test
    public void shouldUpdateAllNodes() throws Exception {

        NodeUpdater updaterMock = mock(NodeUpdater.class);
        NodeUpdaters.updaterForTest = updaterMock;
        NodeUpdaters.testing = true;

        List<DeployableService> services = chor.getDeployableServices();
        NodesUpdater updater = new NodesUpdater(services, chor.getId());
        updater.updateNodes();

        int nodesAmount = 0;
        for (DeployableService svc: services) {
            nodesAmount += svc.getSelectedNodes().size();
        }
        verify(updaterMock, times(nodesAmount)).update();
        
        NodeUpdaters.testing = false;
    }

}
