package org.apache.dubbo.jsonserializer.metadata.discovery;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.remoting.zookeeper.ZookeeperTransporter;

/**
 * ZookeeperDiscoveryFactory
 *
 * @author Denim.leihz 2019-07-23 2:09 PM
 */
public class ZookeeperDiscoveryFactory {

    private ZookeeperTransporter zookeeperTransporter = ExtensionLoader
            .getExtensionLoader(ZookeeperTransporter.class)
            .getAdaptiveExtension();

    public ZookeeperDiscovery createRegistry(URL url) {
        return new ZookeeperDiscovery(url, zookeeperTransporter);
    }
}
