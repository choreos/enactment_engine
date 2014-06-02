package org.ow2.choreos.ee.services.update;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.choreos.nodes.datamodel.CPUSize;
import org.ow2.choreos.nodes.datamodel.ResourceImpact;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class UpdateActionFactory {

    private Logger logger = Logger.getLogger(UpdateActionFactory.class);

    public List<UpdateAction> getActions(DeployableService currentService, DeployableServiceSpec newServiceSpec) {

        DeployableServiceSpec currentServiceSpec = currentService.getSpec();
        List<UpdateAction> actions = new ArrayList<UpdateAction>();

        logger.info("Calculating changes on service spec \nOld = " + currentServiceSpec + "\nNew = " + newServiceSpec);
        if (currentServiceSpec.getNumberOfInstances() < newServiceSpec.getNumberOfInstances()) {
            UpdateAction action = new IncreaseNumberOfReplicas(currentService, newServiceSpec);
            actions.add(action);
        } else if (currentServiceSpec.getNumberOfInstances() > newServiceSpec.getNumberOfInstances()) {
            UpdateAction action = new DecreaseNumberOfReplicas(currentService, newServiceSpec);
            actions.add(action);
        }

        ResourceImpact currentResourceImpact = currentServiceSpec.getResourceImpact();
        ResourceImpact requestedResourceImpact = newServiceSpec.getResourceImpact();

        if (!isNull(currentResourceImpact) && !isNull(requestedResourceImpact)) {

            CPUSize currentCPU = currentResourceImpact.getCpu();
            CPUSize requestedCPU = requestedResourceImpact.getCpu();

            // TODO needs to be ||
            if (!isNull(currentCPU) && !isNull(requestedCPU)) {

                if (currentCPU.ordinal() != requestedCPU.ordinal()) {
                    UpdateAction action = new Migrate(currentService, newServiceSpec);
                    actions.add(action);
                }else {
                    logger.debug("Same cpu impact");
                }

            } else {
                logger.debug("None cpu impact specified: " + currentCPU + " " + requestedCPU);
            }

        } else {
            logger.debug("None resource impact specified");
        }

        logger.info("Detected changes: " + actions);
        return actions;

    }

    private boolean isNull(Object obj) {
        return (obj == null) ? true : false;
    }

}
