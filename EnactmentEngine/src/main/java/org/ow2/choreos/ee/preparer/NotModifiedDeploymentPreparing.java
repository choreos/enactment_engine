package org.ow2.choreos.ee.preparer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.ee.services.preparer.PrepareDeploymentFailedException;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparer;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparerFactory;
import org.ow2.choreos.services.ServiceNotCreatedException;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class NotModifiedDeploymentPreparing {

    private static Logger logger = Logger.getLogger(NotModifiedDeploymentPreparing.class);

    private String chorId;
    private List<DeployableService> services;
    
    private ExecutorService executor;
    private Map<DeployableServiceSpec, Future<DeployableService>> futures;
    private List<DeployableService> preparedServices;


    public NotModifiedDeploymentPreparing(String chorId, List<DeployableService> services) {
        this.chorId = chorId;
        this.services = services;
    }


    public List<DeployableService> prepare() throws DeploymentException {
        if (services.size() == 0)
            return new ArrayList<DeployableService>();        
        submitPrepareTasks();
        waitConfigureTasks();
        retrievePrepapredServices();
        return preparedServices;
    }

    private void submitPrepareTasks() {
        final int N = services.size();
        executor = Executors.newFixedThreadPool(N);
        futures = new HashMap<DeployableServiceSpec, Future<DeployableService>>();
        for (DeployableService choreographyService : services) {
            ServicePreparerTask invoker = new ServicePreparerTask(choreographyService);
            Future<DeployableService> future = executor.submit(invoker);
            futures.put(choreographyService.getSpec(), future);
        }
    }

    private void waitConfigureTasks() {
        PreparerWaiter waiter = new PreparerWaiter(chorId, executor);
        waiter.waitPreparement();
    }

    private void retrievePrepapredServices() {
        FutureCollector futureCollector = new FutureCollector(chorId, futures);
        preparedServices = futureCollector.collectDeployedServicesFromFutures();
    }

    private class ServicePreparerTask implements Callable<DeployableService> {

        DeployableService service;

        public ServicePreparerTask(DeployableService service) {
            this.service = service;
        }

        @Override
        public DeployableService call() throws Exception {
            try {
                ServiceDeploymentPreparer deploymentPreparer = ServiceDeploymentPreparerFactory.getNewInstance(service);
                deploymentPreparer.prepareDeployment();
                return service;
            } catch (PrepareDeploymentFailedException e1) {
                logger.error("Could not prepare the deployment of the service " + service.getUUID() + " ("
                        + service.getSpec().getName() + ")");
                throw new ServiceNotCreatedException(service.getSpec().getName());
            }
        }
    }

}
