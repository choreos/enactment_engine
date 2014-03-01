package org.ow2.choreos.ee.reconfiguration;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.junit.Before;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.services.datamodel.qos.DesiredQoS;
import org.ow2.choreos.services.datamodel.qos.ResponseTimeMetric;
import org.ow2.choreos.tests.ModelsForTest;

public class DroolsRulesTest {
	
	final static String AIRLINE_UUID = "1";
	final static String TRAVELAGENCY_UUID = "2";
	final static String CHOR_ID = "1";

	private List<DeployableService> services;

	private DroolsRulesBuilder droolsRulesBuilder = new DroolsRulesBuilder();
	
//	private KnowledgeBase knowledgeBase;
	private String rule;
	private KnowledgeBuilder builder;

	@Before
	public void setUp() {
		ModelsForTest models = new ModelsForTest(ServiceType.SOAP,
				PackageType.TOMCAT);
		Choreography chor = models.getChoreography();

		chor.setId("1");
		DeployableService airline = chor
				.getDeployableServiceBySpecName("airline");
		airline.setUUID("1");
		DeployableService travelagency = chor
				.getDeployableServiceBySpecName("travelagency");
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

		rule = droolsRulesBuilder.assemblyDroolsRules(chor);
	
		builder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();
		
	}

	//@Test
	public void test() {
		builder.add(ResourceFactory.newByteArrayResource(rule.trim().getBytes()), ResourceType.DRL);
		
		System.out.println(rule.trim().toString());
		
		assertFalse(builder.getErrors().toString(), builder.hasErrors());

		//knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
		//knowledgeBase.addKnowledgePackages(builder.getKnowledgePackages());
	}

}
