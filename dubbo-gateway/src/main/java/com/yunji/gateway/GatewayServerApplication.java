package com.yunji.gateway;

import com.yunji.gateway.netty.NettyHttpServer;
import org.apache.dubbo.jsonserializer.metadata.MetadataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point.
 */
public class GatewayServerApplication {
    private static Logger logger = LoggerFactory.getLogger(GatewayServerApplication.class);

    public static void main(String[] args) throws Exception {
        NettyHttpServer server = new NettyHttpServer(9001);
        //初始化元数据信息.
        MetadataFetcher.init();
        logLogBanner();
        server.registerShutdownHook();
        server.start();
    }

    private static void logLogBanner() {
        String banner =
                "\n\n __   __  _   _   _   _       _   ___     ____    _   _   ____    ____     ___       ____      _      _____   _____  __        __     _     __   __\n" +
                        " \\ \\ / / | | | | | \\ | |     | | |_ _|   |  _ \\  | | | | | __ )  | __ )   / _ \\     / ___|    / \\    |_   _| | ____| \\ \\      / /    / \\    \\ \\ / /\n" +
                        "  \\ V /  | | | | |  \\| |  _  | |  | |    | | | | | | | | |  _ \\  |  _ \\  | | | |   | |  _    / _ \\     | |   |  _|    \\ \\ /\\ / /    / _ \\    \\ V / \n" +
                        "   | |   | |_| | | |\\  | | |_| |  | |    | |_| | | |_| | | |_) | | |_) | | |_| |   | |_| |  / ___ \\    | |   | |___    \\ V  V /    / ___ \\    | |  \n" +
                        "   |_|    \\___/  |_| \\_|  \\___/  |___|   |____/   \\___/  |____/  |____/   \\___/     \\____| /_/   \\_\\   |_|   |_____|    \\_/\\_/    /_/   \\_\\   |_|  \n" +
                        "                                                                                                                                                   \n";
        logger.info(banner);
    }
}
