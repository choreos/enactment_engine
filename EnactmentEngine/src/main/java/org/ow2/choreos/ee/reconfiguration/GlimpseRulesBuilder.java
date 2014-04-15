package org.ow2.choreos.ee.reconfiguration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.ee.config.QoSManagementConfiguration;

public class GlimpseRulesBuilder {

	private Logger logger = Logger.getLogger("reconfLogger");

	private static final String CHOR_RULES_XML_TEMPLATE = "rules/glimpse_rules.xml";

	private static final String MAX_CPU_USAGE_PLACEHOLDER = "@{max_cpu_usage}";

	private static final String MIN_CPU_USAGE_PLACEHOLDER = "@{min_cpu_usage}";
	
	private static final String ACCEPTABLE_PERCENTAGE_RESPONSE_TIME_PLACEHOLDER = "@{acceptable_percentage}";

	private static final String MAX_RESPONSE_TIME_PLACEHOLDER = "@{max_response_time}";



	public String assemblyGlimpseRules() {
		StringBuffer bf = new StringBuffer();

		try {
			bf = bf.append(FileUtils.readFileToString(new File(getClass()
					.getClassLoader().getResource(CHOR_RULES_XML_TEMPLATE)
					.getFile())));
		} catch (IOException e) {
			logger.error("Could not open chor rules xml template file");
			return bf.toString();
		}
		
		String glimpseRules = bf
				.toString()
				.replace(
						MAX_CPU_USAGE_PLACEHOLDER,
						QoSManagementConfiguration
								.get(QoSManagementConfiguration.MAX_CPU_USAGE))
				.replace(
						MIN_CPU_USAGE_PLACEHOLDER,
						QoSManagementConfiguration
								.get(QoSManagementConfiguration.MIN_CPU_USAGE))
		
				.replace(ACCEPTABLE_PERCENTAGE_RESPONSE_TIME_PLACEHOLDER, "0.15")
				
				.replace(MAX_RESPONSE_TIME_PLACEHOLDER, "900");

		return glimpseRules;
	}
	
	
	public static void main(String[] args) {
		System.out.println(new GlimpseRulesBuilder().assemblyGlimpseRules());
	}
}
