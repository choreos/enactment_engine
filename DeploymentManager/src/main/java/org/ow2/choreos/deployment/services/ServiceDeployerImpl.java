package org.ow2.choreos.deployment.services;


import java.io.File;

import org.apache.log4j.Logger;
import org.ow2.choreos.chef.Knife;
import org.ow2.choreos.chef.KnifeException;
import org.ow2.choreos.chef.impl.KnifeImpl;
import org.ow2.choreos.deployment.Configuration;
import org.ow2.choreos.deployment.nodes.ConfigNotAppliedException;
import org.ow2.choreos.deployment.nodes.NodePoolManager;
import org.ow2.choreos.deployment.nodes.datamodel.Config;
import org.ow2.choreos.deployment.nodes.datamodel.Node;
import org.ow2.choreos.deployment.services.datamodel.ArtifactType;
import org.ow2.choreos.deployment.services.datamodel.Service;
import org.ow2.choreos.deployment.services.datamodel.ServiceSpec;
import org.ow2.choreos.deployment.services.recipe.Recipe;
import org.ow2.choreos.deployment.services.recipe.RecipeBuilder;
import org.ow2.choreos.deployment.services.recipe.RecipeBuilderFactory;
import org.ow2.choreos.deployment.services.registry.DeployedServicesRegistry;

public class ServiceDeployerImpl implements ServiceDeployer {

	private Logger logger = Logger.getLogger(ServiceDeployerImpl.class);
	
	private DeployedServicesRegistry registry = new DeployedServicesRegistry();
	private NodePoolManager npm;
	private Knife knife;
	
	public ServiceDeployerImpl(NodePoolManager npm) {
		final String CHEF_REPO = Configuration.get("CHEF_REPO");
		final String CHEF_CONFIG_FILE = Configuration.get("CHEF_CONFIG_FILE");
		this.npm = npm;
		this.knife = new KnifeImpl(CHEF_CONFIG_FILE, CHEF_REPO); 
	}
	
	// protected constructor: to test purposes
	ServiceDeployerImpl(NodePoolManager npm, Knife knife) {
		this.npm = npm;
		this.knife = knife; 
	}

	@Override
	public Service deploy(ServiceSpec serviceSpec) {
		
		Service service = new Service(serviceSpec);
		if (serviceSpec.getArtifactType() != ArtifactType.LEGACY) {
			service = deployNoLegacyService(service);
		} 
		
		if (service != null) {
			registry.addService(service.getName(), service);
		}
		
		return service;
	}

	private Service deployNoLegacyService(Service service) {
		
		Recipe serviceRecipe = this.createRecipe(service);

		try {
			this.uploadRecipe(serviceRecipe);
		} catch (KnifeException e) {
			logger.error("Could not upload recipe", e);
			return null;
		} 

		Node node = deployService(serviceRecipe);
		String hostname = node.getHostname();
		String ip = node.getIp();
		
		if ((hostname == null || hostname.isEmpty())
				&& (ip == null || ip.isEmpty()))
			return null;
		service.setHost(hostname);
		service.setIp(ip);
		service.setNodeId(node.getId());
		
		return service;
	}
	
	private Recipe createRecipe(Service service) {
		
		ArtifactType type = service.getSpec().getArtifactType();
		RecipeBuilder builder = RecipeBuilderFactory.getRecipeBuilderInstance(type);
		Recipe serviceRecipe = builder.createRecipe(service.getSpec());
		return serviceRecipe;
	}
	
	private void uploadRecipe(Recipe serviceRecipe) throws KnifeException {
		
		File folder = new File(serviceRecipe.getCookbookFolder());
		String parent = folder.getParent();
		logger.debug("Uploading recipe " + serviceRecipe.getName());
		String result = this.knife.cookbook().upload(serviceRecipe.getCookbookName(),
				parent);
		logger.debug(result);
	}

	private Node deployService(Recipe serviceRecipe) {
		
		String configName = serviceRecipe.getCookbookName() + "::" + serviceRecipe.getName();
		Config config = new Config(configName);
		Node node;
		try {
			node = npm.applyConfig(config);
		} catch (ConfigNotAppliedException e) {
			return null;
		}
		logger.debug("nodeLocation= " + node.getHostname() + "; node IP="
				+ node.getIp());
		return node;
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

}
