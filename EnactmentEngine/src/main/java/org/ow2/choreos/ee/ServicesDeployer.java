/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.ee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.choreos.chors.EnactmentException;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.ee.nodes.cm.NodesUpdater;
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
     * @throws EnactmentException
     */
    public List<DeployableService> deployServices() throws EnactmentException {
        prepare();
        updateNodes();
        return allServices;
    }

    private void prepare() throws EnactmentException {

        String chorId = chor.getId();

        ChorDiffer differ = new ChorDiffer(chor);
        List<DeployableServiceSpec> toCreate = differ.getNewServiceSpecs();
        Map<DeployableService, DeployableServiceSpec> toUpdate = differ.getServicesToUpdate();
        List<DeployableService> notModifiedServices = differ.getNotModifiedServices();

        NewDeploymentPreparing newPreparer = new NewDeploymentPreparing(chorId, toCreate);
        List<DeployableService> newServices = newPreparer.prepare();

        UpdateDeploymentPreparing updatePreparer = new UpdateDeploymentPreparing(chorId, toUpdate);
        List<DeployableService> updatedServices = updatePreparer.prepare();

        NotModifiedDeploymentPreparing notModifiedPreparer = new NotModifiedDeploymentPreparing(chorId,
                notModifiedServices);
        List<DeployableService> notModifiedPreparedServices = notModifiedPreparer.prepare();

        allServices = new ArrayList<DeployableService>(newServices);
        allServices.addAll(updatedServices);
        allServices.addAll(notModifiedPreparedServices);
    }

    private void updateNodes() throws EnactmentException {
        NodesUpdater nodesUpdater = new NodesUpdater(allServices, chor.getId());
        nodesUpdater.updateNodes();
    }

}
