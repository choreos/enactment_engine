package org.ow2.choreos.ee.services.preparer;

import org.ow2.choreos.ee.GlimpseProbe;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.ServiceInstance;

public class ServiceUndeploymentPreparer {

    private DeployableService service;
    private int numberOfReplicasToUndeploy;

    public ServiceUndeploymentPreparer(DeployableService service, int decreaseAmount) {
        this.service = service;
        this.numberOfReplicasToUndeploy = decreaseAmount;
    }

    public void prepareUndeployment() throws PrepareUndeploymentFailedException {

        int delta = numberOfReplicasToUndeploy;

        if (delta < 0)
            delta = 0;
        else if (numberOfReplicasToUndeploy > service.getServiceInstances().size())
            delta = service.getServiceInstances().size();

        int N = service.getServiceInstances().size();
        for (int i = 0; i < delta; i++) {
            ServiceInstance serviceInstanceToUndeploy = service.getServiceInstances().get(N-1-i);
            InstanceUndeploymentPreparer instanceUndeploymentPreparer = new InstanceUndeploymentPreparer(service,
                    serviceInstanceToUndeploy);
            instanceUndeploymentPreparer.prepareUndeployment();

            unpublish(serviceInstanceToUndeploy.getInstanceId());
        }

    }

    private void unpublish(String serviceInstanceId) {
        String metric = "unpublish";
        GlimpseProbe probe = GlimpseProbe.getInstance();
        probe.publishSLA(serviceInstanceId, metric, serviceInstanceId, service.getUUID(), "");
    }

}
