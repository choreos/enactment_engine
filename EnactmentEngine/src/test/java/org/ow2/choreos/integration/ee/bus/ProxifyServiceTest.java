/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.integration.ee.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.ow2.choreos.Airline;
import org.ow2.choreos.AirlineClientFactory;
import org.ow2.choreos.ee.bus.EasyESBException;
import org.ow2.choreos.ee.bus.EasyESBNode;
import org.ow2.choreos.ee.bus.ProxificationTask;
import org.ow2.choreos.ee.bus.selector.ESBNodeFactory;
import org.ow2.choreos.ee.config.CloudConfiguration;
import org.ow2.choreos.ee.nodes.NPMFactory;
import org.ow2.choreos.ee.services.ServiceCreator;
import org.ow2.choreos.ee.services.ServiceCreatorFactory;
import org.ow2.choreos.nodes.NodeNotFoundException;
import org.ow2.choreos.nodes.NodeNotUpdatedException;
import org.ow2.choreos.nodes.NodePoolManager;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.nodes.datamodel.ResourceImpact;
import org.ow2.choreos.selectors.ObjectCreationException;
import org.ow2.choreos.services.ServiceNotCreatedException;
import org.ow2.choreos.services.ServiceNotFoundException;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.Proxification;
import org.ow2.choreos.services.datamodel.ServiceInstance;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.tests.IntegrationTest;
import org.ow2.choreos.tests.ModelsForTest;
import org.ow2.choreos.utils.LogConfigurator;

/**
 * Deploys a service and a EasyESB node and proxify the service. Before run the
 * test, start the deployment manager.
 * 
 * IT DOES NOT PASS, see issue #1
 * 
 * @author leonardo
 * 
 */
@Category(IntegrationTest.class)
public class ProxifyServiceTest {

    private ModelsForTest models;
    private NodePoolManager npm;
    private ServiceCreator serviceCreator;
    private ServiceInstance serviceInstance;
    private EasyESBNode esbNode;
    private String proxifiedUrl;

    private static final String CLOUD_ACCOUNT = CloudConfiguration.DEFAULT;

    @BeforeClass
    public static void configureLog() {
	LogConfigurator.configLog();
    }

    @Before
    public void setup() {
	models = new ModelsForTest(ServiceType.SOAP, PackageType.COMMAND_LINE);
	npm = NPMFactory.getNewNPMInstance(CLOUD_ACCOUNT);
	serviceCreator = ServiceCreatorFactory.getNewInstance();
    }

    @Test
    public void shouldProxifyAService() throws Exception {

	deployService();
	deployEsbNode();
	proxifyService();

	checkProxifiedWSDLIsOnline();
	invokeService();
    }

    private void deployService() throws ServiceNotCreatedException, NodeNotUpdatedException, NodeNotFoundException,
	    ServiceNotFoundException {
	DeployableServiceSpec airlineSpec = models.getAirlineSpec();
	DeployableService service = serviceCreator.createService(airlineSpec);
	CloudNode node = service.getSelectedNodes().iterator().next();
	npm.updateNode(node.getId());
	serviceInstance = service.getInstances().get(0);
    }

    private void deployEsbNode() throws ObjectCreationException {
	ESBNodeFactory factory = new ESBNodeFactory(npm);
	esbNode = factory.createNewInstance(new ResourceImpact());
    }

    private void proxifyService() {
	Proxification proxification = new Proxification();
	ProxificationTask task = new ProxificationTask(serviceInstance.getServiceSpec().getName(),
		serviceInstance.getNativeUri(), proxification, esbNode);
	try {
	    proxifiedUrl = task.call();
	} catch (EasyESBException e) {
	    System.out.println(e);
	    fail();
	}
    }

    private void checkProxifiedWSDLIsOnline() {
	String wsdl = proxifiedUrl + "?wsdl";
	System.out.println("Accessing " + wsdl);
	WebClient client = WebClient.create(wsdl);
	Response response = client.get();
	assertEquals(200, response.getStatus());
    }

    private void invokeService() throws MalformedURLException {
	String wsdl = proxifiedUrl + "?wsdl";
	AirlineClientFactory factory = new AirlineClientFactory(wsdl);
	Airline wsClient = factory.getClient();
	System.out.println("Accessing buyFlight of " + wsdl);
	String ticketNumber = wsClient.buyFlight();
	assertTrue(ticketNumber.startsWith("33"));
    }

}
