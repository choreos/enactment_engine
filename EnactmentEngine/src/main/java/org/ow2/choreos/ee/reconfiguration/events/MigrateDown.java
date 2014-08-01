package org.ow2.choreos.ee.reconfiguration.events;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.ChoreographyNotFoundException;
import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.reconfiguration.ComplexEventHandler;
import org.ow2.choreos.ee.reconfiguration.HandlingEvent;
import org.ow2.choreos.nodes.datamodel.CPUSize;
import org.ow2.choreos.nodes.datamodel.ResourceImpact;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;
import org.ow2.choreos.services.datamodel.ServiceInstance;

public class MigrateDown extends ComplexEventHandler {

    Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void handleEvent(HandlingEvent event) {

        String serviceId = event.getServiceId();
        String serviceSpecName = getServiceSpecOfService(event, serviceId);

        if (serviceSpecName == null) {
            logger.warn("Service name for service uuid " + serviceId + " is null");
            return;
        }

        ChoreographySpec choreographySpec;
        try {
            choreographySpec = registryHelper.getChorClient().getChoreography(event.getChor().getId()).getChoreographySpec();
        } catch (ChoreographyNotFoundException e1) {
            logger.fatal(e1);
            return;
        }

        for (DeployableServiceSpec spec : choreographySpec.getDeployableServiceSpecs()) {
            if (spec.getName().equals(serviceSpecName)) {
                logger.debug("Found service spec. Going to migrate down");
                
                ResourceImpact resourceImpact = spec.getResourceImpact();
                ResourceImpact ri = resourceImpact;
                if (ri == null) {                    
                    logger.info("No previous resource impact. Not possible to migrate up");
                    return;
                } else {
                    
                    CPUSize cpu = resourceImpact.getCpu();
                    switch (cpu) {
                    case SMALL:
                        logger.info("Current CPU size is small. Not possible to migrate down");                        
                        break;
                    case MEDIUM:
                        resourceImpact.setCpu(CPUSize.SMALL);
                        break;
                    case LARGE:
                        resourceImpact.setCpu(CPUSize.MEDIUM);
                        break;

                    }
                    break;
                }
            }
        }

        try {
            logger.info("Going to update chor with spec: " + choreographySpec);
            registryHelper.getChorClient().updateChoreography(event.getChor().getId(), choreographySpec);
        } catch (ChoreographyNotFoundException e) {
            logger.error(e.getMessage());
        } catch (DeploymentException e) {
            logger.error(e.getMessage());
        }

        try {
            logger.info("Enacting choreography");
            registryHelper.getChorClient().deployChoreography(event.getChor().getId());
        } catch (DeploymentException e) {
            logger.error(e.getMessage());
        } catch (ChoreographyNotFoundException e) {
            logger.error(e.getMessage());
        }

    }

    private String getServiceSpecOfService(HandlingEvent event, String serviceId) {
        String serviceSpecName = null;
        for (DeployableService s : event.getChor().getDeployableServices()) {
            if (s.getUUID().equals(serviceId))
                serviceSpecName = s.getSpec().getName();
        }
        return serviceSpecName;
    }

    private String getServiceHoldingInstance(HandlingEvent event) {

        logger.debug(">>>" + event.getChor().getDeployableServices());

        String serviceId = null;
        for (DeployableService s : event.getChor().getDeployableServices()) {
            boolean found = false;
            for (ServiceInstance i : s.getServiceInstances()) {
                if (i.getInstanceId().equals(event.getServiceId())) {
                    serviceId = s.getUUID();
                    found = true;
                    break;
                }
            }
            if (found)
                break;
        }
        return serviceId;
    }
}
