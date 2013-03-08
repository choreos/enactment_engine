package org.ow2.choreos.deployment.services;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.choreos.chef.Knife;
import org.ow2.choreos.chef.KnifeException;
import org.ow2.choreos.chef.impl.KnifeImpl;
import org.ow2.choreos.deployment.Configuration;
import org.ow2.choreos.deployment.nodes.ConfigNotAppliedException;
import org.ow2.choreos.deployment.nodes.NodePoolManager;
import org.ow2.choreos.deployment.nodes.datamodel.Config;
import org.ow2.choreos.deployment.nodes.datamodel.Node;
import org.ow2.choreos.deployment.services.bus.EasyESBNodesSelector;
import org.ow2.choreos.deployment.services.datamodel.PackageType;
import org.ow2.choreos.deployment.services.datamodel.Service;
import org.ow2.choreos.deployment.services.datamodel.ServiceInstance;
import org.ow2.choreos.deployment.services.datamodel.ServiceSpec;
import org.ow2.choreos.deployment.services.diff.UnhandledModificationException;
import org.ow2.choreos.deployment.services.diff.UpdateAction;
import org.ow2.choreos.deployment.services.recipe.Recipe;
import org.ow2.choreos.deployment.services.recipe.RecipeBuilder;
import org.ow2.choreos.deployment.services.recipe.RecipeBuilderFactory;
import org.ow2.choreos.deployment.services.registry.DeployedServicesRegistry;

public class ServicesManagerImpl implements ServicesManager {

	private Logger logger = Logger.getLogger(ServicesManagerImpl.class);
	
	private DeployedServicesRegistry registry = new DeployedServicesRegistry();
	private NodePoolManager npm;
	private Knife knife;
	
	public ServicesManagerImpl(NodePoolManager npm) {
		
		final String CHEF_REPO = Configuration.get("CHEF_REPO");
		final String CHEF_CONFIG_FILE = Configuration.get("CHEF_CONFIG_FILE");
		this.npm = npm;
		this.knife = new KnifeImpl(CHEF_CONFIG_FILE, CHEF_REPO); 
	}
	
	// protected constructor: to test purposes
	ServicesManagerImpl(NodePoolManager npm, Knife knife) {
		
		this.npm = npm;
		this.knife = knife; 
	}

	@Override
	public Service createService(ServiceSpec serviceSpec) throws ServiceNotDeployedException {
		
		Service service = null;
		try {
			service = new Service(serviceSpec);
		} catch (IllegalArgumentException e) {
			String message = "Invalid service spec"; 
			logger.error(message, e);
			throw new ServiceNotDeployedException(service.getName(), message);
		}

		boolean useTheBus = Boolean.parseBoolean(Configuration.get("BUS"));
		// I'm not comfortable with the "packageType != PackageType.EASY_ESB"
		if (useTheBus && serviceSpec.getPackageType() != PackageType.EASY_ESB) {
			EasyESBNodesSelector selector = new EasyESBNodesSelector();
			selector.selecESBtNodes(service, this.npm);
		}
		
		if (serviceSpec.getPackageType() != PackageType.LEGACY) {
			service = deployNoLegacyService(service);
		} 
		
		registry.addService(service.getName(), service);
		return service;
		
	}

	private Service deployNoLegacyService(Service service) {
		
		prepareDeployment(service);
		logger.debug("prepare deployment complete");
		executeDeployment(service, service.getSpec().getNumberOfInstances());
		logger.debug("execute deployment complete");
		
		return service;
	}

	private void prepareDeployment(Service service) {
		Recipe serviceRecipe = this.createRecipe(service);

		try {
			this.uploadRecipe(serviceRecipe);
		} catch (KnifeException e) {
			logger.error("Could not upload recipe", e);
			return;
		}
	}
	
	private Recipe createRecipe(Service service) {
		
		PackageType type = service.getSpec().getPackageType();
		RecipeBuilder builder = RecipeBuilderFactory.getRecipeBuilderInstance(type);
		Recipe serviceRecipe = builder.createRecipe(service.getSpec());
		service.setRecipe(serviceRecipe);
		return serviceRecipe;
	}
	
	private void uploadRecipe(Recipe serviceRecipe) throws KnifeException {
		
		File folder = new File(serviceRecipe.getCookbookFolder());
		String parent = folder.getParent();
		logger.debug("Uploading recipe " + serviceRecipe.getName());
		String result = this.knife.cookbook().upload(serviceRecipe.getCookbookName(), parent);
		logger.debug(result);
	}

	private void executeDeployment(Service service, int numberOfNewInstances) {
		
		Recipe serviceRecipe = service.getRecipe();
		String configName = serviceRecipe.getCookbookName() + "::" + serviceRecipe.getName();
		Config config = new Config(configName);
		
		List<Node> nodes = new ArrayList<Node>();
		try {
			nodes = npm.applyConfig(config, numberOfNewInstances);
		} catch (ConfigNotAppliedException e) {
			logger.error(e.getMessage());

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		for(Node node:nodes) {
			if (!((node.getHostname() == null || node.getHostname().isEmpty()) 
					&& (node.getIp() == null || node.getIp().isEmpty()))) {
				logger.debug("nodeLocation= " + node.getHostname() + "; node IP=" + node.getIp());
				@SuppressWarnings("unused") // service will have pointers to their instances
				ServiceInstance serviceInstance = new ServiceInstance(node, service);
			} else {
				logger.debug("request to create a node with no IP or hostname!");
			}
		}
				
	}

	@Override
	public Service getService(String serviceId) {
		
		return registry.getService(serviceId);
	}


	@Override
	public void deleteService(String serviceName) throws ServiceNotDeletedException {
		
		registry.deleteService(serviceName);
		if (registry.getService(serviceName) != null)
			throw new ServiceNotDeletedException(serviceName);
	}

	@Override
	public void updateService(ServiceSpec serviceSpec) throws UnhandledModificationException {
		
		Service current = registry.getService(serviceSpec.getName());
		ServiceSpec currentSpec = current.getSpec();
		
		List<UpdateAction> actions = getActions(currentSpec, serviceSpec);
		
		applyUpdate(current, serviceSpec, actions);
		
	}

	private void applyUpdate(Service current, ServiceSpec requested, List<UpdateAction> actions) throws UnhandledModificationException {
		for ( UpdateAction a : actions ) {
			switch (a) {
			case INCREASE_NUMBER_OF_REPLICAS:
				int amount = requested.getNumberOfInstances() - current.getSpec().getNumberOfInstances();
				addServiceInstances(current, amount);
				break;

			default:
				throw new UnhandledModificationException();
			}
		}
	}

	private void addServiceInstances(Service current, int amount) {
		
		executeDeployment(current, amount);
		
	}

	private List<UpdateAction> getActions(ServiceSpec currentSpec,
			ServiceSpec serviceSpec) {
		boolean foundKnownModification = false;
		
		List<UpdateAction> actions = new ArrayList<UpdateAction>();
		
		if(currentSpec.getNumberOfInstances() < serviceSpec.getNumberOfInstances())
			actions.add(UpdateAction.INCREASE_NUMBER_OF_REPLICAS);
		
		if(!foundKnownModification) {
			actions.add(UpdateAction.UNKNOWN_MODIFICATION);
		}
		
		return actions;
	}
}