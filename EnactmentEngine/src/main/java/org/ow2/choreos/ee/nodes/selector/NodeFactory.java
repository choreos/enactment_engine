package org.ow2.choreos.ee.nodes.selector;

import org.ow2.choreos.nodes.NodeNotCreatedException;
import org.ow2.choreos.nodes.NodePoolManager;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.nodes.datamodel.NodeSpec;
import org.ow2.choreos.selectors.ObjectCreationException;
import org.ow2.choreos.selectors.ObjectFactory;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;
import org.ow2.choreos.utils.TimeoutsAndTrials;

class NodeFactory implements ObjectFactory<CloudNode, DeployableServiceSpec> {

    private NodePoolManager npm;

    public NodeFactory(NodePoolManager npm) {
        this.npm = npm;
    }

    @Override
    public CloudNode createNewInstance(DeployableServiceSpec spec) throws ObjectCreationException {
        try {
            NodeSpec nodeSpec = new NodeSpec();
            nodeSpec.setResourceImpact(spec.getResourceImpact());
            return this.npm.createNode(nodeSpec);
        } catch (NodeNotCreatedException e) {
            throw new ObjectCreationException();
        }
    }

    @Override
    public int getTimeoutInSeconds() {
        int nodeCreationTimeout = TimeoutsAndTrials.getTimeout("NODE_CREATION");
        int nodeCreationTrials = TimeoutsAndTrials.getTrials("NODE_CREATION");
        int firstSshTimeout = TimeoutsAndTrials.getTimeout("FIRST_CONNECT_SSH");
        int bootstrapTimeout = TimeoutsAndTrials.getTimeout("BOOTSTRAP");
        int bootstrapTrials = TimeoutsAndTrials.getTrials("BOOTSTRAP");
        int oneReqPerSec = 2 * 100;
        int timeout = nodeCreationTimeout * nodeCreationTrials + firstSshTimeout + bootstrapTimeout * bootstrapTrials
                + oneReqPerSec;
        timeout += timeout * 0.3;
        return timeout;
    }

}
