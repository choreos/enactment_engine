package org.ow2.choreos.ee.services.preparer;

import org.ow2.choreos.ee.nodes.cm.NodeNotPreparedException;
import org.ow2.choreos.ee.nodes.cm.NodePreparer;
import org.ow2.choreos.ee.nodes.cm.NodePreparers;
import org.ow2.choreos.ee.nodes.cm.NodeUpdater;
import org.ow2.choreos.ee.nodes.cm.NodeUpdaters;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.ServiceInstance;

public class InstanceUndeploymentPreparer {

    private ServiceInstance instance;
    private CloudNode node;
    private DeployableService service;

    public InstanceUndeploymentPreparer(DeployableService service, ServiceInstance serviceInstance) {
	this.service = service;
	this.instance = serviceInstance;
	this.node = instance.getNode();
    }

    public void prepareUndeployment() throws PrepareUndeploymentFailedException {
	runUndeploymentPrepare();
	scheduleHandler();
    }

    private void runUndeploymentPrepare() throws PrepareUndeploymentFailedException {
	NodePreparer nodePreparer = NodePreparers.getPreparerFor(node);
	try {
	    nodePreparer.prepareNodeForUndeployment(instance.getInstanceId());
	} catch (NodeNotPreparedException e1) {
	    throw new PrepareUndeploymentFailedException();
	}
    }

    private void scheduleHandler() {
	InstanceRemoverUpdateHandler handler = new InstanceRemoverUpdateHandler(service, instance);
	NodeUpdater nodeUpdater = NodeUpdaters.getUpdaterFor(node);
	nodeUpdater.addHandler(handler);
    }
}
