package org.ow2.choreos.ee.reconfiguration;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.config.QoSManagementConfiguration;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;
import org.ow2.choreos.services.datamodel.PackageType;
import org.ow2.choreos.services.datamodel.ServiceInstance;
import org.ow2.choreos.services.datamodel.ServiceType;
import org.ow2.choreos.services.datamodel.qos.DesiredQoS;
import org.ow2.choreos.services.datamodel.qos.DesiredQoS.ScalePolicy;
import org.ow2.choreos.services.datamodel.qos.ResponseTimeMetric;
import org.ow2.choreos.tests.ModelsForTest;

public class GlimpseRulesBuilder {

    private static final String DEFAULT_MAX_CPU = "65";

    private static final String DEFAULT_MIN_CPU = "95";
    
    private static final ScalePolicy DEFAULT_SCALE_POLICY = ScalePolicy.HORIZONTAL;

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

    private static final String MAX_CPU_USAGE_PLACEHOLDER = "@{max_cpu_usage}";
    private static final String MIN_CPU_USAGE_PLACEHOLDER = "@{min_cpu_usage}";
    private static final String ACCEPTABLE_PERCENTAGE_PLACEHOLDER = "@{acceptable_percentage}";
    private static final String MAX_RESPONSE_TIME_PLACEHOLDER = "@{max_response_time}";
    private static final String INSTANCE_ID_PLACEHOLDER = "@{instance}";

    private static final String CONSUMER_ID_PLACEHOLDER = "@{consumer_id}";
    
    private static final String CLASS_VERSION_PLACEHOLDER = "@{class_version}";

    public GlimpseRulesBuilder(String consumerName) {
        this.consumerName = consumerName;
    }

    public String assemblyGlimpseRules(Choreography choreography) {
        // The template rule defined in the file CHOR_RULES_XML_TEMPLATE
        String template = getFileContent(CHOR_RULES_XML_TEMPLATE);
        
        String rule = template.replace(CONSUMER_ID_PLACEHOLDER, this.consumerName);

        // Rules for converting choreos base events into specific events
        String baseEventsTemplate = getFileContent(GLIMPSE_CHOREOS_EVENTS_TEMPLATE);
        rule = rule.replace(GLIMPSE_CHOREOS_EVENTS_PLACEHOLDER, baseEventsTemplate);

        // Rules for detecting relevant events
        String detectionRulesTemplate = getFileContent(GLIMPSE_DETECTION_RULES_TEMPLATE);
        rule = rule.replace(GLIMPSE_DETECTION_RULES_PLACEHOLDER, detectionRulesTemplate)
                .replace(MIN_CPU_USAGE_PLACEHOLDER, choreography.getChoreographySpec().getResourceParams().getProperty("cpu_min", DEFAULT_MIN_CPU))
                .replace(MAX_CPU_USAGE_PLACEHOLDER, choreography.getChoreographySpec().getResourceParams().getProperty("cpu_max", DEFAULT_MAX_CPU));

        
        String rtDetectionRulesTemplate = "";
        for (DeployableService service : choreography.getDeployableServices()) {

            DesiredQoS desiredQoS = service.getSpec().getDesiredQoS();
            if (desiredQoS == null)
                continue;

            for (ServiceInstance instance : service.getInstances()) {
                ResponseTimeMetric responseTimeMetric = desiredQoS.getResponseTimeMetric();

                
                rtDetectionRulesTemplate = rtDetectionRulesTemplate.concat(getFileContent(RT_GLIMPSE_DETECTION_RULES_TEMPLATE)
                        .replace(INSTANCE_ID_PLACEHOLDER, instance.getInstanceId())
                        .replace(MAX_RESPONSE_TIME_PLACEHOLDER, "" + responseTimeMetric.getMaxDesiredResponseTime())
                        .replace(ACCEPTABLE_PERCENTAGE_PLACEHOLDER, "" + responseTimeMetric.getAcceptablePercentage()));
                
            }
        }
        rule = rule.replace(RT_GLIMPSE_DETECTION_RULES_PLACEHOLDER, rtDetectionRulesTemplate);

        // Rules for reconfiguration
        String reconfigurationRulesTemplate;
        if (DEFAULT_SCALE_POLICY == ScalePolicy.HORIZONTAL)
            reconfigurationRulesTemplate = getFileContent(GLIMPSE_RECONFIGURATION_RULES_TEMPLATE);
        else
            reconfigurationRulesTemplate = getFileContent(GLIMPSE_VERTICAL_RECONFIGURATION_RULES_TEMPLATE);
        
        rule = rule.replace(GLIMPSE_RECONFIGURATION_RULES_PLACEHOLDER, reconfigurationRulesTemplate);
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
