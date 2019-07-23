package com.yunji.gateway;


import org.apache.dubbo.common.URL;
import org.apache.dubbo.jsonserializer.metadata.discovery.ZookeeperDiscovery;
import org.apache.dubbo.jsonserializer.metadata.discovery.ZookeeperDiscoveryFactory;

/**
 * @author Denim.leihz 2019-07-23 2:27 PM
 */
public class Main {
    private static String url = "zookeeper://127.0.0.1:2181/com.alibaba.dubbo.registry.RegistryService?application=demo-provider&dubbo=2.0.2&interface=com.alibaba.dubbo.registry.RegistryService&pid=21149&qos.port=22222&timestamp=1563858591252";
//    private static String url = "zookeeper://172.30.221.4:2181,172.30.221.4:2182,172.30.221.4:2183/com.alibaba.dubbo.registry.RegistryService?application=demo-provider&dubbo=2.0.2&interface=com.alibaba.dubbo.registry.RegistryService&pid=21149&qos.port=22222&timestamp=1563858591252";

    public static void main(String[] args) {
        ZookeeperDiscoveryFactory zookeeperDiscoveryFactory = new ZookeeperDiscoveryFactory();
        ZookeeperDiscovery registry = (ZookeeperDiscovery) zookeeperDiscoveryFactory.createRegistry(URL.valueOf(url));
        registry.loadAllService();
    }
}
