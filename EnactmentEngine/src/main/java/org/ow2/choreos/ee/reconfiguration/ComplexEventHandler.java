package org.ow2.choreos.ee.reconfiguration;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.ChoreographyNotFoundException;
import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.GlimpseProbe;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.ServiceInstance;

public abstract class ComplexEventHandler {

    public Logger logger = Logger.getLogger(this.getClass());

    protected ChoreographyRegistryHelper registryHelper = new ChoreographyRegistryHelper();

    protected abstract boolean prepareUpdate(HandlingEvent event, String serviceId, String serviceSpecName,
            ChoreographySpec choreographySpec);

    public void handleEvent(HandlingEvent event) {
        String serviceId = event.getServiceId();
        String serviceSpecName = getServiceSpecOfService(event, serviceId);

        if (serviceSpecName == null) {
            logger.warn("Service name for service uuid " + serviceId + " is null");
            new GlimpseProbe().sendUpdateEvent("", "finished_update", event.getChor().getId(), serviceId, "all");
            return;
        }

        ChoreographySpec choreographySpec;
        try {
            choreographySpec = registryHelper.getChorClient().getChoreography(event.getChor().getId())
                    .getChoreographySpec();
        } catch (ChoreographyNotFoundException e1) {
            logger.fatal(e1);
            new GlimpseProbe().sendUpdateEvent("", "finished_update", event.getChor().getId(), serviceId, "all");
            return;
        }

        if (!prepareUpdate(event, serviceId, serviceSpecName, choreographySpec)) {
            new GlimpseProbe().sendUpdateEvent("", "finished_update", event.getChor().getId(), serviceId, "all");
            return;
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
        new GlimpseProbe().sendUpdateEvent("wait", "finished_update", event.getChor().getId(), serviceId, "all");
    }

    protected String getServiceSpecOfService(HandlingEvent event, String serviceId) {
        String serviceSpecName = null;
        for (DeployableService s : event.getChor().getDeployableServices()) {
            if (s.getUUID().equals(serviceId))
                serviceSpecName = s.getSpec().getName();
        }
        return serviceSpecName;
    }

    protected String getServiceHoldingInstance(HandlingEvent event) {

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
