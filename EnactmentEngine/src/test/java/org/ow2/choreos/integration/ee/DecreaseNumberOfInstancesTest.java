/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.integration.ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.concurrent.Callable;

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
import org.ow2.choreos.invoker.Invoker;
import org.ow2.choreos.invoker.InvokerBuilder;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.Service;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.tests.IntegrationTest;
import org.ow2.choreos.tests.ModelsForTest;
import org.ow2.choreos.utils.Alarm;
import org.ow2.choreos.utils.LogConfigurator;

/**
 * This test will enact a choreography with two services. One of them will serve
 * with three replicas and will be updated to serve with two
 * 
 * Before the test, start the DeploymentManagerServer
 * 
 * @author tfmend
 * 
 */
@Category(IntegrationTest.class)
public class DecreaseNumberOfInstancesTest {

    private ChoreographySpec spec;
    private ChoreographySpec newSpec;

    @BeforeClass
    public static void startServers() {
        LogConfigurator.configLog();
    }

    @Before
    public void setUp() {
        EEConfiguration.set("BUS", "false");
        EEConfiguration.set("RESERVOIR", "false");
        ModelsForTest models = new ModelsForTest(ServiceType.SOAP, PackageType.TOMCAT, 3);
        spec = models.getChorSpec();
        ModelsForTest newModels = new ModelsForTest(ServiceType.SOAP, PackageType.TOMCAT, 2);
        newSpec = newModels.getChorSpec();
    }

    @Test
    public void shouldEnactChoreographyWithTwoAirlineServicesAndChangeToThree() throws Exception {

        EnactmentEngine ee = new EEImpl();

        String chorId = ee.createChoreography(spec);
        Choreography chor = ee.deployChoreography(chorId);

        Service airline = chor.getDeployableServiceBySpecName(ModelsForTest.AIRLINE);

        DeployableService travel = chor.getDeployableServiceBySpecName(ModelsForTest.TRAVEL_AGENCY);

        Invoker<String> invoker = getBuyTripInvoker(travel);

        String codes, codes2, codes3 = "";

        codes = invoker.invoke();
        codes2 = invoker.invoke();
        codes3 = invoker.invoke();

        assertEquals(3, airline.getUris().size());
        assertTrue(codes.startsWith("33") && codes.endsWith("--22"));
        assertTrue(codes2.startsWith("33") && codes2.endsWith("--22"));
        assertTrue(codes3.startsWith("33") && codes3.endsWith("--22"));
        assertFalse(codes.equals(codes2));
        assertFalse(codes3.equals(codes));
        assertFalse(codes3.equals(codes2));

        ee.updateChoreography(chorId, newSpec);
        chor = ee.deployChoreography(chorId);

        airline = chor.getDeployableServiceBySpecName(ModelsForTest.AIRLINE);
        travel = chor.getDeployableServiceBySpecName(ModelsForTest.TRAVEL_AGENCY);
        invoker = getBuyTripInvoker(travel);

        codes = invoker.invoke();
        codes2 = invoker.invoke();

        assertEquals(2, airline.getUris().size());
        assertTrue(codes.startsWith("33") && codes.endsWith("--22"));
        assertTrue(codes2.startsWith("33") && codes2.endsWith("--22"));
        assertFalse(codes.equals(codes2));

    //    Alarm alarm = new Alarm();
    //    alarm.play();

    }

    private Invoker<String> getBuyTripInvoker(DeployableService travelAgency) {
        BuyTripTask task = new BuyTripTask(travelAgency);
        String taskName = "BuyTripInvokerTask";
        Invoker<String> invoker = new InvokerBuilder<String>(taskName, task, 10).trials(2).pauseBetweenTrials(10)
                .build();
        return invoker;
    }

    private class BuyTripTask implements Callable<String> {

        private TravelAgency client;

        public BuyTripTask(DeployableService travelAgency) {
            String wsdl = travelAgency.getUris().get(0) + "?wsdl";
            TravelAgencyClientFactory factory = new TravelAgencyClientFactory(wsdl);
            try {
                client = factory.getClient();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String call() throws Exception {
            return client.buyTrip();
        }

    }

}
