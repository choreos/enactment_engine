package org.ow2.choreos.ee.context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class NamespaceParser {
    
    public String getNamespaceFrom(final String endpoint) throws XMLStreamException, IOException {

        final String wsdl = getWsdl(endpoint);
        final URL url = new URL(wsdl);
        final InputStreamReader streamReader = new InputStreamReader(url.openStream());
        final BufferedReader wsdlInputStream = new BufferedReader(streamReader);
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        final XMLEventReader reader = xmlInputFactory.createXMLEventReader(wsdlInputStream);

        String elementName, namespace = "";
        XMLEvent event;
        StartElement element;

        while (reader.hasNext()) {
            event = reader.nextEvent();
            if (event.isStartElement()) {
                element = event.asStartElement();
                elementName = element.getName().getLocalPart();
                if ("definitions".equals(elementName)) {
                    final QName qname = new QName("targetNamespace"); // NOPMD
                    namespace = element.getAttributeByName(qname).getValue();
                    break;
                }
            }
        }

        reader.close();

        return namespace;
    }

    private String getWsdl(final String endpoint) {
        String slashLess;
        if (endpoint.endsWith("/")) {
            slashLess = endpoint.substring(0, endpoint.length() - 1);
        } else {
            slashLess = endpoint;
        }

        return slashLess + "?wsdl";
    }
    
    public static void main(String[] args) throws Exception {
        NamespaceParser parser = new NamespaceParser();
//        String endpoint = "http://0.0.0.0:1234/airline";
        String endpoint = "http://localhost:8080/CD-client-shopentrance/CD-client-shopentrance";
        String namespace = parser.getNamespaceFrom(endpoint);
        System.out.println(namespace);
    }

}
