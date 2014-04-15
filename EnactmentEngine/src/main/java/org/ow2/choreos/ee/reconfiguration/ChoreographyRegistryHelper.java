package org.ow2.choreos.ee.reconfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.ow2.choreos.chors.ChoreographyNotFoundException;
import org.ow2.choreos.chors.EnactmentEngine;
import org.ow2.choreos.chors.client.EEClient;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.ee.ChorRegistry;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.services.datamodel.DeployableService;
import org.ow2.choreos.services.datamodel.DeployableServiceSpec;

public class ChoreographyRegistryHelper {

	private ChorRegistry registry = ChorRegistry.getInstance();

	private EnactmentEngine enactmentEngine;

	Logger logger = Logger.getLogger("reconfLogger");

	public ChoreographyRegistryHelper() {
		setClients();
	}

	private void setClients() {
		this.enactmentEngine = new EEClient("http://localhost:9100/enactmentengine/");
	}

	public List<DeployableService> getServicesHostedOn(String ipAddress) {
		
		logger.debug("getServicesHostedOn(): " + ipAddress);

		Choreography chor = getChor(ipAddress);
		
		if (chor == null)
			return new ArrayList<DeployableService>();

		List<DeployableService> result = new ArrayList<DeployableService>();
		for (DeployableService service : chor.getDeployableServices()) {
			for (CloudNode node : service.getSelectedNodes())
				if (node.getIp().equals(ipAddress)) {
					result.add(service);
					break;
				}
		}
		return result;
	}

	public List<DeployableServiceSpec> getServiceSpecsForServices(
			List<DeployableService> services) {

		List<DeployableServiceSpec> specs = new ArrayList<DeployableServiceSpec>();
		for (DeployableService service : services) {
			specs.add(service.getSpec());
		}

		return specs;
	}

	public Choreography getChor(String ipAddress) {
		String chorId = searchForChor(ipAddress);

		logger.debug("getChor received = " + chorId);
		
		if (chorId.isEmpty())
			return null;

		Choreography chor = null;
		try {
			chor = enactmentEngine.getChoreography(chorId);
		} catch (ChoreographyNotFoundException e) {
			e.printStackTrace();
		}
		return chor;
	}

	public EnactmentEngine getChorClient() {
		return enactmentEngine;
	}

	private String searchForChor(String ipAddress) {
		
//		Map<String, Choreography> choreographyMap = registry.getAll();
//		
//		logger.debug("Searching in all chors: " + choreographyMap);
//		
//		for (Entry<String, Choreography> chor : choreographyMap.entrySet()) {
//			
//			for (DeployableService service : chor.getValue()
//					.getDeployableServices()) {
//		
//				for (CloudNode node : service.getSelectedNodes()) {
//					
//					if (node.getIp().equals(ipAddress)) {
//						logger.debug("Found chor id: " + chor.getKey());
//						return chor.getKey();
//					}
//				}
//			}
//		}
//		logger.debug("chor not found");
//		return "";
		
		return "1";
	}

	public Choreography getChoreography(String node, String service) {

		for (Entry<String, Choreography> chor : registry.getAll().entrySet()) {

			for (DeployableService s : chor.getValue().getDeployableServices()) {

				if (s.getSpec().getName().equals(service)) {

					for (CloudNode n : s.getSelectedNodes()) {

						if (n.getIp().equals(node)) {
							return chor.getValue();
						}
					}
				}
			}
		}
		return null;
	}
}
