package com.yunji.metadata;

import com.yunji.metadata.tag.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denim.leihz 2019-07-08 9:50 PM
 */
public class ServiceCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCache.class);
    /**
     * 以服务的SimpleName和version拼接作为key，保存服务元数据的map
     * etc. AdminSkuPriceService:1.0.0 -> 元信息
     */
    private static Map<String, OptimizedMetadata.OptimizedService> services = Collections.synchronizedMap(new TreeMap<>());
    /**
     * 以服务的全限定名和 version 拼接作为key，保存服务的实例信息
     * etc. com.today.api.skuprice.service.AdminSkuPriceService:1.0.0  -> ServiceInfo 实例信息
     */
    private static Map<String, ServiceInfo> serverInfoMap = Collections.synchronizedMap(new TreeMap<>());
    /**
     * 以服务的全限定名和version拼接作为key，保存服务元数据的map
     * etc. AdminSkuPriceService:1.0.0 -> 元信息
     */
    private static Map<String, OptimizedMetadata.OptimizedService> fullNameService = Collections.synchronizedMap(new TreeMap<>());
    /**
     * 只针对文档站点进行使用。url展示
     */
    public static Map<String, String> urlMappings = new ConcurrentHashMap<>();


    public static void resetCache() {
        services.clear();
        fullNameService.clear();
        urlMappings.clear();
        serverInfoMap.clear();
    }

    public static void removeServiceCache(String servicePath, boolean needLoadUrl) {
        String serviceName = servicePath.substring(servicePath.lastIndexOf(".") + 1);
        String fullServiceName = servicePath.substring(servicePath.lastIndexOf("/") + 1);

        removeByServiceKey(serviceName, services);
        removeByServiceKey(fullServiceName, fullNameService);

        removeByServiceKey(fullServiceName, serverInfoMap);
        //for openApi
        if (needLoadUrl) {
            removeByServiceKeyValue(serviceName, urlMappings);
        }

    }

    /**
     * 根据服务简名，移除掉每一个map里的与之相关的服务信息
     *
     * @param serviceName 模糊  HelloService
     * @param map
     */
    public static <T> void removeByServiceKey(String serviceName, Map<String, T> map) {
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
            LOGGER.info(e.getMessage(), "map size 为 0 ，不需要进行移除 ...");
        }

    }

    /**
     * 根据服务简名，移除掉每一个map里的与之相关的服务信息
     *
     * @param serviceName 模糊  HelloService
     * @param map
     */
    public static <T> void removeByServiceKeyValue(String serviceName, Map<String, T> map) {
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

    public static void loadServicesMetadata(String serviceName) {
        /*LOGGER.info("access loadServicesMetadata, infos size:{}", infos.size());
        Map<String, ServiceInfo> diffVersionServices = new HashMap<>(64);
        for (ServiceInfo info : infos) {
            diffVersionServices.put(info.versionName, info);
            LOGGER.info("loadServicesMetadata info: {}:{}, version:{}", info.host, info.port, info.versionName);
        }*/
//        LOGGER.info("diffVersionServices values size: {}", diffVersionServices.values().size());
//        for (ServiceInfo info : diffVersionServices.values()) {
//        String version = info.versionName;
        String metadata;
        int tryCount = 1;

        while (tryCount <= 3) {
            try {
                LOGGER.info("begin to fetch metadataClient ...");
                metadata = MetadataFetcher.fetch();

                if (metadata != null) {
                    try (StringReader reader = new StringReader(metadata)) {
                        Service serviceData = JAXB.unmarshal(reader, Service.class);
                        //ServiceName + VersionName for Key
                        //AdminSkuPriceService:1.0.0
                        String serviceKey = getKey(serviceData);
                        //com.today.api.skuprice.service.AdminSkuPriceService:1.0.0
                        String fullNameKey = getFullNameKey(serviceData);

                        OptimizedMetadata.OptimizedService optimizedService = new OptimizedMetadata.OptimizedService(serviceData);

                        services.put(serviceKey, optimizedService);

//                        serverInfoMap.put(fullNameKey, info);

                        LOGGER.info("----------------- service size :  " + services.size());

                        StringBuilder logBuilder = new StringBuilder();
                        services.forEach((k, v) -> logBuilder.append(k).append(",  "));
                        LOGGER.info("zk 服务实例列表: {}", logBuilder);

                        fullNameService.put(fullNameKey, optimizedService);

                    } catch (Exception e) {
//                        LOGGER.error("{}:{} metadata解析出错, {}", serviceName, version);
                        LOGGER.error(e.getMessage(), e);

                        LOGGER.info(metadata);
                    }
                }

//                LOGGER.info("{}:{} metadata获取成功，尝试次数 {}", serviceName, version, tryCount);
                break;

            } catch (Exception e) {
//                LOGGER.error("{}:{} 触发 Exception ,已尝试 {} 次", serviceName, version, tryCount);
                LOGGER.error(e.getMessage(), e);
                tryCount++;
            }
        }
//        }

    }

    public void destory() {
        services.clear();
    }


    public static OptimizedMetadata.OptimizedService getService(String name, String version) {

        if (name.contains(".")) {
            return fullNameService.get(getKey(name, version));
        } else {
            return services.get(getKey(name, version));
        }
    }

    private static String getKey(Service service) {
        return getKey(service.getName(), service.getMeta().version);
    }

    private static String getFullNameKey(Service service) {
        return getKey(service.getNamespace() + "." + service.getName(), service.getMeta().version);
    }

    private static String getKey(String name, String version) {
        return name + ":" + version;
    }

    public static Map<String, OptimizedMetadata.OptimizedService> getServices() {
        return services;
    }

    public static ServiceInfo getServerInfoMap(String name, String version) {
        return serverInfoMap.get(getKey(name, version));
    }
}
