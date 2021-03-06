/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.ee.nodes.selector;

import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.selectors.Selector;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

/**
 * Selects a node to apply a given configuration
 * 
 * The selection can consider functional requirements, which is provided by
 * spec.resourceImpact. Implementing classes must use the NodePoolManager to
 * retrieve nodes AND/OR create new nodes. NodeSelectors are always accessed as
 * singletons. Implementing classes must consider concurrent access to the
 * selectNodes method.
 * 
 * @author leonardo
 * 
 */
public interface NodeSelector extends Selector<CloudNode, DeployableServiceSpec> {

}
