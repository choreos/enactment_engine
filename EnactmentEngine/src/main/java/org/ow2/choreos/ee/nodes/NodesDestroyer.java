package org.ow2.choreos.ee.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.ow2.choreos.ee.config.CloudConfiguration;
import org.ow2.choreos.ee.nodes.cloudprovider.CloudProvider;
import org.ow2.choreos.ee.nodes.cloudprovider.CloudProviderFactory;
import org.ow2.choreos.invoker.InvokerConfiguration;
import org.ow2.choreos.nodes.NodeNotDestroyed;
import org.ow2.choreos.nodes.NodeNotFoundException;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.utils.Concurrency;

public class NodesDestroyer {

    private static final String TASK_NAME = "NODE_DELETION";

    private static Logger logger = Logger.getLogger(NodesDestroyer.class);

    private final Collection<CloudNode> nodesToDestroy;

    private CloudProvider cp;
    private List<DestroyTask> tasks = new ArrayList<DestroyTask>();
    private ExecutorService executor;

    private int totalTimeout;

    private List<CloudNode> destroyedNodes = new ArrayList<CloudNode>();

    public NodesDestroyer(CloudConfiguration cloudConfiguration, Collection<CloudNode> nodesToDestroy) {
	this.nodesToDestroy = nodesToDestroy;
	this.totalTimeout = InvokerConfiguration.getTotalTimeout(TASK_NAME);
	totalTimeout += totalTimeout * 0.2;
	this.cp = CloudProviderFactory.getFactoryInstance().getCloudProviderInstance(cloudConfiguration);
    }

    /**
     * 
     * @throws NodeNotDestroyed
     *             if some node was not destroyed
     */
    public List<CloudNode> destroyNodes() throws NodeNotDestroyed {
        if (nodesToDestroy == null || nodesToDestroy.isEmpty())
            return new ArrayList<CloudNode>();
        destroy();
	waitDestroy();
	check();
	return destroyedNodes;
    }

    public List<CloudNode> getDestroyedNodes() {
	return destroyedNodes;
    }

    public void setDestroyedNodes(List<CloudNode> destroyedNodes) {
	this.destroyedNodes = destroyedNodes;
    }

    private void destroy() {
	executor = Executors.newFixedThreadPool(nodesToDestroy.size());
	tasks = new ArrayList<DestroyTask>();
	for (CloudNode node : nodesToDestroy) {
	    DestroyTask task = new DestroyTask(node);
	    tasks.add(task);
	    executor.submit(task);
	}
    }

    private void waitDestroy() {
	String erMsg = "Could not wait for nodes destroyment";
	int n = nodesToDestroy.size();
	int totalTimeout = this.totalTimeout + 2 * n; // one req/sec rule
	Concurrency.waitExecutor(executor, totalTimeout, TimeUnit.SECONDS, logger, erMsg);
    }

    private void check() throws NodeNotDestroyed {
	int originalSize = nodesToDestroy.size();
	int finalSize = destroyedNodes.size();
	if (finalSize < originalSize)
	    throw new NodeNotDestroyed("???");
    }

    private class DestroyTask implements Callable<Void> {

	private CloudNode node;

	public DestroyTask(CloudNode node) {
	    this.node = node;
	}

	@Override
	public Void call() throws NodeNotDestroyed, NodeNotFoundException {
	    cp.destroyNode(node.getId());
	    logger.info(node + " destroyed");
	    destroyedNodes.add(node);
	    return null;
	}
    }

}
