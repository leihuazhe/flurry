package com.yunji.gateway.service;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServiceCreator {

    public static final String DEMO_SERVICE = "org.apache.dubbo.demo.DemoService";

    private static final ConcurrentMap<String, GateWayService> SERVICE_MAP = new ConcurrentHashMap<>(32);

    public static GateWayService getGateWayService() {
        ApplicationConfig application = new ApplicationConfig();

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
//        referenceConfig.setAsync(true);
        referenceConfig.setTimeout(7000);

        GateWayService gateWayService = referenceConfig.get();
        SERVICE_MAP.putIfAbsent(DEMO_SERVICE, gateWayService);
        return gateWayService;
    }

    public static GateWayService getServieByServiceName(String serviceName) {
        return SERVICE_MAP.get(serviceName);
    }
}
