package com.yunji.gateway.process;

import com.google.gson.Gson;
import com.yunji.gateway.service.GateWayService;
import com.yunji.gateway.http.request.RequestContext;
import com.yunji.gateway.service.ServiceCreator;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class PostUtil {
    private static final Gson gson = new Gson();

    private static Logger logger = LoggerFactory.getLogger(PostUtil.class);

    public static CompletableFuture<String> postAsync(RequestContext context) {

        return null;
    }

    public static String post(RequestContext context) {
        GateWayService gateWayService = ServiceCreator.getServieByServiceName(ServiceCreator.DEMO_SERVICE);
        String methodName = context.method().get();
        String[] parameterTypes = new String[]{"org.apache.dubbo.demo.Column", "int"};


        String paramsJson = context.parameter().get();

        logger.info("receive json : " + paramsJson);

//        Json
        HashMap<Object, Object> params = new HashMap<>();
        params.put("name", "maple");
        params.put("count", 12);
        params.put("attachments", new HashMap<>());

        Object result = gateWayService.invoke(methodName, parameterTypes,
                new Object[]{params, 1023});

        String content = gson.toJson(result);
        logger.info("response result: " + content);

        return content;
    }
}
