package org.apache.dubbo.demo.consumer.netty;

import com.google.gson.Gson;
import com.yunji.gateway.http.HttpProcessorUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import com.yunji.gateway.service.GateWayService;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.demo.DemoService;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.HashMap;


@ChannelHandler.Sharable
public class HttpProcessHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final ApplicationConfig application = new ApplicationConfig();
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProcessHandler.class);
    private volatile boolean init = false;

    private DemoService demoService;
    private GenericService genericService;


    private GateWayService gateWayService;

    private Gson gson = new Gson();

    HttpProcessHandler() {
//        this.genericService = getGenericService();
//        this.demoService = getServiceStub();
        this.gateWayService = getGateWayService();
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        long start = System.currentTimeMillis();
        if (!init) {
            init();
        }
        String uri = request.uri();

        Object result;
        if (uri.startsWith("/map")) {
//            result = demoService.getMultiIdMaps("maple");
            result = genericService.$invoke("getMultiIdMaps", new String[]{"java.lang.String"}, new Object[]{"maple"});
        } else if (uri.startsWith("/list")) {
//            result = demoService.getMultiIds("maple");
            result = genericService.$invoke("getMultiIds", new String[]{"java.lang.String"}, new Object[]{"maple"});
        } else {
//            result = demoService.getResultByColumn(new Column("123", 12, new HashMap<>()), 1023);

            /*result = genericService.$invokeAsync("getResultByColumn",
                    new String[]{"org.apache.dubbo.demo.Column", int.class.getName()},
                    new Object[]{new Column("123", 12, new HashMap<>()), 1023});

            CompletableFuture<Object> realResult = RpcContext.getContext().getCompletableFuture();

            realResult.whenComplete((actual, t) -> {
                if (t == null) {
                    String content = gson.toJson(actual);
                    LOGGER.info("content :" + content);
                    send(ctx, content, request, HttpResponseStatus.OK);
                } else {
                    LOGGER.info("ex :" + t.getMessage());
                    send(ctx, "error", request, HttpResponseStatus.INTERNAL_SERVER_ERROR);

                }
            });*/
            HashMap<Object, Object> params = new HashMap<>();
            params.put("name", "maple");
            params.put("count", 12);
            params.put("attachments", new HashMap<>());

            //普通
//            result = demoService.getResultByColumn(new Column("maple", 12, new HashMap<>()), 1023);

            result = gateWayService.invoke("getResultByColumn",
                    new String[]{"org.apache.dubbo.demo.Column", int.class.getName()},
                    new Object[]{params, 1023});

            /*result = genericService.$invoke("getResultByColumn",
                    new String[]{"org.apache.dubbo.demo.Column", int.class.getName()},
                    new Object[]{params, 1023});*/


//            LOGGER.info("result is: " + result);
        }

//        String content = new JsonParser().parse((String) result).toString();

        String content = gson.toJson(result);
//        String content = result.toString();

        HttpProcessorUtils.sendHttpResponse(ctx, content, request, HttpResponseStatus.OK);

//        LOGGER.info("Request result:success cost:{} " + (System.currentTimeMillis() - start) + " ms");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Channel error", cause);
//        ctx.close();
    }

    private GenericService getGenericService() {
        application.setName("service-gateway");
        HashMap<String, String> parameters = new HashMap<>();
//        parameters.put("codec", "extremejson");
        parameters.put("client", "custom");
        application.setParameters(parameters);

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");


        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface("org.apache.dubbo.demo.DemoService");
        application.setRegistry(registryConfig);

        referenceConfig.setApplication(application);

        referenceConfig.setGeneric(true);
//        referenceConfig.setAsync(true);

        referenceConfig.setTimeout(7000);
        return referenceConfig.get();
    }

    private GateWayService getGateWayService() {
        application.setName("gateway");
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client", "custom");
        parameters.put("proxy", "custom_javassist");
        parameters.put("gateway", "true");
        application.setParameters(parameters);

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");

        ReferenceConfig<GateWayService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface("org.apache.dubbo.demo.DemoService");
        application.setRegistry(registryConfig);

        referenceConfig.setApplication(application);

//        referenceConfig.setGeneric(true);
//        referenceConfig.setAsync(true);

        referenceConfig.setTimeout(7000);
        return referenceConfig.get();

    }


    private DemoService getServiceStub() {
        application.setName("service-gateway");
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client", "custom");
        application.setParameters(parameters);
        // 直连方式，不使用注册中心
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://127.0.0.1:2181");

        ReferenceConfig<DemoService> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(DemoService.class);
        return reference.get();
    }

    private synchronized void init() {
        if (init) {
            return;
        }
        init = true;
    }

}
