package com.yunji.gateway.service;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.remoting.Constants;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.yunji.gateway.util.GateConstants.*;


/**
 * 网关管理的所有 provider service.
 */
public class ReferenceServiceContext {
    private static ApplicationConfig application = new ApplicationConfig();
    //Fixme Just for test.
    public static final String HELLO_SERVICE = "com.yunji.demo.api.HelloService";

    private static final ConcurrentMap<String, GateWayService> SERVICE_MAP = new ConcurrentHashMap<>(32);

    static {
        application.setName("gateway");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");
        application.setRegistry(registryConfig);
    }

    /**
     * parameters.put("reference.filter", "-genericimpl");
     */
    public static GateWayService getGateWayService(String serviceName) {
        GateWayService service = SERVICE_MAP.get(serviceName);
        if (service != null) {
            return service;
        }
        synchronized (ReferenceServiceContext.class) {
            if (SERVICE_MAP.get(serviceName) == null) {
                SERVICE_MAP.putIfAbsent(serviceName, createReference(serviceName));
            }
        }

        return SERVICE_MAP.get(serviceName);
    }

    /**
     * Java 编程式 构建 ReferenceConfig。
     */
    private static GateWayService createReference(String serviceName) {

        ReferenceConfig<GateWayService> referenceConfig = new ReferenceConfig<>();

        HashMap<String, String> parameters = new HashMap<>();
        //自定义 client -> custom
        parameters.put(PROTOCOL_CLIENT, PROTOCOL_CLIENT_CUSTOM);
        //自定义 proxy -> custom_javassist
        parameters.put(PROXY, PROXY_CUSTOM);
        //自定义 serialization -> custom_hessian2
        parameters.put(Constants.SERIALIZATION_KEY, SERIALIZATION_CUSTOM);
        referenceConfig.setParameters(parameters);

        referenceConfig.setInterface(serviceName);
        referenceConfig.setApplication(application);
        referenceConfig.setGeneric(false);
        referenceConfig.setAsync(true);
        referenceConfig.setGateway(true);
        //默认超时时间 todo
        referenceConfig.setTimeout(6000);

        return referenceConfig.get();
    }
}
