package org.ow2.choreos.ee.reconfiguration;

import org.ow2.choreos.chors.datamodel.Choreography;

public class HandlingEvent {

	String rule;
	Choreography chor;
	String serviceId;

	public HandlingEvent(String rule, Choreography chor, String serviceId) {
		super();
		this.rule = rule;
		this.chor = chor;
		this.serviceId = serviceId;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public Choreography getChor() {
		return chor;
	}

	public void setChorId(Choreography chor) {
		this.chor = chor;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chor == null) ? 0 : chor.hashCode());
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		result = prime * result
				+ ((serviceId == null) ? 0 : serviceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HandlingEvent other = (HandlingEvent) obj;
		if (chor == null) {
			if (other.chor != null)
				return false;
		} else if (!chor.equals(other.chor))
			return false;
		if (rule == null) {
			if (other.rule != null)
				return false;
		} else if (!rule.equals(other.rule))
			return false;
		if (serviceId == null) {
			if (other.serviceId != null)
				return false;
		} else if (!serviceId.equals(other.serviceId))
			return false;
		return true;
	}

}
