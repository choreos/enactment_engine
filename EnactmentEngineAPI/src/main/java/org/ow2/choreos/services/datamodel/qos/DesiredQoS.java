package org.ow2.choreos.services.datamodel.qos;

import java.io.Serializable;

public class DesiredQoS implements Serializable {

    private static final long serialVersionUID = 5137885233674831781L;
    private static final ScalePolicy DEFAULT_SCALE_POLICY = ScalePolicy.HORIZONTAL;
    
    public enum ScalePolicy {
        HORIZONTAL, VERTICAL
    }

    private ResponseTimeMetric responseTime;
    private ScalePolicy scalePolicy = null;

    public void setResponseTimeMetric(ResponseTimeMetric responseTime) {
        this.responseTime = responseTime;
    }

    public ResponseTimeMetric getResponseTimeMetric() {
        return responseTime;
    }

    public ScalePolicy getScalePolicy() {
        return (scalePolicy != null) ? scalePolicy : DEFAULT_SCALE_POLICY;
    }

    public void setScalePolicy(ScalePolicy scalePolicy) {
        this.scalePolicy = scalePolicy;
    }
}