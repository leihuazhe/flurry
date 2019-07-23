package org.apache.dubbo.jsonserializer.metadata.util;

import org.apache.dubbo.common.URL;

/**
 * @author Denim.leihz 2019-07-23 6:28 PM
 */
public class DiscoveryUtil {

    public static URL createRegistryUrl(String registryUrl) {
        String sb = "zookeeper://" + registryUrl + "/com.alibaba.dubbo.registry.RegistryService?dubbo=2.0.2&interface=com.alibaba.dubbo.registry.RegistryService";
        return URL.valueOf(sb);
    }
}
