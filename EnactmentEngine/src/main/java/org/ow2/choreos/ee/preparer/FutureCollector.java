package org.ow2.choreos.ee.preparer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;
import org.ow2.choreos.utils.Concurrency;

class FutureCollector {

    private static Logger logger = Logger.getLogger(FutureCollector.class);

    private String chorId;
    private Map<DeployableServiceSpec, Future<DeployableService>> futures;

    public FutureCollector(String chorId, Map<DeployableServiceSpec, Future<DeployableService>> futures) {
        this.chorId = chorId;
        this.futures = futures;
    }

    /**
     * 
     * @return prepared services
     */
    public List<DeployableService> collectDeployedServicesFromFutures() {
        List<DeployableService> preparedServices = new ArrayList<DeployableService>();
        for (Entry<DeployableServiceSpec, Future<DeployableService>> entry : futures.entrySet()) {
            String specName = entry.getKey().getName();
            try {
                DeployableService service = Concurrency.checkAndGetFromFuture(entry.getValue());
                if (service != null) {
                    preparedServices.add(service);
                } else {
                    logger.error("Future returned a null service for service " + specName + " on chor " + chorId);
                }
            } catch (ExecutionException e) {
                logger.error("Could not get service from future for service " + specName + "on chor " + chorId
                        + " because ", e);
            }
        }
        return preparedServices;
    }

}
