/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.integration.ee.nodes.cloudprovider;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.ow2.choreos.ee.config.CloudConfiguration;
import org.ow2.choreos.ee.nodes.cloudprovider.CloudProvider;
import org.ow2.choreos.ee.nodes.cloudprovider.CloudProviderFactory;
import org.ow2.choreos.nodes.NodeNotCreatedException;
import org.ow2.choreos.nodes.NodeNotDestroyed;
import org.ow2.choreos.nodes.NodeNotFoundException;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.nodes.datamodel.NodeSpec;
import org.ow2.choreos.tests.IntegrationTest;
import org.ow2.choreos.utils.CommandLineException;
import org.ow2.choreos.utils.LogConfigurator;

@Category(IntegrationTest.class)
public class AWSCloudProviderTest {

    private static final String CLOUD_ACCOUNT = "MY_AWS_ACCOUNT";
    private CloudProvider cp;
    private NodeSpec nodeSpec = new NodeSpec();

    @Before
    public void SetUp() {
        LogConfigurator.configLog();
        CloudConfiguration cloudConfiguration = CloudConfiguration.getCloudConfigurationInstance(CLOUD_ACCOUNT);
        CloudProviderFactory factory = CloudProviderFactory.getFactoryInstance();
        this.cp = factory.getCloudProviderInstance(cloudConfiguration);
    }

    @Test
    public void shouldCreateAndDeleteNode() throws NodeNotCreatedException, NodeNotDestroyed, NodeNotFoundException,
            CommandLineException, InterruptedException {

        CloudNode created = cp.createNode(nodeSpec);
        System.out.println("created " + created);
        assertTrue(created != null);

        Thread.sleep(1000);

        cp.destroyNode(created.getId());
    }

}