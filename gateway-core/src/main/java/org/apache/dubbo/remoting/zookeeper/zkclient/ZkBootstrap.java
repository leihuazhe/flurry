//package org.apache.services.remoting.zookeeper.zkclient;
//
//import org.apache.services.util.PropertyUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import static org.apache.services.util.GateConstants.*;
//
///**
// * @author Denim.leihz 2019-07-22 5:30 PM
// */
//public class ZkBootstrap {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ZkBootstrap.class);
//
//    private ZookeeperClientDiscovery zookeeperClient;
//
//
//    public void init() {
//        String zkHost = PropertyUtils.getProperty(REGISTRY_URL_CONSTANT, DEFAULT_REGISTRY_URL);
//        zookeeperClient = new ZookeeperClientDiscovery(zkHost);
//        zookeeperClient.init(false);
//    }
//}
