package com.yunji.flurry;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.zookeeper.ChildListener;
import org.apache.dubbo.remoting.zookeeper.ZookeeperClient;
import org.apache.dubbo.remoting.zookeeper.curator.CuratorZookeeperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Denim.leihz 2019-07-23 11:07 AM
 */
public class MyCuratorClient {
    private static Logger logger = LoggerFactory.getLogger(MyCuratorClient.class);

    private static String urls = "zookeeper://127.0.0.1:2181/com.alibaba.services.registry.RegistryService?application=demo-provider&services=2.0.2&interface=com.alibaba.services.registry.RegistryService&pid=21149&qos.port=22222&timestamp=1563858591252";

    private final ZookeeperClient zkClient;

    public MyCuratorClient(URL url) {
        this.zkClient = new CuratorZookeeperClient(url);
    }

    public static void main(String[] args) throws InterruptedException {
        logger.info("Begin to test zookeeper .");
        CountDownLatch latch = new CountDownLatch(1);
        URL url = URL.valueOf(urls);
        MyCuratorClient myCuratorClient = new MyCuratorClient(url);

//        myCuratorClient.subscribe("/services/com.maple.services.api.DemoService/providers");
        myCuratorClient.subscribe("/leihz");

        latch.await();
    }


    public void subscribe(String path) {

        zkClient.addChildListener(path, new ChildListener() {

            @Override
            public void childChanged(String path, List<String> currentChilds) {
                for (String child : currentChilds) {
                    child = URL.decode(child);

                    logger.info("Parent path: {}, child: {}", path, child);

                   /* subscribe(url.setPath(child).addParameters(RegistryConstants.INTERFACE_KEY, child,
                            RegistryConstants.CHECK_KEY, String.valueOf(false)), listener);*/
                }
                System.out.println("path change: " + path);
            }
        });


    }
}
