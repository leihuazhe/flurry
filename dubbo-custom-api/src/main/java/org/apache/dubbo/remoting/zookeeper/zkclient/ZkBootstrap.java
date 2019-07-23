//package org.apache.dubbo.remoting.zookeeper.zkclient;
//
//import org.apache.dubbo.util.PropertyUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import static org.apache.dubbo.util.GateConstants.*;
//
///**
// * @author Denim.leihz 2019-07-22 5:30 PM
// */
//public class ZkBootstrap {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ZkBootstrap.class);
//
//    private OriginalZkClient zookeeperClient;
//
//
//    public void init() {
//        String zkHost = PropertyUtils.getProperty(REGISTRY_URL, DEFAULT_REGISTRY_URL);
//        zookeeperClient = new OriginalZkClient(zkHost);
//        zookeeperClient.init(false);
//    }
//}
