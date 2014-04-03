package org.ow2.choreos.ee.reconfiguration.events;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.ChoreographyNotFoundException;
import org.ow2.choreos.chors.DeploymentException;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.ee.reconfiguration.ComplexEventHandler;
import org.ow2.choreos.ee.reconfiguration.HandlingEvent;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class HighResponseTime extends ComplexEventHandler {

	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void handleEvent(HandlingEvent event, Choreography chor) {

		String serviceId = event.getServiceId();

		if (chor == null) 
			return;
		
		String serviceSpecName = null;
		for (DeployableService s : chor.getDeployableServices()) {
			if (s.getUUID().equals(serviceId))
				serviceSpecName = s.getSpec().getName();
		}

		if (serviceSpecName == null) {
			logger.warn("Service name for service uuid " + serviceId + " is null");
			return;
		}

		ChoreographySpec choreographySpec = chor.getChoreographySpec();

		for (DeployableServiceSpec spec : choreographySpec
				.getDeployableServiceSpecs()) {
			if (spec.getName().equals(serviceSpecName)) {
				logger.debug("Found service spec. Going to increase number of instances");
				spec.setNumberOfInstances(spec.getNumberOfInstances() + 1);
				break;
			}
		}

		try {
			logger.info("Going to update chor with spec: " + choreographySpec);
			registryHelper.getChorClient().updateChoreography(
					chor.getId(), choreographySpec);
		} catch (ChoreographyNotFoundException e) {
			logger.error(e.getMessage());
		} catch (DeploymentException e) {
			logger.error(e.getMessage());
		}

		try {
			logger.info("Enacting choreography");
			registryHelper.getChorClient().deployChoreography(
					chor.getId());
		} catch (DeploymentException e) {
			logger.error(e.getMessage());
		} catch (ChoreographyNotFoundException e) {
			logger.error(e.getMessage());
		}

	}
}
