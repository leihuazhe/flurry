package org.apache.dubbo.gateway;

import org.apache.dubbo.config.*;
import org.apache.dubbo.gateway.core.ApplicationConfigHolder;
import org.apache.dubbo.remoting.Constants;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.apache.dubbo.util.GateConstants.*;

/**
 * Dubbo {@link GateWayService} Factory
 */
public class GatewayServiceFactory {
    private static final Integer TIME_OUT = 10 * 1000;

    private static final ConcurrentMap<Integer, CustomReferenceConfig<GateWayService>> SERVICE_CACHE = new ConcurrentHashMap<>();


    public static GateWayService create(RpcRequest request) {

        return create(MetaServiceInfo.builder()
                .serviceName(request.getServiceName())
                .methodName(request.getMethod())
                .version(request.getVersion())
                .group(request.getGroup())
                .build());
    }

    public static GateWayService create(MetaServiceInfo serviceInfo) {
        Integer key = getCacheKey(serviceInfo);
        CustomReferenceConfig<GateWayService> referenceConfig = SERVICE_CACHE.get(key);
        if (referenceConfig == null) {
            synchronized (GatewayServiceFactory.class) {
                referenceConfig = SERVICE_CACHE.get(key);
                if (referenceConfig == null) {
                    SERVICE_CACHE.putIfAbsent(key, createReference(serviceInfo));
                }
            }
            referenceConfig = SERVICE_CACHE.get(key);
        }

        return referenceConfig.get();
    }


    public void destroy() {
        SERVICE_CACHE.forEach((s, generic) -> generic.destroy());
        SERVICE_CACHE.clear();
    }

    private static Integer getCacheKey(MetaServiceInfo serviceInfo) {
        String interfaceName = serviceInfo.getServiceName();
        String version = serviceInfo.getVersion();
        String group = serviceInfo.getGroup();

        return Objects.hash(interfaceName, version, group);
    }


    /**
     * Java 编程式 构建 CustomReferenceConfig。
     * <p>
     * parameters.put("reference.filter", "-genericimpl");
     */
    private static CustomReferenceConfig<GateWayService> createReference(MetaServiceInfo serviceInfo) {

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

        ApplicationConfig application = ApplicationConfigHolder.getApplication();

        referenceConfig.setApplication(application);
        referenceConfig.setInterface(serviceInfo.getServiceName());
        referenceConfig.setVersion(serviceInfo.getVersion());

        String group = serviceInfo.getGroup();
        if (group != null) {
            referenceConfig.setGroup(group);
        }

        //设置其他参数,比如超时等
        referenceConfig.setTimeout(TIME_OUT);

        return referenceConfig;
    }
}