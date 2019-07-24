package org.apache.dubbo.jsonserializer.metadata.discovery;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.jsonserializer.metadata.discovery.curator.CuratorClientDiscovery;
import org.apache.dubbo.jsonserializer.metadata.discovery.zkclient.ZookeeperClientDiscovery;
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

    public CuratorClientDiscovery createRegistry(URL url) {
        return new CuratorClientDiscovery(url, zookeeperTransporter);
    }

    public ZookeeperClientDiscovery createOriginalRegistry(URL url) {
        return new ZookeeperClientDiscovery(url);
    }
}
