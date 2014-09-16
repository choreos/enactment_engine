/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.ee.nodes.cm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.ee.config.EEConfiguration;
import org.ow2.choreos.invoker.InvokerConfiguration;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.utils.Concurrency;

public class NodesUpdater {

    private static final String TASK_NAME = "NODE_UPDATE";

    private List<DeployableService> services;
    private String chorId;
    private Set<CloudNode> nodesToUpdate;
    private ExecutorService executor;

    private int totalTimeout;

    private Logger logger = Logger.getLogger(NodesUpdater.class);

    public NodesUpdater(List<DeployableService> services, String chorId) {
        this.services = services;
        this.chorId = chorId;
        this.totalTimeout = InvokerConfiguration.getTotalTimeout(TASK_NAME);
        this.totalTimeout += totalTimeout * 0.1;
    }

    public void updateNodes(boolean isDecrease) throws DeploymentException {
        logger.info("Going to update nodes of choreography " + chorId);
        setNodesToUpdate(isDecrease);
        submitUpdates();
        waitUpdates();
    }

    private void setNodesToUpdate(boolean isDecrease) {
        nodesToUpdate = new HashSet<CloudNode>();

        if (isDecrease || Boolean.parseBoolean(EEConfiguration.get("IDEMPOTENCY_GUARANTEE"))) {
            for (DeployableService deployable : services) {
                for (CloudNode node : deployable.getSelectedNodes()) {
                    nodesToUpdate.add(node);
                }
            }
        } else {
            for (DeployableService deployable : services) {
                // removing replica
                if (deployable.getServiceInstances() != null)
                    logger.debug("# of Nodes vs. Instances: nodes = " + deployable.getSelectedNodes().size()
                            + "; instances = " + deployable.getServiceInstances().size());
                for (CloudNode node : deployable.getSelectedNodes()) {
                    if (deployable.isNewInstanceNode(node))
                        nodesToUpdate.add(node);
                }
            }
        }

    }

    private void submitUpdates() {
        if (nodesToUpdate.size() < 1) {
            logger.info("There are no nodes to update");
            return;
        }
        executor = Executors.newFixedThreadPool(nodesToUpdate.size());
        for (CloudNode node : nodesToUpdate) {
            UpdateNodeTask updater = new UpdateNodeTask(node);
            executor.submit(updater);
        }
    }

    private void waitUpdates() {
        Concurrency.waitExecutor(executor, totalTimeout, "Could not properly update all the nodes of chor " + chorId);
    }

    private class UpdateNodeTask implements Callable<Void> {

        CloudNode node;

        public UpdateNodeTask(CloudNode node) {
            this.node = node;
        }

        @Override
        public Void call() throws Exception {
            NodeUpdater updater = NodeUpdaters.getUpdaterFor(node);
            updater.update();
            return null;
        }
    }

}
