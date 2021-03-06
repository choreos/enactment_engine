/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.integration.ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.ow2.choreos.TravelAgency;
import org.ow2.choreos.TravelAgencyClientFactory;
import org.ow2.choreos.chors.EnactmentEngine;
import org.ow2.choreos.chors.client.EEClient;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.config.EEConfiguration;
import org.ow2.choreos.ee.rest.EnactmentEngineServer;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.ServiceSpec;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.tests.IntegrationTest;
import org.ow2.choreos.tests.ModelsForTest;
import org.ow2.choreos.utils.LogConfigurator;

/**
 * It is the same than SimpleEnactmentTest, but using the REST API.
 * 
 * This test will enact a choreography with two services, with a service
 * depending on the other.
 * 
 * @author leonardo
 * 
 */
@Category(IntegrationTest.class)
public class RestEnactmentTest {

    private static final String AIRLINE = ModelsForTest.AIRLINE;
    private static final String TRAVEL_AGENCY = ModelsForTest.TRAVEL_AGENCY;

    private static EnactmentEngineServer server;
    private ChoreographySpec chorSpec;

    private ModelsForTest models;

    @BeforeClass
    public static void startServers() {
        LogConfigurator.configLog();
        server = new EnactmentEngineServer();
        server.start();
    }

    @AfterClass
    public static void shutDownServers() {
        server.stop();
    }

    @Before
    public void setUp() {
        EEConfiguration.set("BUS", "false");
        EEConfiguration.set("RESERVOIR", "false");
        models = new ModelsForTest(ServiceType.SOAP, PackageType.COMMAND_LINE);
        chorSpec = models.getChorSpec();
    }

    @Test
    public void shouldConfigureAChoreography() throws Exception {

        String host = EnactmentEngineServer.URL;
        EnactmentEngine ee = new EEClient(host);
        String chorId = ee.createChoreography(chorSpec);

        assertEquals("1", chorId);
    }

    @Test
    public void shouldRetrieveChoreographySpec() throws Exception {

        String host = EnactmentEngineServer.URL;
        EnactmentEngine ee = new EEClient(host);
        String chorId = ee.createChoreography(chorSpec);
        Choreography chor = ee.getChoreography(chorId);

        ServiceSpec travel = chor.getChoreographySpec().getServiceSpecByName(TRAVEL_AGENCY);
        assertEquals(chorSpec.getServiceSpecByName(TRAVEL_AGENCY), travel);

        ServiceSpec airline = chor.getChoreographySpec().getServiceSpecByName(AIRLINE);
        assertEquals(chorSpec.getServiceSpecByName(AIRLINE), airline);
    }

    @Test
    public void shouldEnactChoreography() throws Exception {

        String host = EnactmentEngineServer.URL;
        EnactmentEngine ee = new EEClient(host);
        String chorId = ee.createChoreography(chorSpec);
        Choreography chor = ee.deployChoreography(chorId);
        System.out.println("A chor: " + chor);

        String uri = ((DeployableService) chor.getDeployableServiceBySpecName(ModelsForTest.TRAVEL_AGENCY))
                .getInstances().get(0).getNativeUri();
        String wsdl = uri + "?wsdl";
        TravelAgencyClientFactory factory = new TravelAgencyClientFactory(wsdl);
        TravelAgency client = factory.getClient();
        String codes = client.buyTrip();

        assertTrue(codes.startsWith("33") && codes.endsWith("--22"));
    }

}
