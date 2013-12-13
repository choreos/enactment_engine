package org.ow2.choreos;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class AirlineClientFactory {

    private static final String NAMESPACE = "http://choreos.ow2.org/";
    private static final String SERVICE_NAME = "AirlineServiceService";
    
    private String wsdl;
    private Airline client;

    public AirlineClientFactory(String wsdl) {
        this.wsdl = wsdl;
    }

    /**
     * 
     * @return a cached client since building the client is a heavy operation.
     * @throws MalformedURLException 
     */
    public Airline getCLient() throws MalformedURLException {
        if (client == null) {
            synchronized (this) {
                if (client == null)
                    makeNewInstance();
            }
        }
        return client;
    }

    private void makeNewInstance() throws MalformedURLException {
        final QName qname = new QName(NAMESPACE, SERVICE_NAME);
        final URL url = new URL(wsdl);
        Service service = Service.create(url, qname);
        client = service.getPort(Airline.class);
    }

}
