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

    public void updateNodes() throws DeploymentException {
        logger.info("Going to update nodes of choreography " + chorId);
        setNodesToUpdate();
        submitUpdates();
        waitUpdates();
    }

    private void setNodesToUpdate() {
        nodesToUpdate = new HashSet<CloudNode>();
        for (DeployableService deployable : services) {
            for (CloudNode node : deployable.getSelectedNodes()) {
                nodesToUpdate.add(node);
            }
        }
    }

    private void submitUpdates() {
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
