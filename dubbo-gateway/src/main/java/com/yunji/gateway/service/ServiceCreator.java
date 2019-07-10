package com.yunji.gateway.service;

//import com.yunji.demo.api.HelloService;

import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.serialize.Serialization;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.remoting.Constants;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServiceCreator {
    private static ApplicationConfig application = new ApplicationConfig();

    public static final String DEMO_SERVICE = "org.apache.dubbo.demo.DemoService";
    public static final String HELLO_SERVICE = "com.yunji.demo.api.HelloService";


    private static final ConcurrentMap<String, GateWayService> SERVICE_MAP = new ConcurrentHashMap<>(32);
    private static final ConcurrentMap<String, GenericService> GENERIC_SERVICE_MAP = new ConcurrentHashMap<>(32);
//    private static final ConcurrentMap<String, HelloService> HELLO_SERVICE_MAP = new ConcurrentHashMap<>(32);

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
        ReferenceConfig<GateWayService> referenceConfig = new ReferenceConfig<>();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client", "custom");
        parameters.put("proxy", "custom_javassist");
        parameters.put("gateway", "true");
        parameters.put(Constants.SERIALIZATION_KEY, "custom_hessian2");
        referenceConfig.setParameters(parameters);
        referenceConfig.setInterface(serviceName);
        referenceConfig.setApplication(application);
        referenceConfig.setGeneric(false);
//        referenceConfig.setGateway(true);
        referenceConfig.setTimeout(7000);

        GateWayService gateWayService = referenceConfig.get();
        SERVICE_MAP.putIfAbsent(serviceName, gateWayService);
        return gateWayService;
    }

    public static GenericService getGenericService(String serviceName) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client", "custom");

        referenceConfig.setParameters(parameters);
        referenceConfig.setInterface(serviceName);

        referenceConfig.setApplication(application);
        referenceConfig.setGeneric(true);
        referenceConfig.setTimeout(7000);

        GenericService genericService = referenceConfig.get();
        GENERIC_SERVICE_MAP.putIfAbsent(serviceName, genericService);
        return genericService;
    }

//    public static HelloService getHelloService(String serviceName) {
//        ReferenceConfig<HelloService> referenceConfig = new ReferenceConfig<>();
//        referenceConfig.setInterface(HelloService.class);
//
//        referenceConfig.setApplication(application);
//        referenceConfig.setTimeout(7000);
//
//        HelloService helloService = referenceConfig.get();
//        HELLO_SERVICE_MAP.putIfAbsent(serviceName, helloService);
//        return helloService;
//    }

    public static GateWayService getServiceByServiceName(String serviceName) {
        return SERVICE_MAP.get(serviceName);
    }

    public static GenericService getGenericServiceByServiceName(String serviceName) {
        return GENERIC_SERVICE_MAP.get(serviceName);
    }

//    public static HelloService getHelloServiceByName(String serviceName) {
//        return HELLO_SERVICE_MAP.get(serviceName);
//    }


}
