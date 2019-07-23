package com.yunji.gateway.handler;

import com.yunji.gateway.netty.http.request.RequestContext;
import com.yunji.gateway.util.GateWayErrorCode;
import com.yunji.gateway.util.GatewayException;
import io.netty.channel.ChannelHandlerContext;
import org.apache.dubbo.gateway.GateWayService;
import org.apache.dubbo.gateway.GatewayServiceFactory;
import org.apache.dubbo.gateway.RestServiceConfig;
import org.apache.dubbo.jsonserializer.metadata.ServiceMetadataRepository;
import org.apache.dubbo.jsonserializer.metadata.OptimizedMetadata;
import org.apache.dubbo.jsonserializer.metadata.tag.Field;
import org.apache.dubbo.rpc.RpcContext;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class JsonSender {
    private static ServiceMetadataRepository repository = ServiceMetadataRepository.getRepository();

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
        String callService = context.service().orElseThrow(paramsSupplier);
        String paramsJson = context.parameter().orElseThrow(paramsSupplier);

        //获取指定服务的GateWayService
        GateWayService gateWayService = GatewayServiceFactory.create(buildRestConfig(serviceName, version, null));

        //获取 parameterTypes 参数
        OptimizedMetadata.OptimizedService service = repository.getService(callService, null);
        List<Field> requestFields = service.getMethodMap().get(methodName).request.fields;

        String[] parameterTypes = new String[requestFields.size()];
        for (int i = 0; i < requestFields.size(); i++) {
            parameterTypes[i] = requestFields.get(i).dataType.qualifiedName;
        }

        gateWayService.invoke(methodName, parameterTypes, new Object[]{paramsJson});
        return RpcContext.getContext().getCompletableFuture();
    }


    /**
     * 获取服务元数据信息
     */
    public static CompletableFuture<String> getServiceMetadata(RequestContext context, ChannelHandlerContext ctx) {
        String serviceName = context.service().orElseThrow(paramsSupplier);
        String methodName = context.method().orElseThrow(paramsSupplier);
        String version = context.version().orElseThrow(paramsSupplier);

        //获取指定服务的GateWayService
        GateWayService gateWayService = GatewayServiceFactory.create(buildRestConfig(serviceName, version, null));

        String[] parameterTypes = new String[0];

        gateWayService.invoke(methodName, parameterTypes, new Object[]{});

        return RpcContext.getContext().getCompletableFuture();
    }

    private static RestServiceConfig buildRestConfig(String serviceName, String version, String group) {

        return new RestServiceConfig(serviceName, version, group, new HashMap<>());
    }
}
