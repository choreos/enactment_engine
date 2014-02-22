package org.ow2.choreos.ee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.EnactmentException;
import org.ow2.choreos.ee.services.preparer.PrepareDeploymentFailedException;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparer;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparerFactory;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.services.ServiceNotCreatedException;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.utils.Concurrency;
import org.ow2.choreos.utils.TimeoutsAndTrials;

public class NotModifiedDeploymentPreparing {

    private String chorId;
    private List<DeployableService> services;
    private ExecutorService executor;
    private Map<DeployableService, Future<DeployableService>> futures;
    private List<DeployableService> configuredServices;

    private int totalTimeout;

    private Logger logger = Logger.getLogger(NotModifiedDeploymentPreparing.class);

    public NotModifiedDeploymentPreparing(String chorId, List<DeployableService> services) {
        this.chorId = chorId;
        this.services = services;
        getTotalTimeout();
    }

    private void getTotalTimeout() {
        int nodeCreationTotalTimeout = TimeoutsAndTrials.getTotalTimeout("NODE_CREATION");
        int firstSshTimeout = TimeoutsAndTrials.getTimeout("FIRST_CONNECT_SSH");
        int bootstrapTotalTimeout = TimeoutsAndTrials.getTotalTimeout("BOOTSTRAP");
        int prepareTotalTimeout = TimeoutsAndTrials.getTotalTimeout("PREPARE_DEPLOYMENT");
        int oneReqPerSec = 2 * 100;
        this.totalTimeout = nodeCreationTotalTimeout + firstSshTimeout + bootstrapTotalTimeout + prepareTotalTimeout
                + oneReqPerSec;
        this.totalTimeout += totalTimeout * 0.2;
    }

    public List<DeployableService> prepare() throws EnactmentException {
        if (services.size() == 0)
            return new ArrayList<DeployableService>();        
        submitConfigureTasks();
        waitConfigureTasks();
        retrieveConfiguredServices();
        checkStatus();
        return configuredServices;
    }

    private void submitConfigureTasks() {
        final int N = services.size();
        executor = Executors.newFixedThreadPool(N);
        futures = new HashMap<DeployableService, Future<DeployableService>>();
        for (DeployableService choreographyService : services) {
            ServicePreparerTask invoker = new ServicePreparerTask(choreographyService);
            Future<DeployableService> future = executor.submit(invoker);
            futures.put(choreographyService, future);
        }
    }

    private void waitConfigureTasks() {
        Concurrency.waitExecutor(executor, totalTimeout, "Could not properly configure all the services of chor "
                + chorId);
    }

    private void retrieveConfiguredServices() {
        configuredServices = new ArrayList<DeployableService>();
        for (Entry<DeployableService, Future<DeployableService>> entry : futures.entrySet()) {
            String specName = entry.getKey().getSpec().getName();
            try {
                DeployableService service = Concurrency.checkAndGetFromFuture(entry.getValue());
                if (service != null) {
                    configuredServices.add(service);
                } else {
                    logger.error("Future returned a null service for service " + specName);
                }
            } catch (ExecutionException e) {
                logger.error("Could not get service from future for service " + specName + " because ", e);
            }
        }
    }

    private void checkStatus() throws EnactmentException {
        if (configuredServices == null || configuredServices.size() != services.size()) {
            throw new EnactmentException();
        }
    }

    private class ServicePreparerTask implements Callable<DeployableService> {

        DeployableService service;

        public ServicePreparerTask(DeployableService service) {
            this.service = service;
        }

        @Override
        public DeployableService call() throws Exception {
            ServiceDeploymentPreparer deploymentPreparer = ServiceDeploymentPreparerFactory.getNewInstance(service);
            try {
                Set<CloudNode> selectedNodes = deploymentPreparer.prepareDeployment();
                service.addSelectedNodes(selectedNodes);
                return service;
            } catch (PrepareDeploymentFailedException e1) {
                logger.error("Could not prepare the deployment of the service " + service.getUUID() + " ("
                        + service.getSpec().getName() + ")");
                throw new ServiceNotCreatedException(service.getSpec().getName());
            }
        }
    }

}
