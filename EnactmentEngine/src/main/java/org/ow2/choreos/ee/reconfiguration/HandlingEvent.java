package org.ow2.choreos.ee.reconfiguration;

public class HandlingEvent {

	String rule;
	String chorId;
	String serviceId;

	public HandlingEvent(String rule, String chorId, String serviceId) {
		super();
		this.rule = rule;
		this.chorId = chorId;
		this.serviceId = serviceId;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getChorId() {
		return chorId;
	}

	public void setChorId(String chorId) {
		this.chorId = chorId;
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
		result = prime * result + ((chorId == null) ? 0 : chorId.hashCode());
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
		if (chorId == null) {
			if (other.chorId != null)
				return false;
		} else if (!chorId.equals(other.chorId))
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
