package com.yunji.gateway.netty;

import com.yunji.gateway.netty.http.util.Constants;
import com.yunji.gateway.handler.HttpRequestHandler;
import com.yunji.gateway.handler.ServerProcessHandler;
import com.yunji.gateway.util.GatewayException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.dubbo.jsonserializer.metadata.MetadataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author maple NettyHttpServer
 * @since 2018年08月23日 上午9:54
 */
public class NettyHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    /**
     * Synchronization monitor for the "refresh" and "destroy"
     */
    private final Object startupShutdownMonitor = new Object();

    /**
     * Reference to the JVM shutdown hook, if registered
     */
    private Thread shutdownHook;

    private final int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private String registryUrl;


    public NettyHttpServer(int port) {
        this.port = port > 0 ? port : 0;
    }

    public NettyHttpServer registryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
        return this;
    }

    /**
     * start
     */
    public void start() {
        if (registryUrl == null) {
            throw new GatewayException("registryUrl == null");
        }
        // eventGroup
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("netty-server-boss-group", Boolean.TRUE));
        workerGroup = new NioEventLoopGroup(Constants.DEFAULT_IO_THREADS, new DefaultThreadFactory("netty-server-worker-group", Boolean.TRUE));

        // sharable handler
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
        ServerProcessHandler serverProcessHandler = new ServerProcessHandler();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline ph = ch.pipeline();
                            //处理http服务的关键handler
                            ph.addLast("encoder", new HttpResponseEncoder());
                            ph.addLast("decoder", new HttpRequestDecoder());
                            ph.addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
                            // 服务端业务逻辑
                            ph.addLast("requestHandler", httpRequestHandler);
                            ph.addLast("serverHandler", serverProcessHandler);
                        }

                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                    .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);


            ChannelFuture future = bootstrap.bind(port).sync();

            logger.info("NettyServer start listen at {}", port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public NettyHttpServer registerShutdownHook() {
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

        return this;
    }

    public NettyHttpServer logLogBanner() {
        String banner =
                "\n\n __   __  _   _   _   _       _   ___     ____    _   _   ____    ____     ___       ____      _      _____   _____  __        __     _     __   __\n" +
                        " \\ \\ / / | | | | | \\ | |     | | |_ _|   |  _ \\  | | | | | __ )  | __ )   / _ \\     / ___|    / \\    |_   _| | ____| \\ \\      / /    / \\    \\ \\ / /\n" +
                        "  \\ V /  | | | | |  \\| |  _  | |  | |    | | | | | | | | |  _ \\  |  _ \\  | | | |   | |  _    / _ \\     | |   |  _|    \\ \\ /\\ / /    / _ \\    \\ V / \n" +
                        "   | |   | |_| | | |\\  | | |_| |  | |    | |_| | | |_| | | |_) | | |_) | | |_| |   | |_| |  / ___ \\    | |   | |___    \\ V  V /    / ___ \\    | |  \n" +
                        "   |_|    \\___/  |_| \\_|  \\___/  |___|   |____/   \\___/  |____/  |____/   \\___/     \\____| /_/   \\_\\   |_|   |_____|    \\_/\\_/    /_/   \\_\\   |_|  \n" +
                        "                                                                                                                                                   \n";
        logger.info(banner);

        return this;
    }

    public NettyHttpServer initMetadata() throws Exception {
        MetadataFetcher.init();
        return this;
    }
}
