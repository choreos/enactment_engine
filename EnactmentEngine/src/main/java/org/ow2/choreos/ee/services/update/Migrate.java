package org.ow2.choreos.ee.services.update;

import org.apache.log4j.Logger;
import org.ow2.choreos.ee.services.preparer.PrepareDeploymentFailedException;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparer;
import org.ow2.choreos.ee.services.preparer.ServiceDeploymentPreparerFactory;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class Migrate extends BaseAction {
    
    Logger logger = Logger.getLogger(Migrate.class);
    
    private static final String NAME = "Migrate instance";

    private DeployableService currentService;
    private DeployableServiceSpec newSpec;

    public Migrate(DeployableService currentService, DeployableServiceSpec newSpec) {
        this.currentService = currentService;
        this.newSpec = newSpec;
    }

    @Override
    public void applyUpdate() throws UpdateActionFailedException {
        currentService.setSpec(newSpec);
        currentService.getServiceInstances().clear();
        currentService.getSelectedNodes().clear();
        
        ServiceDeploymentPreparer deploymentPreparer = ServiceDeploymentPreparerFactory.getNewInstance(currentService);
        try {
            logger.debug("preparing migration");
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
