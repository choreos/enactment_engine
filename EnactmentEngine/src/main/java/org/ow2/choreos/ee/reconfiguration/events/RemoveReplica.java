package org.ow2.choreos.ee.reconfiguration.events;

import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.reconfiguration.ComplexEventHandler;
import org.ow2.choreos.ee.reconfiguration.HandlingEvent;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class RemoveReplica extends ComplexEventHandler {

    @Override
    protected boolean prepareUpdate(HandlingEvent event, String serviceId, String serviceSpecName,
            ChoreographySpec choreographySpec) {
        
        for (DeployableServiceSpec spec : choreographySpec.getDeployableServiceSpecs()) {
            if (spec.getName().equals(serviceSpecName)) {
                logger.debug("Found service spec " + serviceSpecName);
                if (spec.getNumberOfInstances() > 1) {
                    spec.setNumberOfInstances(spec.getNumberOfInstances() - 1);
                    return true;
                } else {
                    logger.debug("Number of instances <= 1. Not going to update");
                    return false;
                }
            }
        }
     
        return false;
    }
}
