/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.integration.ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.ow2.choreos.nodes.datamodel.CPUSize;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.nodes.datamodel.RAMSize;
import org.ow2.choreos.nodes.datamodel.ResourceImpact;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.ServiceInstance;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.tests.IntegrationTest;
import org.ow2.choreos.tests.ModelsForTest;
import org.ow2.choreos.utils.LogConfigurator;

/**
 * 
 * @author tfmend, nelson
 * 
 */
@Category(IntegrationTest.class)
public class VerticalScalingTest {

    private ChoreographySpec smallSpec;
    private ChoreographySpec mediumSpec;

    /**
     * Needs to be manually defined with same ip addrress according to the first
     * medium ip in the DeploymentManager properties file
     */
    //private static final String MEDIUM_VM_IP = "192.168.122.14";

    @BeforeClass
    public static void startServers() {
        LogConfigurator.configLog();
    }

    @Before
    public void setUp() {

        EEConfiguration.set("BUS", "false");
        EEConfiguration.set("RESERVOIR", "false");

        ResourceImpact smallImpact = new ResourceImpact();
        smallImpact.setRAM(RAMSize.SMALL);
        smallImpact.setCpu(CPUSize.SMALL);
        ModelsForTest smallModels = new ModelsForTest(ServiceType.SOAP, PackageType.COMMAND_LINE, smallImpact);
        smallSpec = smallModels.getChorSpec();

        ResourceImpact mediumImpact = new ResourceImpact();
        mediumImpact.setRAM(RAMSize.MEDIUM);
        mediumImpact.setCpu(CPUSize.MEDIUM);
        ModelsForTest mediumModels = new ModelsForTest(ServiceType.SOAP, PackageType.COMMAND_LINE, mediumImpact);
        mediumSpec = mediumModels.getChorSpec();
    }

    @Test
    public void shouldMigrateAirlineServiceFromSmallToMediumMachine() throws Exception {

        EnactmentEngine ee = new EEImpl();

        String chorId = ee.createChoreography(smallSpec);
        Choreography chor = ee.deployChoreography(chorId);

        DeployableService airline = chor.getDeployableServiceBySpecName(ModelsForTest.AIRLINE);
        DeployableService travel = chor.getDeployableServiceBySpecName(ModelsForTest.TRAVEL_AGENCY);

        String wsdl = travel.getUris().get(0) + "?wsdl";
        TravelAgencyClientFactory factory = new TravelAgencyClientFactory(wsdl);
        TravelAgency client = factory.getClient();
        String codes = client.buyTrip();
        
        assertEquals(1, airline.getUris().size());
        assertTrue(codes.startsWith("33") && codes.endsWith("--22"));

        ee.updateChoreography(chorId, mediumSpec);
        chor = ee.deployChoreography(chorId);
        Thread.sleep(4000);

        airline = chor.getDeployableServiceBySpecName(ModelsForTest.AIRLINE);
        travel = chor.getDeployableServiceBySpecName(ModelsForTest.TRAVEL_AGENCY);

        wsdl = travel.getUris().get(0) + "?wsdl";
        factory = new TravelAgencyClientFactory(wsdl);
        client = factory.getClient();
        codes = client.buyTrip();

        assertEquals(1, airline.getUris().size());
        assertTrue(codes.startsWith("33") && codes.endsWith("--22"));

        String actualIp = airline.getUris().get(0);
        
     //   assertTrue(fixture == node.getRam());

    }

}
