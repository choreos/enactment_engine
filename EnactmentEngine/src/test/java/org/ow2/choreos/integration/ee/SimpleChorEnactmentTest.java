/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.integration.ee;

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
import org.ow2.choreos.utils.Alarm;
import org.ow2.choreos.utils.LogConfigurator;

/**
 * This test will enact a choreography with two services, with a service
 * depending on the other.
 * 
 * Before the test, start the DeploymentManager
 * 
 * @author leonardo, tfmend, nelson
 * 
 */
@Category(IntegrationTest.class)
public class SimpleChorEnactmentTest {

    protected ChoreographySpec chorSpec;

    @BeforeClass
    public static void startServers() {
        LogConfigurator.configLog();
    }

    @Before
    public void setUp() {
        EEConfiguration.set("BUS", "false");
        EEConfiguration.set("RESERVOIR", "false");
        ModelsForTest models = new ModelsForTest(ServiceType.SOAP, PackageType.COMMAND_LINE);
        chorSpec = models.getChorSpec();
    }

    @Test
    public void shouldEnactChoreography() throws Exception {

        EnactmentEngine ee = new EEImpl();

        String chorId = ee.createChoreography(chorSpec);
        Choreography chor = ee.enactChoreography(chorId);

        Service travelService = chor.getDeployableServiceBySpecName(ModelsForTest.TRAVEL_AGENCY);
        
        String wsdl = travelService.getUris().get(0) + "?wsdl";
        TravelAgencyClientFactory factory = new TravelAgencyClientFactory(wsdl);
        TravelAgency client = factory.getClient();
        String codes = client.buyTrip();

        assertTrue(codes.startsWith("33") && codes.endsWith("--22"));
        
        Alarm alarm = new Alarm();
        alarm.play();
    }
}
