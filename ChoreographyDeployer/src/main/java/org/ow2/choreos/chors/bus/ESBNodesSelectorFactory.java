/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.chors.bus;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.ChoreographyDeployerConfiguration;
import org.ow2.choreos.nodes.NodePoolManager;
import org.ow2.choreos.nodes.client.NodesClient;

public class ESBNodesSelectorFactory {

    private static final String DEPLOYMENT_MANAGER_URI_PROPERTY = "DEPLOYMENT_MANAGER_URI";
    private static Logger logger = Logger.getLogger(ESBNodesSelectorFactory.class);

    private static enum SelectorType {
	SINGLE_NODE, ALWAYS_CREATE
    };

    public static ESBNodesSelector getInstance(String type) {

	try {
	    SelectorType selectorType = SelectorType.valueOf(type);
	    return getInstance(selectorType);
	} catch (IllegalArgumentException e) {
	    logger.error("Invalid ESBNodesSelector type: " + type);
	    throw e;
	}
    }

    private static ESBNodesSelector getInstance(SelectorType type) {

	String host = ChoreographyDeployerConfiguration.get(DEPLOYMENT_MANAGER_URI_PROPERTY);
	NodePoolManager npm = new NodesClient(host);

	switch (type) {
	case SINGLE_NODE:
	    return new SingleESBNodeSelector(npm);

	case ALWAYS_CREATE:
	    return new AlwaysCreateESBNodeSelector(npm);

	default:
	    throw new IllegalArgumentException("Invalid ESBNodesSelector type: " + type);
	}
    }

}
