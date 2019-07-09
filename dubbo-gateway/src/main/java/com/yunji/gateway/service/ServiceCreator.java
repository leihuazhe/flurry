package com.yunji.gateway.service;

import com.yunji.demo.api.HelloService;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
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
    private static final ConcurrentMap<String, HelloService> HELLO_SERVICE_MAP = new ConcurrentHashMap<>(32);

    public static GateWayService getGateWayService(String serviceName) {
        application.setName("gateway");
//        application.setParameters(parameters);

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");

        ReferenceConfig<GateWayService> referenceConfig = new ReferenceConfig<>();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client", "custom");
        parameters.put("proxy", "custom_javassist");
        parameters.put("gateway", "true");
//        parameters.put("reference.filter", "-genericimpl");

        referenceConfig.setParameters(parameters);


        referenceConfig.setInterface(serviceName);
        application.setRegistry(registryConfig);

        referenceConfig.setApplication(application);
//        referenceConfig.setAsync(true);
        referenceConfig.setGeneric(false);
        referenceConfig.setGateway(true);
        referenceConfig.setTimeout(7000);


        GateWayService gateWayService = referenceConfig.get();
        SERVICE_MAP.putIfAbsent(serviceName, gateWayService);
        return gateWayService;
    }

    public static GenericService getGenericService(String serviceName) {
        ApplicationConfig application = new ApplicationConfig();

        application.setName("gateway");
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("client", "custom");
//        parameters.put("proxy", "custom_javassist");
//        parameters.put("gateway", "true");
//        parameters.put("reference.filter", "-genericimpl");
        application.setParameters(parameters);

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");

        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(serviceName);
        application.setRegistry(registryConfig);

        referenceConfig.setApplication(application);
//        referenceConfig.setAsync(true);
        referenceConfig.setGeneric(true);
        referenceConfig.setGateway(true);
        referenceConfig.setTimeout(7000);


        GenericService genericService = referenceConfig.get();
        GENERIC_SERVICE_MAP.putIfAbsent(serviceName, genericService);
        return genericService;
    }

    public static HelloService getHelloService(String serviceName) {
//        ApplicationConfig application = new ApplicationConfig();

//        application.setName("helloService");

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");

        ReferenceConfig<HelloService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(HelloService.class);
        application.setRegistry(registryConfig);

        referenceConfig.setApplication(application);
        referenceConfig.setTimeout(7000);

        HelloService helloService = referenceConfig.get();
        HELLO_SERVICE_MAP.putIfAbsent(serviceName, helloService);
        return helloService;
    }

    public static GateWayService getServiceByServiceName(String serviceName) {
        return SERVICE_MAP.get(serviceName);
    }

    public static GenericService getGenericServiceByServiceName(String serviceName) {
        return GENERIC_SERVICE_MAP.get(serviceName);
    }

    public static HelloService getHelloServiceByName(String serviceName) {
        return HELLO_SERVICE_MAP.get(serviceName);
    }


}
