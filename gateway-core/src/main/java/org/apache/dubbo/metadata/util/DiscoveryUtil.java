package org.apache.dubbo.metadata.util;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.metadata.whitelist.ConfigContext;

/**
 * @author Denim.leihz 2019-07-23 6:28 PM
 */
public class DiscoveryUtil {

    public static URL createRegistryUrl(ConfigContext context) {
        String registryUrl = context.getRegistryUrl();
        String sb = "zookeeper://" + registryUrl + "/org.apache.services.registry.RegistryService?services=2.0.2&interface=org.apache.services.registry.RegistryService";

        return URL.valueOf(sb);
    }
}
