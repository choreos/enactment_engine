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
import org.ow2.choreos.chors.EnactmentException;
import org.ow2.choreos.ee.services.ServiceCreator;
import org.ow2.choreos.ee.services.ServiceCreatorFactory;
import org.ow2.choreos.ee.services.preparer.PrepareDeploymentFailedException;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparer;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparerFactory;
import org.ow2.choreos.services.ServiceNotCreatedException;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class NewDeploymentPreparing {

    private static Logger logger = Logger.getLogger(NewDeploymentPreparing.class);

    private String chorId;
    private List<DeployableServiceSpec> specs;

    private ExecutorService executor;
    private Map<DeployableServiceSpec, Future<DeployableService>> futures;
    private List<DeployableService> preparedServices;

    public NewDeploymentPreparing(String chorId, List<DeployableServiceSpec> specs) {
        this.chorId = chorId;
        this.specs = specs;
    }

    public List<DeployableService> prepare() throws EnactmentException {
        if (specs.size() == 0)
            return new ArrayList<DeployableService>();
        logger.info("Request to configure nodes; creating services; setting up Chef; for chor " + chorId);
        submitPrepareTasks();
        waitConfigureTasks();
        retrievePreparedServices();
        logger.info("Nodes are configured to run chef-client on chor " + chorId);
        return preparedServices;
    }

    private void submitPrepareTasks() {
        final int N = specs.size();
        executor = Executors.newFixedThreadPool(N);
        futures = new HashMap<DeployableServiceSpec, Future<DeployableService>>();
        for (DeployableServiceSpec choreographyServiceSpec : specs) {
            CreateServiceTask invoker = new CreateServiceTask(choreographyServiceSpec);
            Future<DeployableService> future = executor.submit(invoker);
            futures.put(choreographyServiceSpec, future);
        }
    }

    private void waitConfigureTasks() {
        PreparerWaiter waiter = new PreparerWaiter(chorId, executor);
        waiter.waitPreparement();
    }

    private void retrievePreparedServices() {
        FutureCollector futureCollector = new FutureCollector(chorId, futures);
        preparedServices = futureCollector.collectDeployedServicesFromFutures();
    }

    private class CreateServiceTask implements Callable<DeployableService> {

        DeployableServiceSpec spec;

        public CreateServiceTask(DeployableServiceSpec serviceSpec) {
            this.spec = serviceSpec;
        }

        @Override
        public DeployableService call() throws Exception {
            try {
                ServiceCreator serviceCreator = ServiceCreatorFactory.getNewInstance();
                DeployableService service = serviceCreator.createService(spec);
                ServiceDeploymentPreparer deploymentPreparer = ServiceDeploymentPreparerFactory.getNewInstance(service);
                deploymentPreparer.prepareDeployment();
                return service;
            } catch (ServiceNotCreatedException e) {
                logger.error("Service " + spec.getName() + " not created!");
                throw e;
            } catch (PrepareDeploymentFailedException e) {
                logger.error("Could not prepare the deployment of the service " + spec.getName());
                throw new ServiceNotCreatedException(spec.getName());
            }
        }
    }

}
