package com.yunji.gateway.process;

import com.google.gson.Gson;
import com.yunji.gateway.http.HttpProcessorUtils;
import com.yunji.gateway.service.GateWayService;
import com.yunji.gateway.http.request.RequestContext;
import com.yunji.gateway.service.ReferenceServiceContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.RpcContext;

import java.util.concurrent.CompletableFuture;

public class PostUtil {
    private static final Gson gson = new Gson();

    private static Logger logger = LoggerFactory.getLogger(PostUtil.class);

    public static CompletableFuture<String> postAsync(RequestContext context) {

        return null;
    }

    public static String post(RequestContext context, ChannelHandlerContext ctx) {
//        return jsonPost(context);
        jsonPostAsync(context, ctx);

        return null;
    }

    private static String jsonPost(RequestContext context) {
        GateWayService gateWayService = ReferenceServiceContext.getGateWayService(ReferenceServiceContext.HELLO_SERVICE);
        String methodName = context.method().get();
        String[] parameterTypes = new String[]{"com.yunji.demo.api.OrderRequest"};

        String paramsJson = context.parameter().get();

        logger.info("receive json : " + paramsJson);


        String result = gateWayService.invoke(methodName, parameterTypes,
                new Object[]{paramsJson});

        logger.info("response result: " + result);

        return result;
    }

    private static void jsonPostAsync(RequestContext context, ChannelHandlerContext ctx) {
        GateWayService gateWayService = ReferenceServiceContext.getGateWayService(ReferenceServiceContext.HELLO_SERVICE);
        String methodName = context.method().get();
        String[] parameterTypes = new String[]{"com.yunji.demo.api.OrderRequest"};

        String paramsJson = context.parameter().get();

        logger.info("receive json : " + paramsJson);

        gateWayService.invoke(methodName, parameterTypes,
                new Object[]{paramsJson});

        CompletableFuture<String> resultFuture = RpcContext.getContext().getCompletableFuture();

        resultFuture.whenComplete((result, tx) -> {
            if (tx != null) {
                logger.error(tx.getMessage());
                HttpProcessorUtils.sendHttpResponse(ctx, tx.getMessage(), context.request(), HttpResponseStatus.OK);
            } else {
                logger.info("response result: " + result);
                HttpProcessorUtils.sendHttpResponse(ctx, result, context.request(), HttpResponseStatus.OK);
            }
        });
        logger.info("异步请求结束.");
    }

//    private static String jsonPost2(RequestContext context) {
//        GateWayService gateWayService = ReferenceServiceContext.getServiceByServiceName(ReferenceServiceContext.HELLO_SERVICE);
//        String methodName = context.method().get();
//        String[] parameterTypes = new String[]{"com.yunji.demo.api.OrderRequest"};
//
//        String paramsJson = context.parameter().get();
//
//        OrderRequest request = new OrderRequest();
//        request.setOrderNo("10231023");
//        request.setProductCount(12);
//        request.setTotalAmount(12.23);
//        request.setStoreId("127001");
//        request.setOrderDetialList(new ArrayList<>());
//
//
//        Object result = gateWayService.invoke(methodName, parameterTypes,
//                new Object[]{request});
//
//
//        String content = gson.toJson(result);
//        logger.info("response result: " + content);
//
//        return content;
//    }

//    private static String beanPost(RequestContext context) {
//        HelloService helloService = ReferenceServiceContext.getHelloServiceByName(ReferenceServiceContext.HELLO_SERVICE);
//
//        OrderRequest request = new OrderRequest();
//        request.setOrderNo("10231023");
//        request.setProductCount(12);
//        request.setTotalAmount(12.23);
//        request.setStoreId("127001");
//        request.setOrderDetialList(new ArrayList<>());
//        OrderResponse result = helloService.createOrder(request);
//
//        String content = gson.toJson(result);
//        logger.info("response result: " + content);
//
//        return content;
//    }
}
