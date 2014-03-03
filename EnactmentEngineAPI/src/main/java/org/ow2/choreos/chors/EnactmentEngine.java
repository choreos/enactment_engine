/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.chors;

import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;

public interface EnactmentEngine {

    /**
     * Creates a new choreography that still have to be deployed.
     * 
     * @param services
     *            specification of choreography services
     * @return the id of the just created choreography
     */
    public String createChoreography(ChoreographySpec chor);

    /**
     * Retrieve choreography information.
     * 
     * @param chorId
     *            the choreography id
     * @return the choreography representation
     * @throws ChoreographyNotFoundException
     *             if <code>chorId</code> does not exist
     */
    public Choreography getChoreography(String chorId) throws ChoreographyNotFoundException;

    /**
     * Deploys a choreography
     * 
     * @param chorId
     *            the choreography id
     * @return choreography representation, including information about deployed
     *         services
     * @throws ChoreographyNotFoundException
     *             if <code>chorId</code> does not exist
     * @throws DeploymentException
     *             if something goes wrong
     */
    public Choreography deployChoreography(String chorId) throws DeploymentException, ChoreographyNotFoundException;

    /**
     * Updates a choreography
     * 
     * @param chorId
     *            the choreography id
     * @return choreography representation, including information about deployed
     *         services
     * @throws ChoreographyNotFoundException
     *             if <code>chorId</code> does not exist
     * @throws DeploymentException
     *             if something goes wrong
     */
    public void updateChoreography(String chorId, ChoreographySpec spec) throws DeploymentException,
            ChoreographyNotFoundException;

}
