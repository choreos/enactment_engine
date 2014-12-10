package org.ow2.choreos.services.datamodel;

public class PortRetriever {

    private static final int TOMCAT_PORT = 8080;

    public int getPortByPackageType(PackageType packageType) {
        if (packageType.equals(PackageType.TOMCAT))
            return TOMCAT_PORT;
        else
            throw new IllegalArgumentException("Port not known for package type " + packageType);        
    }

}
