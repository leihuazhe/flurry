package com.yunji.backup;

import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.common.MetadataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Denim.leihz 2019-07-08 9:58 PM
 */
public class ServiceMetadataRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMetadataRepository.class);
    /**
     * 以服务的SimpleName和version拼接作为key，保存服务元数据的map,etc. AdminSkuPriceService:1.0.0 -> 元信息
     */
    private Map<String, OptimizedService> services = Collections.synchronizedMap(new TreeMap<>());
    /**
     * 以服务的全限定名和version拼接作为key，保存服务元数据的map,etc. AdminSkuPriceService:1.0.0 -> 元信息
     */
    private Map<String, OptimizedService> fullNameService = Collections.synchronizedMap(new TreeMap<>());


    private Map<String, String> pathServiceMapping = Collections.synchronizedMap(new HashMap<>());

    private Map<String, String> fullPathServiceMapping = Collections.synchronizedMap(new HashMap<>());


    private ServiceMetadataRepository() {
    }

    public static ServiceMetadataRepository getRepository() {
        return InnerInstance.instance;
    }

    private static class InnerInstance {
        static ServiceMetadataRepository instance = new ServiceMetadataRepository();
    }


    public void putService(String serviceKey, String serviceInterface, OptimizedService service) {
        this.services.put(serviceKey, service);
        this.pathServiceMapping.put(serviceInterface, serviceKey);
    }

    public void putFullService(String fullServiceKey, String serviceInterface, OptimizedService service) {
        this.fullNameService.put(fullServiceKey, service);
        this.fullPathServiceMapping.put(serviceInterface, fullServiceKey);
    }

    public Map<String, OptimizedService> getServices() {
        return Collections.unmodifiableMap(services);
    }

    public OptimizedService getService(String service, String version) {
        //todo version question
        if (version == null) version = "1.0.0";
        if (service.contains(".")) {
            return fullNameService.get(MetadataUtil.getServiceKey(service, version));
        } else {
            return services.get(MetadataUtil.getServiceKey(service, version));
        }
    }

    public void removeServiceByRoundInterface(String serviceName) {
        String serviceKey = pathServiceMapping.get(serviceName);
        String fullServiceKey = fullPathServiceMapping.get(serviceName);

        if (serviceKey != null && services.get(serviceKey) != null) {
            services.remove(serviceKey);
        }

        if (fullServiceKey != null && fullNameService.get(fullServiceKey) != null) {
            fullNameService.remove(fullServiceKey);
        }

        LOGGER.info("service size: {}, fullService size: {}", services.size(), fullNameService.size());
    }

    public void resetCache() {
        services.clear();
        fullNameService.clear();
    }

    public void removeServiceCache(String servicePath, boolean needLoadUrl) {
        String serviceName = servicePath.substring(servicePath.lastIndexOf(".") + 1);
        String fullServiceName = servicePath.substring(servicePath.lastIndexOf("/") + 1);

        removeByServiceKey(serviceName, services);
        removeByServiceKey(fullServiceName, fullNameService);
    }

    /**
     * 根据服务简名，移除掉每一个map里的与之相关的服务信息
     *
     * @param serviceName 模糊  HelloService
     * @param map
     */
    public <T> void removeByServiceKey(String serviceName, Map<String, T> map) {
        try {
            if (map.size() == 0) {
                throw new RuntimeException("map 为空，不需要进行移除");
            }

            LOGGER.debug("<< begin >>>  根据serviceName:{} 移除不可用实例 移除前map size：{}", serviceName, map.size());
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String serviceKey = it.next();
                String ignoreVersionKey = serviceKey.substring(0, serviceKey.indexOf(":"));

                if (ignoreVersionKey.equals(serviceName)) {
                    it.remove();
                    LOGGER.info("根据 serviceName:{}, 移除不可用实例: key {}, service:{}", serviceName, serviceKey, ignoreVersionKey);
                }
            }
            LOGGER.debug("<< end >>>  根据serviceName:{} 移除不可用实例 移除后map size：{}", map.size());
        } catch (RuntimeException e) {
            LOGGER.debug(e.getMessage(), "map size 为 0 ，不需要进行移除 ...");
        }

    }

    /**
     * 根据服务简名，移除掉每一个map里的与之相关的服务信息
     *
     * @param serviceName 模糊  HelloService
     * @param map
     */
    public <T> void removeByServiceKeyValue(String serviceName, Map<String, T> map) {
        try {
            if (map.size() == 0) {
                throw new RuntimeException("urlMappings map 为空，不需要进行移除");
            }
            LOGGER.debug("<< begin >>>  根据serviceName:{} 移除不可用实例 移除前map size：{}", serviceName, map.size());
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String serviceValue = (String) map.get(it.next());
                if (serviceValue.contains(serviceName)) {
                    it.remove();
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("移除不可用实例信息 struct {}", serviceValue);
                }
            }
            LOGGER.debug("<< end >>>  根据serviceName:{} 移除不可用实例 移除后map size：{}", map.size());
        } catch (RuntimeException e) {
            LOGGER.info(e.getMessage(), "map size 为 0 ，不需要进行移除 ...");
        }

    }

    public void destory() {
        services.clear();
    }


}
