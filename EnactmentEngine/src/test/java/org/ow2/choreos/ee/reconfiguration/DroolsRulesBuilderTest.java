package org.ow2.choreos.ee.reconfiguration;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.services.datamodel.qos.DesiredQoS;
import org.ow2.choreos.services.datamodel.qos.ResponseTimeMetric;
import org.ow2.choreos.tests.ModelsForTest;

public class DroolsRulesBuilderTest {

    final static String AIRLINE_UUID = "1";
    final static String TRAVELAGENCY_UUID = "2";
    final static String CHOR_ID = "1";

    List<DeployableService> services;

    DroolsRulesBuilder droolsRulesBuilder = new DroolsRulesBuilder();

    @Test
    @Ignore
    public void createCorrectChorRules() {
        ModelsForTest models = new ModelsForTest(ServiceType.SOAP, PackageType.TOMCAT);
        Choreography chor = models.getChoreography();

        chor.setId("1");
        DeployableService airline = chor.getDeployableServiceBySpecName("airline");
        airline.setUUID("1");
        DeployableService travelagency = chor.getDeployableServiceBySpecName("travelagency");
        travelagency.setUUID("2");

        ResponseTimeMetric responseTime = new ResponseTimeMetric();
        responseTime.setAcceptablePercentage(0.05f);
        responseTime.setMaxDesiredResponseTime(120f);

        DesiredQoS desiredQoS = new DesiredQoS();

        desiredQoS.setResponseTimeMetric(responseTime);

        airline.getSpec().setDesiredQoS(desiredQoS);
        travelagency.getSpec().setDesiredQoS(desiredQoS);

        services = chor.getDeployableServices();
        chor.setDeployableServices(services);

        String assembledDroolsRules = droolsRulesBuilder.assemblyDroolsRules(chor);
        assertTrue(assembledDroolsRules.contains("rule \"HighResponseTime_Service_airline0\""));
    }

    @Test
    @Ignore
    public void serviceSpecWithNullDesiredQoS() {
        ModelsForTest models = new ModelsForTest(ServiceType.SOAP, PackageType.TOMCAT);
        Choreography chor = models.getChoreography();

        chor.setId("1");
        DeployableService airline = chor.getDeployableServiceBySpecName("airline");
        airline.setUUID("1");
        DeployableService travelagency = chor.getDeployableServiceBySpecName("travelagency");
        travelagency.setUUID("2");

        ResponseTimeMetric responseTime = new ResponseTimeMetric();
        responseTime.setAcceptablePercentage(0.05f);
        responseTime.setMaxDesiredResponseTime(120f);

        DesiredQoS desiredQoS = new DesiredQoS();
        desiredQoS.setResponseTimeMetric(responseTime);

        airline.getSpec().setDesiredQoS(desiredQoS);
        travelagency.getSpec().setDesiredQoS(null);

        services = chor.getDeployableServices();
        chor.setDeployableServices(services);

        String assembledDroolsRules = droolsRulesBuilder.assemblyDroolsRules(chor);

        System.out.println(assembledDroolsRules);

        assertTrue(assembledDroolsRules.contains("rule \"HighResponseTime_Service_airline0\""));

    }

}
