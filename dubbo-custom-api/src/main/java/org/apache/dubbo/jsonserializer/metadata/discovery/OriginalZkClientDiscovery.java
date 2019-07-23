package org.apache.dubbo.jsonserializer.metadata.discovery;


import org.apache.dubbo.jsonserializer.metadata.discovery.zkclient.OriginalZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Denim.leihz 2019-07-23 8:33 PM
 */
public class OriginalZkClientDiscovery {

    private static Logger logger = LoggerFactory.getLogger(ZookeeperDiscovery.class);

    private final static String DEFAULT_ROOT = "dubbo";

    private final String root = "/dubbo";

    private final OriginalZkClient zkClient;


    public OriginalZkClientDiscovery() {
        zkClient = new OriginalZkClient("127.0.0.1:2181");
    }

    public void loadAllService() {
        zkClient.getChildrenForWatcher(root);
    }


}
