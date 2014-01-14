package org.ow2.choreos.ee.reconfiguration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.choreos.chors.datamodel.Choreography;

public class GlimpseRulesBuilder {

	private Logger logger = Logger.getLogger("reconfLogger");

	private static final String CHOR_RULES_XML_TEMPLATE = "rules/glimpse_rules_template.xml";

	private static final String CHOR_RULES_PLACEHOLDER = "@{chor_rules}";

	private static final String CHOR_ID_PLACEHOLDER = "@{chor_id}";

	private DroolsRulesBuilder droolsRulesBuilder = new DroolsRulesBuilder();

	public String assemblyGlimpseRules(Choreography choreography) {
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
				.replace(CHOR_RULES_PLACEHOLDER,
						droolsRulesBuilder.assemblyDroolsRules(choreography))
				.replace(CHOR_ID_PLACEHOLDER, choreography.getId());
		return glimpseRules;
	}
}
