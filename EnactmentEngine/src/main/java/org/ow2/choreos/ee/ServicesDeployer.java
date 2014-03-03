/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.ee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.ee.nodes.cm.NodesUpdater;
import org.ow2.choreos.ee.preparer.NewDeploymentPreparing;
import org.ow2.choreos.ee.preparer.NotModifiedDeploymentPreparing;
import org.ow2.choreos.ee.preparer.UpdateDeploymentPreparing;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class ServicesDeployer {

    private Choreography chor;
    private List<DeployableService> allServices;

    public ServicesDeployer(Choreography chor) {
        this.chor = chor;
    }

    /**
     * 
     * @return all the choreography deployed services (not only the just
     *         deployed)
     * @throws DeploymentException
     */
    public List<DeployableService> deployServices() throws DeploymentException {
        prepare();
        updateNodes();
        return allServices;
    }

    private void prepare() throws DeploymentException {

        String chorId = chor.getId();

        ChorDiffer differ = new ChorDiffer(chor);
        List<DeployableServiceSpec> toCreate = differ.getNewServiceSpecs();
        Map<DeployableService, DeployableServiceSpec> toUpdate = differ.getServicesToUpdate();
        List<DeployableService> notModifiedServices = differ.getNotModifiedServices();

        NewDeploymentPreparing newPreparer = new NewDeploymentPreparing(chorId, toCreate);
        List<DeployableService> preparedNewServices = newPreparer.prepare();

        UpdateDeploymentPreparing updatePreparer = new UpdateDeploymentPreparing(chorId, toUpdate);
        List<DeployableService> preparedUpdatedServices = updatePreparer.prepare();

        NotModifiedDeploymentPreparing notModifiedPreparer = new NotModifiedDeploymentPreparing(chorId,
                notModifiedServices);
        List<DeployableService> preparedNotModifiedServices = notModifiedPreparer.prepare();

        allServices = new ArrayList<DeployableService>(preparedNewServices);
        allServices.addAll(preparedUpdatedServices);
        allServices.addAll(preparedNotModifiedServices);
    }

    private void updateNodes() throws DeploymentException {
        NodesUpdater nodesUpdater = new NodesUpdater(allServices, chor.getId());
        nodesUpdater.updateNodes();
    }

}
