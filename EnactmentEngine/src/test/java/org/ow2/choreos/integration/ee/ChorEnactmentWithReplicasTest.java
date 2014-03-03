/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.integration.ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.ow2.choreos.TravelAgency;
import org.ow2.choreos.TravelAgencyClientFactory;
import org.ow2.choreos.chors.EnactmentEngine;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.EEImpl;
import org.ow2.choreos.ee.config.EEConfiguration;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.Service;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.tests.IntegrationTest;
import org.ow2.choreos.tests.ModelsForTest;
import org.ow2.choreos.utils.LogConfigurator;

/**
 * This test will enact a choreography with two services. The Airline service
 * will serve with two replicas. The test assume that Travel Agency round robins
 * the Airline instances.
 * 
 * @author tfmend
 * 
 */
@Category(IntegrationTest.class)
public class ChorEnactmentWithReplicasTest {

    private ChoreographySpec spec;

    @BeforeClass
    public static void startServers() {
        LogConfigurator.configLog();
    }

    @Before
    public void setUp() {
        EEConfiguration.set("BUS", "false");
        EEConfiguration.set("RESERVOIR", "false");
        ModelsForTest models = new ModelsForTest(ServiceType.SOAP, PackageType.COMMAND_LINE, 2);
        spec = models.getChorSpec();
    }

    @Test
    public void shouldEnactChoreographyWithTwoAirlineServices() throws Exception {

        EnactmentEngine ee = new EEImpl();

        String chorId = ee.createChoreography(spec);
        Choreography chor = ee.deployChoreography(chorId);

        Service airline = chor.getDeployableServiceBySpecName(ModelsForTest.AIRLINE);
        assertEquals(2, airline.getUris().size());

        Service travel = chor.getDeployableServiceBySpecName(ModelsForTest.TRAVEL_AGENCY);
        String wsdl = travel.getUris().get(0) + "?wsdl";
        TravelAgencyClientFactory factory = new TravelAgencyClientFactory(wsdl);
        TravelAgency client = factory.getClient();
        String codes = client.buyTrip();
        assertTrue(codes.startsWith("33") && codes.endsWith("--22"));

        TravelAgencyClientFactory factory2 = new TravelAgencyClientFactory(wsdl);
        TravelAgency client2 = factory2.getClient();
        String codes2 = client2.buyTrip();
        assertTrue(codes2.startsWith("33") && codes2.endsWith("--22"));

        System.out.println(codes);
        System.out.println(codes2);
        assertFalse(codes.equals(codes2));
    }

}
