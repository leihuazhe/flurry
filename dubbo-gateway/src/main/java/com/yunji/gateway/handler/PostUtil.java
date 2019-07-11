package com.yunji.gateway.handler;

import com.yunji.gateway.service.GateWayService;
import com.yunji.gateway.netty.http.request.RequestContext;
import com.yunji.gateway.service.ReferenceServiceContext;
import com.yunji.gateway.util.HttpHandlerUtil;
import com.yunji.metadata.MetadataFetcher;
import com.yunji.metadata.OptimizedMetadata;
import com.yunji.metadata.tag.Field;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.RpcContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PostUtil {
    private static Logger logger = LoggerFactory.getLogger(PostUtil.class);

    public static CompletableFuture<String> postAsync(RequestContext context, ChannelHandlerContext ctx) {
        return jsonPostAsync(context, ctx);
    }

    public static String post(RequestContext context, ChannelHandlerContext ctx) {
        return jsonPost(context);
    }

    private static String jsonPost(RequestContext context) {
        GateWayService gateWayService = ReferenceServiceContext.getGateWayService(ReferenceServiceContext.HELLO_SERVICE);
        String methodName = context.method().get();

        OptimizedMetadata.OptimizedService service = MetadataFetcher.getService(context.service().get(), null);

        List<Field> requestFields = service.getMethodMap().get(methodName).request.fields;

        String[] parameterTypes = new String[requestFields.size()];

        for (int i = 0; i < requestFields.size(); i++) {
            parameterTypes[i] = requestFields.get(i).dataType.qualifiedName;
        }

        String paramsJson = context.parameter().get();

        String result = gateWayService.invoke(methodName, parameterTypes,
                new Object[]{paramsJson});

        logger.info("response result: " + result);

        return result;
    }

    private static CompletableFuture<String> jsonPostAsync(RequestContext context, ChannelHandlerContext ctx) {
        GateWayService gateWayService = ReferenceServiceContext.getGateWayService(ReferenceServiceContext.HELLO_SERVICE);
        String methodName = context.method().get();

        //获取 parameterTypes 参数
        OptimizedMetadata.OptimizedService service = MetadataFetcher.getService(context.service().get(), null);

        List<Field> requestFields = service.getMethodMap().get(methodName).request.fields;

        String[] parameterTypes = new String[requestFields.size()];

        for (int i = 0; i < requestFields.size(); i++) {
            parameterTypes[i] = requestFields.get(i).dataType.qualifiedName;
        }

        String paramsJson = context.parameter().get();

        logger.info("receive json : " + paramsJson);

        gateWayService.invoke(methodName, parameterTypes,
                new Object[]{paramsJson});

        CompletableFuture<String> resultFuture = RpcContext.getContext().getCompletableFuture();
        logger.info("异步请求结束，返回 CompletableFuture:  " + resultFuture);
        return resultFuture;


    }
}
