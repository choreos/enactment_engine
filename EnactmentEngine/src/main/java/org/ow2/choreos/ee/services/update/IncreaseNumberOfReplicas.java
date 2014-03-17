package org.ow2.choreos.ee.services.update;

import org.ow2.choreos.ee.services.preparer.PrepareDeploymentFailedException;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparer;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparerFactory;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class IncreaseNumberOfReplicas extends BaseAction {

    private static final String NAME = "Increase number of replicas";

    private DeployableService currentService;
    private DeployableServiceSpec newSpec;

    public IncreaseNumberOfReplicas(DeployableService currentService, DeployableServiceSpec newSpec) {
        this.currentService = currentService;
        this.newSpec = newSpec;
    }

    @Override
    public void applyUpdate() throws UpdateActionFailedException {
        currentService.setSpec(newSpec);
        ServiceDeploymentPreparer deploymentPreparer = ServiceDeploymentPreparerFactory.getNewInstance(currentService);
        try {
            deploymentPreparer.prepareDeployment();
        } catch (PrepareDeploymentFailedException e) {
            throw new UpdateActionFailedException();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
