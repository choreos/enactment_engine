/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.ee.bus;

import org.apache.log4j.Logger;

import com.ebmwebsourcing.easyesb.admin.client.impl.AdminClientImpl;

import easybox.easyesb.petalslink.com.soa.model.datatype._1.EJaxbBasicNodeInformationsType;
import easyesb.petalslink.com.data.admin._1.AddNeighBourNode;
import easyesb.petalslink.com.data.admin._1.GetNodeInformations;
import easyesb.petalslink.com.data.admin._1.GetNodeInformationsResponse;
import esstar.petalslink.com.service.management._1_0.ManagementException;

/**
 * Access an EasyESB node.
 * 
 * @author leonardo
 * 
 */
public class EasyESBNodeImpl implements EasyESBNode {

    private static Logger logger = Logger.getLogger(EasyESBNodeImpl.class);

    private final String adminEndpoint;

    static {
	EasyAPILoader.loadEasyAPI();
    }

    public EasyESBNodeImpl(String adminEndpoint) {
	this.adminEndpoint = adminEndpoint;
    }
    
    @Override
    public String getAdminEndpoint() {
	return this.adminEndpoint;
    }

    @Override
    public void addNeighbour(EasyESBNode neighbour) throws EasyESBException {
	String neighbourAdminEndpoint = neighbour.getAdminEndpoint();
	EJaxbBasicNodeInformationsType neighbourNodeInfo = getNodeInfo(neighbourAdminEndpoint);
	AddNeighBourNode parameters = new AddNeighBourNode();
	parameters.setNeighbourNode(neighbourNodeInfo);
	AdminClientImpl cli = new AdminClientImpl(this.adminEndpoint);
	try {
	    cli.addNeighBourNode(parameters);
	} catch (ManagementException e1) {
	    fail("Adding " + neighbourAdminEndpoint + " as neighbour of " + this.adminEndpoint + " failed.");
	}
    }

    private EJaxbBasicNodeInformationsType getNodeInfo(String adminEndpoint) throws EasyESBException {
	AdminClientImpl client = new AdminClientImpl(adminEndpoint);
	GetNodeInformations nodein = new GetNodeInformations();
	GetNodeInformationsResponse nodeout = null;
	try {
	    nodeout = client.getNodeInformations(nodein);
	} catch (ManagementException e1) {
	    fail("Retrieving information about " + adminEndpoint + " failed");
	}
	EJaxbBasicNodeInformationsType nodeInfo = nodeout.getNode().getBasicNodeInformations();
	return nodeInfo;
    }

    private void fail(String msg) throws EasyESBException {
	logger.error(msg);
	throw new EasyESBException(msg);
    }

    // manual validation
    public static void main(String[] args) throws EasyESBException {
	String nodeAddress = "http://54.226.167.166:8180/services/adminExternalEndpoint";
	String neighbourAddress = "http://54.227.157.80:8180/services/adminExternalEndpoint";
	EasyESBNodeImpl node = new EasyESBNodeImpl(nodeAddress);
	EasyESBNodeImpl neighbour = new EasyESBNodeImpl(neighbourAddress);
	node.addNeighbour(neighbour);
    }

}
