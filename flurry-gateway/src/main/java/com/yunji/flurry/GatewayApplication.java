package com.yunji.flurry;

import com.yunji.flurry.bootstrap.FlurryApplicationBuilder;
import com.yunji.flurry.util.MixUtils;

import static com.yunji.flurry.util.GateConstants.*;

/**
 * Entry point.
 */
public class GatewayApplication {

    public static void main(String[] args) throws Exception {

        new FlurryApplicationBuilder()
                .registryUrl(MixUtils.getZkAddress())
                .diamondId(MixUtils.get(DATA_ID_KEY, DEFAULT_DATA_ID))
                .registerShutdownHook()
                .logLogBanner()
                //加载配置文件
                .loadProperties()
                .serverPort(9000)
                .start();
    }
}
