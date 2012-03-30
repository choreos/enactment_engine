package eu.choreos.nodepoolmanager.chef;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import eu.choreos.nodepoolmanager.Configuration;

/**
 * Provide access to Chef functionalities using scripts with chef commands
 * 
 *
 */
public class ChefScripts {
	
	private static final String CHEF_NODE_LIST = "chef/chef_node_list.sh";
	private static final String CHEF_BOOTSTRAP_SCRIPT = "chef/chef_bootstrap.sh";
	private static final String CHEF_ADD_COOKBOOK_SCRIPT = "chef/chef_add_cookbook.sh";
	private static final String CHEF_NAME_SCRIPT = "chef/my_chef_name.sh";
	private static final String CHEF_NODE_SHOW = "chef/chef_node_show.sh";
	private static final String CHEF_NODE_DELETE = "chef/chef_node_delete.sh";
	private static final String CHEF_CLIENT_DELETE = "chef/chef_client_delete.sh";
	private static final String CHEF_UPLOAD_COOKBOOK = "chef/chef_upload_cookbook.sh";
	
    public static String getChefBootstrapScript(String pKeyFile, String ip, String user) {
    	
    	URL scriptFile = ClassLoader.getSystemResource(CHEF_BOOTSTRAP_SCRIPT);
        String command = null;
		try {
			command = FileUtils.readFileToString(new File(scriptFile.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Should not happen");
		}

        String config = Configuration.get("CHEF_CONFIG_FILE");

        command = command.replace("$privateKeyFile", pKeyFile);
        command = command.replace("$ip", ip);
        command = command.replace("$user", user);
        command = command.replace("$knifeFile", config);
        
        return command;
    }
    
    public static String getChefAddCookbook(String nodeName, String cookbook, String recipe) {
    	
    	URL scriptFile = ClassLoader.getSystemResource(CHEF_ADD_COOKBOOK_SCRIPT);
    	String command = null;
		try {
			command = FileUtils.readFileToString(new File(scriptFile.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Should not happen");
		}

        String config = Configuration.get("CHEF_CONFIG_FILE");

    	command = command.replace("$nodeName", nodeName);
    	command = command.replace("$cookbook", cookbook);
    	command = command.replace("$recipe", recipe);
    	command = command.replace("$knifeFile", config);

    	return command;
    }
    
    public static String getChefNodeList() {
    	
    	URL scriptFile = ClassLoader.getSystemResource(CHEF_NODE_LIST);
    	String command = null;
		try {
			command = FileUtils.readFileToString(new File(scriptFile.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Should not happen");
		}

        String config = Configuration.get("CHEF_CONFIG_FILE");
    	command = command.replace("$knifeFile", config);

    	return command;
    }
    
    public static String getChefNodeShow(String nodeName) {
    	
    	URL scriptFile = ClassLoader.getSystemResource(CHEF_NODE_SHOW);
    	String command = null;
		try {
			command = FileUtils.readFileToString(new File(scriptFile.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Should not happen");
		}

        String config = Configuration.get("CHEF_CONFIG_FILE");
    	command = command.replace("$knifeFile", config);
    	command = command.replace("$nodeName", nodeName);

    	return command;
    }
    
    public static String getChefName() {
    	
    	URL scriptFile = ClassLoader.getSystemResource(CHEF_NAME_SCRIPT);
    	String command = null;
		try {
			command = FileUtils.readFileToString(new File(scriptFile.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Should not happen");
		}
    	return command;
    }
    
    public static String getDeleteNode(String nodeName) {
    	
    	URL scriptFile = ClassLoader.getSystemResource(CHEF_NODE_DELETE);
    	String command = null;
		try {
			command = FileUtils.readFileToString(new File(scriptFile.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Should not happen");
		}

        String config = Configuration.get("CHEF_CONFIG_FILE");
    	command = command.replace("$knifeFile", config);
    	command = command.replace("$nodeName", nodeName);

    	return command;
    }

    public static String getDeleteClient(String clientName) {
    	
		URL scriptFile = ClassLoader.getSystemResource(CHEF_CLIENT_DELETE);
    	String command = null;
		try {
			command = FileUtils.readFileToString(new File(scriptFile.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Should not happen");
		}

        String config = Configuration.get("CHEF_CONFIG_FILE");
    	command = command.replace("$knifeFile", config);
    	command = command.replace("$clientName", clientName);

    	return command;
    }
    
    public static String getUploadCookbook(String cookbookName, String cookbookParentFolder) {
    	
		URL scriptFile = ClassLoader.getSystemResource(CHEF_UPLOAD_COOKBOOK);
    	String command = null;
		try {
			command = FileUtils.readFileToString(new File(scriptFile.getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Should not happen");
		}

        String config = Configuration.get("CHEF_CONFIG_FILE");
    	command = command.replace("$knifeFile", config);
    	command = command.replace("$cookbookName", cookbookName);
    	command = command.replace("$cookbookParentFolder", cookbookParentFolder);
    	
    	return command;
    }
}