package org.ow2.choreos.ee.preparer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.EnactmentException;
import org.ow2.choreos.ee.services.update.ServiceUpdater;
import org.ow2.choreos.services.ServiceNotFoundException;
import org.ow2.choreos.services.ServiceNotModifiedException;
import org.ow2.choreos.services.UnhandledModificationException;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class UpdateDeploymentPreparing {

    private static Logger logger = Logger.getLogger(UpdateDeploymentPreparing.class);

    private String chorId;
    private Map<DeployableService, DeployableServiceSpec> toUpdate;

    private ExecutorService executor;
    private Map<DeployableServiceSpec, Future<DeployableService>> futures;
    private List<DeployableService> preparedServices;

    public UpdateDeploymentPreparing(String chorId, Map<DeployableService, DeployableServiceSpec> toUpdate) {
	this.chorId = chorId;
	this.toUpdate = toUpdate;
    }

    public List<DeployableService> prepare() throws EnactmentException {
	if (toUpdate.size() == 0)
	    return new ArrayList<DeployableService>();
	logger.info("Request to configure nodes; creating services; setting up Chef");
	submitPrepareTasks();
        waitConfigureTasks();
        retrievePreparedServices();	
	return preparedServices;
    }
    
    private void submitPrepareTasks() {
        final int N = toUpdate.size();
        executor = Executors.newFixedThreadPool(N);
        futures = new HashMap<DeployableServiceSpec, Future<DeployableService>>();
        for (Entry<DeployableService, DeployableServiceSpec> entry : toUpdate.entrySet()) {
            DeployableService service = entry.getKey();
            DeployableServiceSpec spec = entry.getValue();
            logger.debug("Requesting update of " + spec);
            ServiceUpdateInvoker invoker = new ServiceUpdateInvoker(service, spec);
            Future<DeployableService> future = executor.submit(invoker);
            futures.put(spec, future);
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

    private class ServiceUpdateInvoker implements Callable<DeployableService> {

	private DeployableServiceSpec serviceSpec;
	private DeployableService service;

	public ServiceUpdateInvoker(DeployableService service, DeployableServiceSpec serviceSpec) {
	    this.serviceSpec = serviceSpec;
	    this.service = service;
	}

	@Override
	public DeployableService call() throws UnhandledModificationException, ServiceNotFoundException,
		ServiceNotModifiedException {

	    ServiceUpdater servicesManager = new ServiceUpdater(service, serviceSpec);
	    try {
		servicesManager.updateService();
		logger.debug("Service updated: " + service);
		return service;
	    } catch (UnhandledModificationException e) {
		logger.error(e.getMessage());
		throw e;
	    }
	}
    }

}
