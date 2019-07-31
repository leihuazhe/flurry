package com.yunji.gateway.bootstrap;

import com.yunji.gateway.netty.NettyHttpServer;
import com.yunji.gateway.netty.http.util.Constants;
import com.yunji.gateway.util.GatewayException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.dubbo.metadata.MetadataResolver;
import org.apache.dubbo.metadata.util.MetadataUtil;
import org.apache.dubbo.util.GatewayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Denim.leihz 2019-07-29 2:12 PM
 */
public class GatewayApplication {
    private static final Logger logger = LoggerFactory.getLogger(GatewayApplication.class);

    private MetadataResolver metadataResolver;

    /**
     * Synchronization monitor for the "refresh" and "destroy"
     */
    private final Object startupShutdownMonitor = new Object();

    /**
     * Reference to the JVM shutdown hook, if registered
     */
    private Thread shutdownHook;

    private int port;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private String registryUrl;

    private String banner;


    {
        metadataResolver = GatewayUtil.getSupportedExtension(MetadataResolver.class);
    }


    public void setPort(int port) {
        if (port > 0) {
            this.port = port;
        } else {
            throw new GatewayException("netty server port <= 0 ");
        }
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }


    public void setBanner(String banner) {
        this.banner = banner;
        logger.info(banner);
    }

    public void run() {
        if (registryUrl == null) {
            throw new GatewayException("registryUrl == null");
        }
        // eventGroup
        bossGroup = new NioEventLoopGroup(1,
                new DefaultThreadFactory("netty-server-boss-group", Boolean.TRUE));
        workerGroup = new NioEventLoopGroup(Constants.DEFAULT_IO_THREADS,
                new DefaultThreadFactory("netty-server-worker-group", Boolean.TRUE));

        new NettyHttpServer(port, bossGroup, workerGroup).start();
    }


    public void registerShutdownHook() {
        if (this.shutdownHook == null) {
            // No shutdown hook registered yet.
            this.shutdownHook = new Thread(() -> {
                synchronized (startupShutdownMonitor) {
                    logger.info("ready to shutdown this gateway!");
                    if (bossGroup != null) {
                        bossGroup.shutdownGracefully();
                    }
                    if (workerGroup != null) {
                        workerGroup.shutdownGracefully();
                    }
                    logger.info("end to shutdown this gateway!");
                }
            }, "netty-server-shutdownHook-thread");
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
        }
    }

    public void initMetadata(String dataId) throws Exception {
        MetadataUtil.initMetadata(dataId, registryUrl, metadataResolver);
    }


}
