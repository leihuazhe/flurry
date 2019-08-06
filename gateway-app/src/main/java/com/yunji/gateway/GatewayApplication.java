package com.yunji.gateway;

import com.yunji.gateway.bootstrap.GatewayApplicationBuilder;
import org.apache.dubbo.util.EnvUtil;

import static org.apache.dubbo.util.GateConstants.*;

/**
 * Entry point.
 */
public class GatewayApplication {

    public static void main(String[] args) throws Exception {

        new GatewayApplicationBuilder()
                .registryUrl(EnvUtil.getZkAddress())
                .diamondId(EnvUtil.get(DATA_ID_CONSTANT, DEFAULT_DATA_ID))
                .registerShutdownHook()
                .logLogBanner()
                .serverPort(9000)
                .start();
    }
}
