package com.yunji.gateway.process;

import com.google.gson.Gson;
import com.yunji.gateway.service.GateWayService;
import com.yunji.gateway.http.request.RequestContext;
import com.yunji.gateway.service.ServiceCreator;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class PostUtil {
    private static final Gson gson = new Gson();

    private static Logger logger = LoggerFactory.getLogger(PostUtil.class);

    public static CompletableFuture<String> postAsync(RequestContext context) {

        return null;
    }

    public static String post(RequestContext context) {
//        return beanPost(context);
        return jsonPost(context);
    }

    private static String jsonPost(RequestContext context) {
        GateWayService gateWayService = ServiceCreator.getServiceByServiceName(ServiceCreator.HELLO_SERVICE);
        String methodName = context.method().get();
        String[] parameterTypes = new String[]{"com.yunji.demo.api.OrderRequest"};

        String paramsJson = context.parameter().get();

        logger.info("receive json : " + paramsJson);


        Object result = gateWayService.invoke(methodName, parameterTypes,
                new Object[]{paramsJson});


        String content = gson.toJson(result);
        logger.info("response result: " + content);

        return content;
    }

//    private static String jsonPost2(RequestContext context) {
//        GateWayService gateWayService = ServiceCreator.getServiceByServiceName(ServiceCreator.HELLO_SERVICE);
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
//        HelloService helloService = ServiceCreator.getHelloServiceByName(ServiceCreator.HELLO_SERVICE);
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
