package com.yunji.flurry.handler;

import com.yunji.flurry.core.Request;
import com.yunji.flurry.netty.http.request.RequestContext;
import com.yunji.flurry.util.GateWayErrorCode;
import com.yunji.flurry.util.FlurryException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import com.yunji.flurry.core.DubboExecutedFacade;
import org.apache.dubbo.config.RpcRequest;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class GatewayAsyncSender {

    private final DubboExecutedFacade executedFacade;

    private Supplier<FlurryException> paramsSupplier = () -> {
        throw new FlurryException(GateWayErrorCode.IllegalParams);
    };

    public GatewayAsyncSender(String registryUrl, String diamondId) {
        executedFacade = new DubboExecutedFacade(registryUrl, diamondId);
    }

    public CompletableFuture<String> sendAsync(RequestContext context, ChannelHandlerContext ctx) {
        return jsonPostAsync(context, ctx);
    }

    private CompletableFuture<String> jsonPostAsync(RequestContext context, ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Request request = buildRequest(context, channel);

        return executedFacade.executeRequest(request);
    }


    private Request buildRequest(RequestContext context, Channel channel) {
        String serviceName = context.service().orElseThrow(paramsSupplier);
        String methodName = context.method().orElseThrow(paramsSupplier);
        String version = context.version().orElseThrow(paramsSupplier);

        String requestContent = context.parameter().orElseThrow(paramsSupplier);

        String requestUrl = context.requestUrl();


        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        String clientIP = socketAddress.getAddress().getHostAddress();
        int clientPort = socketAddress.getPort();

        System.out.println(clientIP + ":" + clientPort);


        Request request = new Request();

        request.setUrl(requestUrl);
        request.setRemoteIp(clientIP);
        request.setRemotePort(clientPort);
        request.setShadow(context.isShadow());

        request.setInterfaceName(serviceName);
        request.setMethodName(methodName);
        request.setVersion(version);
        request.setContent(requestContent);

        return request;
    }
}
