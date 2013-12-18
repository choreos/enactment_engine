package org.ow2.choreos.ee;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.EnactmentException;
import org.ow2.choreos.ee.nodes.cm.NodeUpdater;
import org.ow2.choreos.ee.nodes.cm.NodeUpdaters;
import org.ow2.choreos.invoker.Invoker;
import org.ow2.choreos.invoker.InvokerException;
import org.ow2.choreos.invoker.InvokerFactory;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.utils.Concurrency;
import org.ow2.choreos.utils.TimeoutsAndTrials;

public class NodesUpdater {

    private static final String TASK_NAME = "UPDATE_NODE";

    private List<DeployableService> services;
    private String chorId;
    private Set<CloudNode> nodesToUpdate;
    private ExecutorService executor;

    private int totalTimeout;

    private Logger logger = Logger.getLogger(NodesUpdater.class);

    public NodesUpdater(List<DeployableService> services, String chorId) {
        this.services = services;
        this.chorId = chorId;
        int timeout = TimeoutsAndTrials.get("UPDATE_NODE_TIMEOUT");
        int trials = TimeoutsAndTrials.get("UPDATE_NODE_TRIALS");
        int pause = TimeoutsAndTrials.get("UPDATE_NODE_PAUSE");
        this.totalTimeout = (timeout + pause) * trials;
        this.totalTimeout += totalTimeout * 0.1;
    }

    public void updateNodes() throws EnactmentException {
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
            UpdateNodeInvoker updater = new UpdateNodeInvoker(node);
            executor.submit(updater);
        }
    }

    private void waitUpdates() {
        Concurrency.waitExecutor(executor, totalTimeout, "Could not properly update all the nodes of chor " + chorId);
    }

    private class UpdateNodeInvoker implements Callable<Void> {

        CloudNode node;

        public UpdateNodeInvoker(CloudNode node) {
            this.node = node;
        }

        @Override
        public Void call() {
            UpdateNodeTask task = new UpdateNodeTask(node);
            InvokerFactory<Void> factory = new InvokerFactory<Void>();
            Invoker<Void> invoker = factory.geNewInvokerInstance(TASK_NAME, task);
            try {
                invoker.invoke();
            } catch (InvokerException e) {
                logger.error("Bad response from updating node " + node.getId() + "; maybe some service is not deployed");
            }
            return null;
        }
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
