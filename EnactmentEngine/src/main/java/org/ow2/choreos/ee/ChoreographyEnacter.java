package org.ow2.choreos.ee;

import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.chors.datamodel.LegacyService;
import org.ow2.choreos.ee.context.ContextCaster;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.ServiceInstance;
import org.ow2.choreos.services.datamodel.qos.DesiredQoS;
import org.ow2.choreos.services.datamodel.qos.DesiredQoS.ScalePolicy;
import org.ow2.choreos.services.datamodel.qos.ResponseTimeMetric;

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
    }

    private void publish() {
        String metric = "deploy_status";
        String ch = chor.getId();

        GlimpseProbe probe = GlimpseProbe.getInstance();
        for (DeployableService service : chor.getDeployableServices()) {

            DesiredQoS qos = service.getSpec().getDesiredQoS();

            if (qos != null) {

                // publish "meta-data"
                ResponseTimeMetric rtm = qos.getResponseTimeMetric();
                for (ServiceInstance instance : service.getInstances()) {
                    logger.info("Monitoging: publishing " + service.getUUID() + " -> " + instance.getInstanceId());

                    probe.publishSLA(chor.getChoreographySpec().getResourceParams().getProperty("cpu_min"),
                            "min_cpu_user", "0.2", instance.getNode().getIp(), instance.getNode().getIp());

                    probe.publishSLA(chor.getChoreographySpec().getResourceParams().getProperty("cpu_max"),
                            "max_cpu_user", "0.1", instance.getNode().getIp(), instance.getNode().getIp());

                    probe.publishSLA("" + rtm.getMaxDesiredResponseTime(), "max_response_time",
                            "" + rtm.getAcceptablePercentage(), service.getUUID(), instance.getNode().getIp());

                    if (qos.getScalePolicy() == ScalePolicy.VERTICAL)
                        probe.publishSLA("vertical", "policy", "", service.getUUID(), instance.getNode().getIp());
                    else
                        probe.publishSLA("horizontal", "policy", "", service.getUUID(), instance.getNode().getIp());

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
