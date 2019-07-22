package com.yunji.gateway;

import com.yunji.gateway.netty.NettyHttpServer;
import com.yunji.gateway.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.dubbo.util.GateConstants.*;

/**
 * Entry point.
 */
public class GatewayServerApplication {
    private static Logger logger = LoggerFactory.getLogger(GatewayServerApplication.class);

    /*public static void main(String[] args) throws Exception {
        NettyHttpServer server = new NettyHttpServer(9001);
        //初始化元数据信息.
        MetadataFetcher.init();
        logLogBanner();
        server.registerShutdownHook();
        server.start();
    }*/

    public static void main(String[] args) throws Exception {
        new NettyHttpServer(9001)
                .registryUrl(PropertyUtils.getProperty(REGISTRY_URL, DEFAULT_REGISTRY_URL))
                .registerShutdownHook()
                .logLogBanner()
                .initMetadata()
                .start();
    }
}
