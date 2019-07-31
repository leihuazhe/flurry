package com.yunji.gateway.handler;

import com.yunji.gateway.netty.http.request.RequestContext;
import com.yunji.gateway.util.GateWayErrorCode;
import com.yunji.gateway.util.GatewayException;
import io.netty.channel.ChannelHandlerContext;
import org.apache.dubbo.gateway.core.DubboAsyncClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class GatewayAsyncSender {

    private static DubboAsyncClient client = new DubboAsyncClient();

    private static Supplier<GatewayException> paramsSupplier = () -> {
        throw new GatewayException(GateWayErrorCode.IllegalParams);
    };

    public static CompletableFuture<String> sendAsync(RequestContext context, ChannelHandlerContext ctx) {
        return jsonPostAsync(context, ctx);
    }

    private static CompletableFuture<String> jsonPostAsync(RequestContext context, ChannelHandlerContext ctx) {
        String serviceName = context.service().orElseThrow(paramsSupplier);
        String methodName = context.method().orElseThrow(paramsSupplier);
        String version = context.version().orElseThrow(paramsSupplier);
        String paramsJson = context.parameter().orElseThrow(paramsSupplier);

        return client.execute(serviceName, methodName, version, paramsJson);
    }
}
