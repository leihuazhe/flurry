package com.yunji.flurry.netty;

import com.yunji.flurry.handler.HttpRequestHandler;
import com.yunji.flurry.handler.ServerProcessHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;


/**
 * @author maple NettyHttpServer
 * @since 2018年08月23日 上午9:54
 */
public class NettyHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    private String registryUrl;

    private String diamondId;

    private final int port;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public NettyHttpServer(int port, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.port = port;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
    }

    /**
     * start
     */
    public void start() {
        // sharable handler
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
        ServerProcessHandler serverProcessHandler = new ServerProcessHandler(registryUrl, diamondId);

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

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    public void setDiamondId(String diamondId) {
        this.diamondId = diamondId;
    }
}
