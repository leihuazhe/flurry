package com.yunji.gateway;

import com.yunji.gateway.bootstrap.GatewayApplicationBuilder;
import com.yunji.gateway.util.MixUtils;

import static com.yunji.gateway.util.GateConstants.*;

/**
 * Entry point.
 */
public class GatewayApplication {

    public static void main(String[] args) throws Exception {

        new GatewayApplicationBuilder()
                .registryUrl(MixUtils.getZkAddress())
                .diamondId(MixUtils.get(DATA_ID_KEY, DEFAULT_DATA_ID))
                .registerShutdownHook()
                .logLogBanner()
                .serverPort(9000)
                .start();
    }
}
