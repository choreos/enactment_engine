package org.ow2.choreos.services.datamodel.uri;

import java.util.HashMap;
import java.util.Map;

import org.ow2.choreos.services.datamodel.PackageType;

public class URIContextRetrieverFactory {

    private static Map<String, Class<? extends URIContextRetriever>> classMap;

    static {
        classMap = new HashMap<String, Class<? extends URIContextRetriever>>();
        classMap.put(PackageType.COMMAND_LINE.toString(), JarURIContextRetriever.class);
        classMap.put(PackageType.TOMCAT.toString(), WarURIContextRetriever.class);
        classMap.put(PackageType.EASY_ESB.toString(), CDURIContextRetriever.class);
    }

    public static URIContextRetriever getNewInstance(PackageType packageType) {
        return getNewInstance(packageType.toString());
    }

    public static URIContextRetriever getNewInstance(String packageType) {
        Class<? extends URIContextRetriever> clazz = classMap.get(packageType);
        URIContextRetriever uriContextRetriever = null;
        try {
            uriContextRetriever = clazz.newInstance();
        } catch (InstantiationException e) {
            creationFailed(packageType);
        } catch (IllegalAccessException e) {
            creationFailed(packageType);
        }
        return uriContextRetriever;
    }

    private static void creationFailed(String type) {
        throw new IllegalStateException("Invalid package type when retrieving URIContextRetriever: " + type);
    }

}
