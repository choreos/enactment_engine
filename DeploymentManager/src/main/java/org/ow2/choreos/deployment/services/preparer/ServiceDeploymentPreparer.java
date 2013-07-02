package org.ow2.choreos.deployment.services.preparer;

import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.choreos.deployment.nodes.selector.NodeSelector;
import org.ow2.choreos.deployment.nodes.selector.NodeSelectorFactory;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.selectors.NotSelectedException;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class ServiceDeploymentPreparer {

    private DeployableServiceSpec spec;
    private String serviceUUID;
    private String serviceSpecName;
    private List<CloudNode> nodes;

    private Logger logger = Logger.getLogger(ServiceDeploymentPreparer.class);

    public ServiceDeploymentPreparer(DeployableServiceSpec spec, String serviceUUID) {
        this.spec = spec;
        this.serviceUUID = serviceUUID;
        this.serviceSpecName = spec.getName();
    }

    public List<CloudNode> prepareDeployment() throws PrepareDeploymentFailedException {
        selectNodes();
        prepareInstances();
        return nodes;
    }

    private void selectNodes() throws PrepareDeploymentFailedException {
        NodeSelector selector = NodeSelectorFactory.getFactoryInstance().getNodeSelectorInstance();
        try {
            nodes = selector.select(spec, spec.getNumberOfInstances());
            logger.info("Selected nodes to " + serviceSpecName + ": " + nodes);
        } catch (NotSelectedException e) {
            failDeployment();
        }
        if (nodes == null || nodes.isEmpty()) {
            failDeployment();
        }
    }

    private void failDeployment() throws PrepareDeploymentFailedException {
        throw new PrepareDeploymentFailedException(serviceSpecName);
    }

    private void prepareInstances() {
        for (CloudNode node : nodes) {
            try {
                InstanceDeploymentPreparer instanceDeploymentPreparer = new InstanceDeploymentPreparer(spec, serviceUUID, node);
                instanceDeploymentPreparer.prepareDeployment();
            } catch (PrepareDeploymentFailedException e) {
                logger.error(e.getMessage());
            }
        }
    }

}