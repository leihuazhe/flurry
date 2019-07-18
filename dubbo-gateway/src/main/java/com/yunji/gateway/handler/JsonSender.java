package com.yunji.gateway.handler;

import com.yunji.gateway.netty.http.request.RequestContext;
import com.yunji.gateway.util.GateWayErrorCode;
import com.yunji.gateway.util.GatewayException;
import io.netty.channel.ChannelHandlerContext;
import org.apache.dubbo.gateway.GateWayService;
import org.apache.dubbo.gateway.ReferenceServiceContext;
import org.apache.dubbo.jsonserializer.metadata.MetadataFetcher;
import org.apache.dubbo.jsonserializer.metadata.OptimizedMetadata;
import org.apache.dubbo.jsonserializer.metadata.tag.Field;
import org.apache.dubbo.rpc.RpcContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class JsonSender {
    private static Supplier<GatewayException> paramsSupplier = () -> {
        throw new GatewayException(GateWayErrorCode.IllegalParams);
    };


    public static CompletableFuture<String> sendAsync(RequestContext context, ChannelHandlerContext ctx) {
        return jsonPostAsync(context, ctx);
    }

    public static CompletableFuture<String> getServiceMetadata(RequestContext context, ChannelHandlerContext ctx) {
        String serviceName = context.service().orElseThrow(paramsSupplier);
        String methodName = context.method().orElseThrow(paramsSupplier);
        String paramsJson = context.parameter().orElseThrow(paramsSupplier);

        //获取指定服务的GateWayService
        GateWayService gateWayService = ReferenceServiceContext.getGateWayService(serviceName);

        String[] parameterTypes = new String[0];


        gateWayService.invoke(methodName, parameterTypes, new Object[]{paramsJson});
        return RpcContext.getContext().getCompletableFuture();
    }

    private static CompletableFuture<String> jsonPostAsync(RequestContext context, ChannelHandlerContext ctx) {
        String serviceName = context.service().orElseThrow(paramsSupplier);
        String methodName = context.method().orElseThrow(paramsSupplier);
        String callService = context.service().orElseThrow(paramsSupplier);
        String paramsJson = context.parameter().orElseThrow(paramsSupplier);

        //获取指定服务的GateWayService
        GateWayService gateWayService = ReferenceServiceContext.getGateWayService(serviceName);

        //获取 parameterTypes 参数
        OptimizedMetadata.OptimizedService service = MetadataFetcher.getService(callService, null);
        List<Field> requestFields = service.getMethodMap().get(methodName).request.fields;

        String[] parameterTypes = new String[requestFields.size()];
        for (int i = 0; i < requestFields.size(); i++) {
            parameterTypes[i] = requestFields.get(i).dataType.qualifiedName;
        }

        gateWayService.invoke(methodName, parameterTypes, new Object[]{paramsJson});
        return RpcContext.getContext().getCompletableFuture();
    }
}
