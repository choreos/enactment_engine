package org.ow2.choreos.ee.reconfiguration.events;

import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.GlimpseProbe;
import org.ow2.choreos.ee.reconfiguration.ComplexEventHandler;
import org.ow2.choreos.ee.reconfiguration.HandlingEvent;
import org.ow2.choreos.nodes.datamodel.CPUSize;
import org.ow2.choreos.nodes.datamodel.ResourceImpact;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class MigrateDown extends ComplexEventHandler {

    @Override
    protected boolean prepareUpdate(HandlingEvent event, String serviceId, String serviceSpecName,
            ChoreographySpec choreographySpec) {
        for (DeployableServiceSpec spec : choreographySpec.getDeployableServiceSpecs()) {
            if (spec.getName().equals(serviceSpecName)) {
                logger.debug("Found service spec. Going to migrate down");

                ResourceImpact resourceImpact = spec.getResourceImpact();
                ResourceImpact ri = resourceImpact;
                if (ri == null) {
                    logger.info("No previous resource impact. Not possible to migrate up");
                    return false;
                } else {
                    CPUSize cpu = resourceImpact.getCpu();
                    switch (cpu) {
                    case SMALL:
                        logger.info("Current CPU size is small. Not possible to migrate down. Going to remove instance");
                        return new RemoveReplica().prepareUpdate(event, serviceId, serviceSpecName, choreographySpec);
                    case MEDIUM:
                        resourceImpact.setCpu(CPUSize.SMALL);
                        unpublish(serviceId);
                        return true;
                    case LARGE:
                        resourceImpact.setCpu(CPUSize.MEDIUM);
                        unpublish(serviceId);
                        return true;
                    default:
                        return false;
                    }
                }
            }
        }
        return false;
    }
    
    private void unpublish(String serviceId) {
        String metric = "unpublish_service";
        GlimpseProbe probe = GlimpseProbe.getInstance();
        probe.publishSLA(serviceId, metric, serviceId, serviceId, "");
    }
}
