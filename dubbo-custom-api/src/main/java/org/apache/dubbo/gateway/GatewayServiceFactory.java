package org.apache.dubbo.gateway;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.CustomReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.remoting.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.apache.dubbo.util.GateConstants.*;

/**
 * Dubbo {@link GateWayService} Factory
 */
public class GatewayServiceFactory {

    private Logger logger = LoggerFactory.getLogger(GatewayServiceFactory.class);
    private static ApplicationConfig application = new ApplicationConfig();

    private static final ConcurrentMap<Integer, CustomReferenceConfig<GateWayService>> SERVICE_CACHE = new ConcurrentHashMap<>();

    static {
        application.setName("gateway");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");
        application.setRegistry(registryConfig);
    }

    public static GateWayService create(RestServiceConfig config) {
        String interfaceName = config.getInterfaceName();
        String version = config.getVersion();
        String group = config.getGroup();

        Integer key = Objects.hash(interfaceName, version, group);
        CustomReferenceConfig<GateWayService> referenceConfig = SERVICE_CACHE.get(key);
        if (referenceConfig == null) {
            synchronized (GatewayServiceFactory.class) {
                referenceConfig = SERVICE_CACHE.get(key);
                if (referenceConfig == null) {
                    SERVICE_CACHE.putIfAbsent(key, createReference(config));
                }
            }
            referenceConfig = SERVICE_CACHE.get(key);
        }

        return referenceConfig.get();
    }


    /**
     * Java 编程式 构建 CustomReferenceConfig。
     * <p>
     * parameters.put("reference.filter", "-genericimpl");
     */
    private static CustomReferenceConfig<GateWayService> createReference(RestServiceConfig config) {

        CustomReferenceConfig<GateWayService> referenceConfig = new CustomReferenceConfig<>();

        HashMap<String, String> parameters = new HashMap<>();
        //自定义 client -> custom
        parameters.put(PROTOCOL_CLIENT, PROTOCOL_CLIENT_CUSTOM);
        //自定义 proxy -> custom_javassist
        parameters.put(PROXY, PROXY_CUSTOM);
        //自定义 serialization -> custom_hessian2
        parameters.put(Constants.SERIALIZATION_KEY, SERIALIZATION_CUSTOM);
        referenceConfig.setParameters(parameters);
        //不能泛化
        referenceConfig.setGeneric(false);
        referenceConfig.setAsync(true);
        referenceConfig.setGateway(true);

        referenceConfig.setApplication(application);
        referenceConfig.setInterface(config.getInterfaceName());
        referenceConfig.setVersion(config.getVersion());

        String group = config.getGroup();
        if (group != null) {
            referenceConfig.setGroup(group);
        }

        //设置其他参数,比如超时等
        referenceConfig.setTimeout(6000);

        return referenceConfig;
    }
}
