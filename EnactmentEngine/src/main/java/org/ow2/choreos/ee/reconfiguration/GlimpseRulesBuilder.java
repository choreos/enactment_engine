package org.ow2.choreos.ee.reconfiguration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class GlimpseRulesBuilder {

    private Logger logger = Logger.getLogger("reconfLogger");

    private String consumerName;

    private static final String CHOR_RULES_XML_TEMPLATE = "rules/glimpse_rules_template.xml";
    private static final String GLIMPSE_CHOREOS_EVENTS_TEMPLATE = "rules/glimpse_choreos_events_template.txt";
    private static final String GLIMPSE_DETECTION_RULES_TEMPLATE = "rules/glimpse_detection_rules_template.txt";
    private static final String RT_GLIMPSE_DETECTION_RULES_TEMPLATE = "rules/rt_glimpse_detection_rules_template.txt";
    private static final String GLIMPSE_RECONFIGURATION_RULES_TEMPLATE = "rules/glimpse_reconfiguration_rules_template.txt";
    private static final String GLIMPSE_VERTICAL_RECONFIGURATION_RULES_TEMPLATE = "rules/glimpse_vertical_reconfiguration_rules_template.txt";

    private static final String GLIMPSE_CHOREOS_EVENTS_PLACEHOLDER = "@{glimpse_choreos_events}";
    private static final String GLIMPSE_DETECTION_RULES_PLACEHOLDER = "@{glimpse_detection_rules}";
    private static final String RT_GLIMPSE_DETECTION_RULES_PLACEHOLDER = "@{rt_glimpse_detection_rules}";
    private static final String GLIMPSE_RECONFIGURATION_RULES_PLACEHOLDER = "@{glimpse_reconfiguration_rules}";
    private static final String GLIMPSE_VERTICAL_RECONFIGURATION_RULES_PLACEHOLDER = "@{glimpse_vertical_reconfiguration_rules}";

    private static final String CONSUMER_ID_PLACEHOLDER = "@{consumer_id}";

    private static final String CLASS_VERSION_PLACEHOLDER = "@{class_version}";

    public GlimpseRulesBuilder(String consumerName) {
        this.consumerName = consumerName;
    }

    public String assemblyGlimpseRules() {
        // The template rule defined in the file CHOR_RULES_XML_TEMPLATE
        String template = getFileContent(CHOR_RULES_XML_TEMPLATE);

        String rule = template.replace(CONSUMER_ID_PLACEHOLDER, this.consumerName);

        // Rules for converting choreos base events into specific events
        String baseEventsTemplate = getFileContent(GLIMPSE_CHOREOS_EVENTS_TEMPLATE);
        rule = rule.replace(GLIMPSE_CHOREOS_EVENTS_PLACEHOLDER, baseEventsTemplate);

        // Rules for detecting relevant events
        String detectionRulesTemplate = getFileContent(GLIMPSE_DETECTION_RULES_TEMPLATE);
        rule = rule.replace(GLIMPSE_DETECTION_RULES_PLACEHOLDER, detectionRulesTemplate);

        String rtDetectionRulesTemplate = getFileContent(RT_GLIMPSE_DETECTION_RULES_TEMPLATE);
        rule = rule.replace(RT_GLIMPSE_DETECTION_RULES_PLACEHOLDER, rtDetectionRulesTemplate);

        // Rules for reconfiguration
        String reconfigurationRulesTemplate;
        reconfigurationRulesTemplate = getFileContent(GLIMPSE_RECONFIGURATION_RULES_TEMPLATE);
        rule = rule.replace(GLIMPSE_RECONFIGURATION_RULES_PLACEHOLDER, reconfigurationRulesTemplate);

        String vtReconfigurationRulesTemplate;
        vtReconfigurationRulesTemplate = getFileContent(GLIMPSE_VERTICAL_RECONFIGURATION_RULES_TEMPLATE);
        rule = rule.replace(GLIMPSE_VERTICAL_RECONFIGURATION_RULES_PLACEHOLDER, vtReconfigurationRulesTemplate);

        rule = rule.replace(CONSUMER_ID_PLACEHOLDER, this.consumerName);
        rule = rule.replace(CLASS_VERSION_PLACEHOLDER, "_" + this.consumerName.replace("-", ""));

        return rule;
    }

    private String getFileContent(String file) {
        StringBuffer template = new StringBuffer();
        try {
            template = template.append(FileUtils.readFileToString(new File(getClass().getClassLoader()
                    .getResource(file).getFile())));
        } catch (IOException e) {
            logger.error("Could not open chor rules xml template file");
        }
        return template.toString();
    }

}
