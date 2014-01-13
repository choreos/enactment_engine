package org.ow2.choreos.ee.reconfiguration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.ee.config.QoSManagementConfiguration;

public class DroolsRulesBuilder {

	private Logger logger = Logger.getLogger("reconfLogger");

	private static final String CHOR_RULES_TEMPLATE = "rules/drools_rules_template";

	private static final String SERVICES_RULES_PLACEHOLDER = "@{services_rules}";

	private static final String MAX_CPU_USAGE_PLACEHOLDER = "@{max_cpu_usage}";

	private static final String MIN_CPU_USAGE_PLACEHOLDER = "@{min_cpu_usage}";

	private ServiceRulesBuilder servicesRuleBuilder = new ServiceRulesBuilder();

	public String assemblyDroolsRules(Choreography choreography) {
		StringBuffer bf = new StringBuffer();

		try {
			bf = bf.append(FileUtils.readFileToString(new File(getClass()
					.getClassLoader().getResource(CHOR_RULES_TEMPLATE)
					.getFile())));
		} catch (IOException e) {
			logger.error("Could not open chor rules xml template file");
			return bf.toString();
		}

		String glimpseRules = bf
				.toString()
				.replace(
						SERVICES_RULES_PLACEHOLDER,
						servicesRuleBuilder.assemblyRules(
								choreography.getDeployableServices(),
								choreography.getId()))
				.replace(
						MAX_CPU_USAGE_PLACEHOLDER,
						QoSManagementConfiguration
								.get(QoSManagementConfiguration.MAX_CPU_USAGE))
				.replace(
						MIN_CPU_USAGE_PLACEHOLDER,
						QoSManagementConfiguration
								.get(QoSManagementConfiguration.MIN_CPU_USAGE));

		return glimpseRules;
	}

}
