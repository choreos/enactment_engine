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
import org.ow2.choreos.services.datamodel.ServiceInstance;

public class RemoveReplica extends ComplexEventHandler {
	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void handleEvent(HandlingEvent event) {
		logger.info("Trying to handle " + event.getChorId());
		Choreography c = null;
		try {
			c = registryHelper.getChorClient().getChoreography("1");
		} catch (ChoreographyNotFoundException e1) {
			logger.error("Choeography not found");
		} catch (NullPointerException e) {
			logger.error("Choreography no deployed yet");
			return;
		}

		ChoreographySpec cSpec;
		try {			
			cSpec = c.getChoreographySpec();
		} catch (NullPointerException e) {
			logger.error("Choreography no deployed yet");
			return;
		}
		
		String specName = "";
		
		for (DeployableService s : c.getDeployableServices()) {
			for (ServiceInstance i : s.getInstances()) {
				if (i.getNode().getIp().equals(event.getChorId()))
					specName = s.getSpec().getName();
			}
		}

		if (specName.isEmpty()) {
			logger.debug("Not found service spec for services in " + event.getChorId());
			return;
		}

		for (DeployableServiceSpec s : cSpec.getDeployableServiceSpecs()) {
			if (s.getName().equals(specName)) {
				logger.debug("Found service spec " + specName);
				if (s.getNumberOfInstances() > 1) {
					s.setNumberOfInstances(s.getNumberOfInstances() - 1);
					break;
				}
				else {
					logger.debug("Number of instances <= 1. Not going to update");
					return;
				}
			}
		}

		try {
			logger.info("Going to decrease number of instance for chor with spec: " + cSpec);
			registryHelper.getChorClient().updateChoreography("1", cSpec);
		} catch (ChoreographyNotFoundException e) {
			logger.error(e.getMessage());
		} catch (DeploymentException e) {
			logger.error(e.getMessage());
		}

		try {
			logger.info("Enacting choreography");
			registryHelper.getChorClient().deployChoreography("1");
		} catch (DeploymentException e) {
			logger.error(e.getMessage());
		} catch (ChoreographyNotFoundException e) {
			logger.error(e.getMessage());
		}

	}
}
