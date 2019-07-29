package com.yunji.gateway;

import com.yunji.gateway.bootstrap.GatewayApplicationBuilder;
import org.apache.dubbo.util.PropertyUtils;

import static org.apache.dubbo.util.GateConstants.*;

/**
 * Entry point.
 */
public class GatewayApplication {

    public static void main(String[] args) throws Exception {
        new GatewayApplicationBuilder()
                .registryUrl(PropertyUtils.getProperty(REGISTRY_URL, DEFAULT_REGISTRY_URL))
                .registerShutdownHook()
                .logLogBanner()
                .serverPort(9001)
                .initMetadata(PropertyUtils.getProperty(DATA_ID_CONSTANT, DEFAULT_DATA_ID))
                .start();


    }
}
