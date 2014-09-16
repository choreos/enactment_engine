package org.ow2.choreos.ee.reconfiguration.events;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.reconfiguration.ComplexEventHandler;
import org.ow2.choreos.ee.reconfiguration.HandlingEvent;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class AddReplica extends ComplexEventHandler {

    Logger logger = Logger.getLogger(this.getClass());

    @Override
    protected boolean prepareUpdate(HandlingEvent event, String serviceId, String serviceSpecName,
            ChoreographySpec choreographySpec) {
        
        for (DeployableServiceSpec spec : choreographySpec.getDeployableServiceSpecs()) {
            if (spec.getName().equals(serviceSpecName)) {
                logger.debug("Found service spec. Going to increase number of instances");
                spec.setNumberOfInstances(spec.getNumberOfInstances() + 1);
                return true;
            }
        }
        return false;
    }

}
