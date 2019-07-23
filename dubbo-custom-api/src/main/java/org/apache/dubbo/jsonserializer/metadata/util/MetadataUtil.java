package org.apache.dubbo.jsonserializer.metadata.util;

import org.apache.dubbo.jsonserializer.metadata.tag.Service;

/**
 * @author Denim.leihz 2019-07-23 6:06 PM
 */
public class MetadataUtil {

    public static String getServiceKey(Service service) {
        return getServiceKey(service.getName(), service.getMeta().version);
    }

    public static String getServiceFullNameKey(Service service) {
        return getServiceKey(service.getNamespace() + "." + service.getName(), service.getMeta().version);
    }

    public static String getServiceKey(String name, String version) {
        return name + ":" + version;
    }
}
