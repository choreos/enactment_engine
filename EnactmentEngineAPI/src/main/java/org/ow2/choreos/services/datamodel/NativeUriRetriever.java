package org.ow2.choreos.services.datamodel;

import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.services.datamodel.uri.URIContextRetriever;
import org.ow2.choreos.services.datamodel.uri.URIContextRetrieverFactory;

public class NativeUriRetriever {

    private CloudNode node;
    private DeployableServiceSpec spec;
    private String instanceId;

    public NativeUriRetriever(ServiceInstance instance) {
        this.node = instance.getNode();
        this.spec = instance.getServiceSpec();
        this.instanceId = instance.getInstanceId();
    }

    public String getDefaultnativeUri() {
        // interesting note about the ending slash
        // http://www.searchenginejournal.com/to-slash-or-not-to-slash-thats-a-server-header-question/6763/
        if (node.getHostname() == null && node.getIp() == null)
            throw new IllegalStateException("Sorry, I don't know neither the hostname nor the IP yet");
        String uriContext = getUriContext();
        return getUriWithPort(uriContext);
    }

    private String getUriContext() {
        PackageType packageType = spec.getPackageType();
        URIContextRetriever uriContextRetriever = URIContextRetrieverFactory.getNewInstance(packageType);
        return uriContextRetriever.retrieveURI(spec, instanceId);
    }

    private String getUriWithPort(String uriContext) {
        int port = spec.getPort();
        if (node.getIp() != null && !node.getIp().isEmpty()) {
            return "http://" + node.getIp() + ":" + port + "/" + uriContext;
        } else
            return "http://" + node.getHostname() + ":" + port + "/" + uriContext;
    }

}
