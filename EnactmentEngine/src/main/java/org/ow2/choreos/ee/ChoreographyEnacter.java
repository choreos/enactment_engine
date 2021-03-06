package org.ow2.choreos.ee;

import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.chors.datamodel.LegacyService;
import org.ow2.choreos.ee.config.QoSManagementConfiguration;
import org.ow2.choreos.ee.context.ContextCaster;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.ServiceInstance;

public class ChoreographyEnacter {

    private static Logger logger = Logger.getLogger(ChoreographyEnacter.class);

    private Choreography chor;

    private ChorRegistry reg = ChorRegistry.getInstance();

    public ChoreographyEnacter(Choreography chor) {
        this.chor = chor;
    }

    public Choreography enact() throws DeploymentException {
        logBegin();
        deploy();
        createLegacyServices();
        castContext();
        finish();
        publish();
        logEnd();
        return chor;
    }

    private void logBegin() {
        ChoreographyContext ctx = reg.getContext(chor.getId());
        ChoreographySpec requestedChoreographySpec = ctx.getRequestedChoreographySpec();
        if (chor.getChoreographySpec() == requestedChoreographySpec)
            logger.info("Starting enactment; chorId= " + chor.getId());
        else if (chor.getChoreographySpec() == requestedChoreographySpec)
            logger.info("Starting enactment for requested update; chorId= " + chor.getId());
    }

    private void deploy() throws DeploymentException {
        ServicesDeployer deployer = new ServicesDeployer(chor);
        List<DeployableService> deployedServices = deployer.deployServices();
        chor.setDeployableServices(deployedServices);
    }

    private void createLegacyServices() {
        LegacyServicesCreator legacyServicesCreator = new LegacyServicesCreator();
        List<LegacyService> legacyServices = legacyServicesCreator.createLegacyServices(chor.getChoreographySpec());
        chor.setLegacyServices(legacyServices);
    }

    private void castContext() {
        ContextCaster caster = new ContextCaster(chor);
        caster.cast();
    }

    private void finish() {
        ChoreographyContext ctx = reg.getContext(chor.getId());
        ctx.enactmentFinished();
        ctx.startMonitoring();
    }

    private void publish() {
        boolean qosMgMt = Boolean.parseBoolean(QoSManagementConfiguration.get(QoSManagementConfiguration.QOS_MGMT));
        if (qosMgMt) {
            String metric = "deploy_status";
            String ch = chor.getId();
            GlimpseProbe probe = GlimpseProbe.getInstance();
            for (DeployableService service : chor.getDeployableServices()) {
                for (ServiceInstance instance : service.getInstances()) {
                    logger.info("Monitoging: publishing " + service.getUUID() + " -> " + instance.getInstanceId());
                    probe.publishDeployStatus(metric, ch, service.getUUID(), instance.getInstanceId(), instance
                            .getNode().getIp());
                }
            }
        }
    }

    private void logEnd() {
        logger.info("Enactment completed; chorId=" + chor.getId());
    }

}
