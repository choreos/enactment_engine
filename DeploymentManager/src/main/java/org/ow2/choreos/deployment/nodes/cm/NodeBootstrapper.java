package org.ow2.choreos.deployment.nodes.cm;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.choreos.chef.ChefNodeNameRetriever;
import org.ow2.choreos.chef.Knife;
import org.ow2.choreos.chef.KnifeException;
import org.ow2.choreos.chef.impl.KnifeImpl;
import org.ow2.choreos.deployment.Configuration;
import org.ow2.choreos.nodes.NodeNotAccessibleException;
import org.ow2.choreos.nodes.datamodel.Node;
import org.ow2.choreos.utils.RemoteFileWriter;
import org.ow2.choreos.utils.SshCommandFailed;
import org.ow2.choreos.utils.SshNotConnected;
import org.ow2.choreos.utils.SshUtil;
import org.ow2.choreos.utils.SshWaiter;

import com.jcraft.jsch.JSchException;

/**
 * 
 * @author leonardo, cadu, felps
 *
 */
public class NodeBootstrapper {
	
	public static final int SSH_TIMEOUT_IN_SECONDS = 250;
    private static final String CHEF_REPO = Configuration.get("CHEF_REPO");
    private static final String CHEF_CONFIG_FILE = Configuration.get("CHEF_CONFIG_FILE");
	private static final String BOOTSTRAP_LOG_FILE_LOCATION = "/tmp/bootstrap.log";

	private int sshTimeoutInSeconds;
	
	private Logger logger = Logger.getLogger(NodeBootstrapper.class);
    
    private Node node;
    
    public NodeBootstrapper(Node node) {
    	this.node = node;
    	this.sshTimeoutInSeconds = SSH_TIMEOUT_IN_SECONDS;
    }
    
    NodeBootstrapper(Node node, int sshTimeoutInSeconds) {
    	this.node = node;
    	this.sshTimeoutInSeconds = sshTimeoutInSeconds;
    }
    
    public void bootstrapNode() throws NodeNotAccessibleException, KnifeException, NodeNotBootstrappedException {

        SshWaiter sshWaiter = new SshWaiter();
        try {
			sshWaiter.waitSsh(this.node.getIp(), 
					this.node.getUser(), this.node.getPrivateKeyFile(), this.sshTimeoutInSeconds);
		} catch (SshNotConnected e) {
			throw new NodeNotAccessibleException(this.node.getIp() + " not accessible");
		}

    	logger.info("Bootstrapping " + this.node.getIp());
    	Knife knife = new KnifeImpl(CHEF_CONFIG_FILE, CHEF_REPO);
    	
    	configureHarakiri("http://172.16.239.10:9100/deploymentmanager/", this.node.getChefName());
    	
		String bootstrapLog = knife.bootstrap(this.node.getIp(), this.node.getUser(), this.node.getPrivateKeyFile(), DefaultRecipes.getDefaultRecipes());
		logger.debug("remote Bootstrap log: " + bootstrapLog);
		saveLogOnNode(bootstrapLog);
		logger.info("Bootstrap completed at " + this.node);
		this.retrieveAndSetChefName(bootstrapLog);
		
		NodeChecker checker = new NodeChecker();
		if (!checker.checkNodeOnNodesList(node)) {
			String msg = "Node " + node.getId() + " not bootstrapped";
			logger.error(msg);
			throw new NodeNotBootstrappedException(msg);
		}
    }
    
    private void configureHarakiri(String deploymentManagerURL, String nodeId) {
    	String  chef_repo_path = Configuration.get(CHEF_REPO);
    	try {
			FileUtils.deleteDirectory(new File(chef_repo_path+"/harakiri"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	File chef_repo_folder = new File(chef_repo_path);
    	
    	String harakiri_template_path = "src/main/resources/chef/harakiri";
    	File harakiri_template_folder = new File(harakiri_template_path);
    	
    	try {
			FileUtils.copyDirectoryToDirectory(harakiri_template_folder, chef_repo_folder);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	// .... create a new recipe builder
    }

	private void saveLogOnNode(String bootstrapLog) {

		SshUtil ssh = new SshUtil(this.node.getIp(), this.node.getUser(), this.node.getPrivateKeyFile());
		RemoteFileWriter remoteFileWriter = new RemoteFileWriter();
		
		try {
			remoteFileWriter.writeFile(bootstrapLog, BOOTSTRAP_LOG_FILE_LOCATION, ssh);
		} catch (SshCommandFailed e) {
			logger.error("Could not create the bootstrap log");
		}
		
		ssh.disconnect();
	}

	public void retrieveAndSetChefName(String bootstrapLog) {
        
    	ChefNodeNameRetriever nameRetriever = new ChefNodeNameRetriever();
        String chefNodeName = null;
        
        try {
        	chefNodeName = nameRetriever.retrieveChefNodeNameFromBootstrapLog(bootstrapLog);
        } catch (IllegalArgumentException e1) {
			try {
				logger.debug("Going to retrieve chef name by running retriever script on node...");
				chefNodeName = nameRetriever.getChefNodeName(this.node.getIp(), 
						this.node.getUser(), this.node.getPrivateKeyFile());
			} catch (JSchException e) {
				chefNodeName = this.node.getHostname();
			} catch (SshCommandFailed e) {
				chefNodeName = this.node.getHostname();
			}
        }
		
		logger.debug("Retrieved chef name: " + chefNodeName);
		this.node.setChefName(chefNodeName);
    }
	

}
