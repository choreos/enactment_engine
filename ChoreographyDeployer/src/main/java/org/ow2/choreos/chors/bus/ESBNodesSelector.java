/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.chors.bus;

import java.util.Map;

import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.services.datamodel.ServiceInstance;

/**
 * 
 * @author leonardo
 * 
 */
public interface ESBNodesSelector {

    /**
     * Selects EasyESB nodes to each one of the service instances.
     * 
     * Implementors MUST guarantee EASY_ESB services (CDs) will be not
     * proxified.
     * 
     * @param choreography
     * @return a map whose value is a serviceInstance and the value is the
     *         EasyESB node that must be used to proxify the serviceInstance; if
     *         it was not possible to select an esb node to a specific instance,
     *         its value on the map in null.
     */
    public Map<ServiceInstance, EasyESBNode> selectESBNodes(Choreography choreography);

}
