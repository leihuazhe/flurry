package com.yunji.gateway.handler;

import com.yunji.gateway.netty.http.request.RequestContext;
import com.yunji.gateway.util.GateWayErrorCode;
import com.yunji.gateway.util.GatewayException;
import io.netty.channel.ChannelHandlerContext;
import com.yunji.gateway.core.DubboExecutedFacade;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class GatewayAsyncSender {

    private final DubboExecutedFacade executedFacade;

    private Supplier<GatewayException> paramsSupplier = () -> {
        throw new GatewayException(GateWayErrorCode.IllegalParams);
    };

    public GatewayAsyncSender(String registryUrl, String diamondId) {
        executedFacade = new DubboExecutedFacade(registryUrl, diamondId, true);
    }

    public CompletableFuture<String> sendAsync(RequestContext context, ChannelHandlerContext ctx) {
        return jsonPostAsync(context, ctx);
    }

    private CompletableFuture<String> jsonPostAsync(RequestContext context, ChannelHandlerContext ctx) {
        String serviceName = context.service().orElseThrow(paramsSupplier);
        String methodName = context.method().orElseThrow(paramsSupplier);
        String version = context.version().orElseThrow(paramsSupplier);
        String paramsJson = context.parameter().orElseThrow(paramsSupplier);

        return executedFacade.execute(serviceName, methodName, version, paramsJson);
    }
}
