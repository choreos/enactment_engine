package org.ow2.choreos.chors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.Configuration.Option;
import org.ow2.choreos.chors.context.ContextCaster;
import org.ow2.choreos.chors.context.ContextSender;
import org.ow2.choreos.chors.context.ContextSenderFactory;
import org.ow2.choreos.chors.datamodel.ChorSpec;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.deployment.services.datamodel.Service;
import org.ow2.choreos.deployment.services.datamodel.ServiceType;

public class ChorDeployerImpl implements ChoreographyDeployer {

	private Logger logger = Logger.getLogger(ChorDeployerImpl.class);
	private ChorRegistry reg = ChorRegistry.getInstance();
	
	@Override
	public String createChoreography(ChorSpec chor) {

		String chorId = reg.create(chor);
		logger.info("Choreography " + chorId + " created.");
		
		return chorId;
	}

	@Override
	public Choreography getChoreography(String chorId) {

		Choreography chor = reg.get(chorId);
		return chor;
	}

	@Override
	public Choreography enact(String chorId) throws EnactmentException, ChoreographyNotFoundException {

		Choreography chor = reg.get(chorId);
		if (chor == null) {
			throw new ChoreographyNotFoundException(chorId);
		}
		
		logger.info("Starting enactment; chorId= " + chorId);
		
		Deployer deployer = new Deployer();
		Map<String, Service> deployedMap = deployer.deployServices(chor);
		chor.setDeployedServices(new ArrayList<Service>(deployedMap.values()));
		
		boolean useTheBus = Boolean.parseBoolean(Configuration.get(Option.BUS));
		if (useTheBus) {
			this.proxifyServices(chor.getDeployedServices());
		}
		
		ContextSender sender = ContextSenderFactory.getInstance(ServiceType.SOAP);
		ContextCaster caster = new ContextCaster(sender);
		caster.cast(chor.getRequestedSpec(), deployedMap);
		
		chor.choreographyEnacted();
		
		logger.info("Enactment completed; chorId=" + chorId);

		return chor;
	}

	private void proxifyServices(List<Service> deployedServices) {

//		InstancesFilter filter = new InstancesFilter();
//		List<ServiceInstance> instances = filter.filter(deployedServices);
//		SingleESBNodeSelector proxifier = new SingleESBNodeSelector();
//		proxifier.proxify(instances);
		// TODO:  should PUT /services/ (a registry would resolve...)
	}

	@Override
	public void update(String chorId, ChorSpec spec) throws ChoreographyNotFoundException {

		Choreography chor = reg.get(chorId);
		if(chor == null) {
			throw new ChoreographyNotFoundException(chorId);
		}
		
		logger.info("Starting update on choreography with ID " + chorId);
		
		chor.setSpec(spec);
		
		logger.info("Updated choreography with ID " + chorId);
		
		return;
	}

}
