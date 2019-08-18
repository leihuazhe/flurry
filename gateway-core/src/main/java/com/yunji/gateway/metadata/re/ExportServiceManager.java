package com.yunji.gateway.metadata.re;

import com.google.common.collect.Sets;
import com.yunji.gateway.core.ConfigListener;
import com.yunji.gateway.core.RegistryMetadataClient;
import com.yunji.gateway.metadata.OptimizedMetadata;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.apache.dubbo.common.constants.CommonConstants.*;

/**
 * @author Denim.leihz 2019-08-16 9:11 PM
 */
public class ExportServiceManager implements RegistryListener, ConfigListener {
    private static final Logger logger = LoggerFactory.getLogger(ExportServiceManager.class);
    /**
     * Map of bean definition objects, keyed by bean name
     */
    private static final ConcurrentMap<String, OptimizedMetadata.OptimizedService> serviceMetadataMap = new ConcurrentHashMap<>(256);
    /**
     * 暂存最近一次的服务白名单信息.
     */
    private Set<String> lastRegistryServices = new HashSet<>();

    private RegistryMetadataClient registryMetadataClient;

    private static volatile ExportServiceManager INSTANCE = null;

    private ExportServiceManager() {
    }

    public void setRegistryMetadataClient(RegistryMetadataClient registryMetadataClient) {
        this.registryMetadataClient = registryMetadataClient;
    }

    public OptimizedMetadata.OptimizedService getMetadata(String interfaceName, String version) {
        return getMetadata(interfaceName);
    }

    public OptimizedMetadata.OptimizedService getMetadata(String interfaceName) {
        return serviceMetadataMap.get(interfaceName);
    }

    public void clear() {
        serviceMetadataMap.clear();
    }


    /**
     * 单例获取
     *
     * @return ExportServiceManager
     */
    public static ExportServiceManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ExportServiceManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExportServiceManager();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * 外部化配置 加 synchronized,这个接口调用较少
     *
     * @param properties 外部化配置文件
     */
    @Override
    public synchronized void notify(Properties properties) {
        //得到元数据服务接口全限定名信息.
        Set<String> referServiceList = MetadataUtil.getReferService(properties);

        for (String serviceName : referServiceList) {
            OptimizedMetadata.OptimizedService service = serviceMetadataMap.get(serviceName);
            if (service == null) {
                synchronized (this) {
                    service = serviceMetadataMap.get(serviceName);
                    if (service == null) {
                        List<URL> urls = registryMetadataClient.subscribe(serviceName, this);
                        this.notifyAsync(serviceName, urls);
                    } else {
                        logger.info("Target white list service: {} already cached.", serviceName);
                    }
                }
            } else {//
                logger.info("Config white service list,current service [{}]'s metadata  already cached.");
            }
        }

        //处理变少的 RegistryServices
        Sets.SetView<String> reduceService = Sets.difference(lastRegistryServices, referServiceList);
        if (!reduceService.isEmpty()) {
            for (String service : reduceService) {
                registryMetadataClient.unsubscribe(service);
            }
        }
        lastRegistryServices.clear();
        lastRegistryServices.addAll(referServiceList);
    }


    /**
     * 通知回调,这是我们订阅的服务在注册中心变更后,callback 回来的消息.
     *
     * @param path         监听服务路径
     * @param childrenUrls providers 下面的内容
     */
    @Override
    public void notify(String serviceName, String path, List<URL> childrenUrls, ChangeType changeType) {
        switch (changeType) {
            case REGISTRY_INIT_CALLBACK:
                logger.info("First subscribe event, service: [{}], childrenUrls: {}", serviceName, childrenUrls);
                break;
            case RECOVER:
                logger.info("Registry Recover notified event,service: [{}], path: [{}], childrenUrls: {}", serviceName, path, childrenUrls);
                break;
            case REGISTRY_CALLBACK:
                logger.info("Registry center notified event,service: [{}], path: [{}], childrenUrls: {}", serviceName, path, childrenUrls);
                //need remove.
                if (serviceMetadataMap.containsKey(serviceName) && childrenUrls.isEmpty()) {
                    serviceMetadataMap.remove(serviceName);
                    logger.info("Remove service:{} metadata cache, because there is no provider alive.", serviceName);
                    return;
                }
                break;
            default:
                break;
        }
        //providers
        List<URL> providerURLs = childrenUrls.stream()
                .filter(Objects::nonNull)
                .filter(UrlUtils::isProvider)
                .collect(Collectors.toList());

//        refreshMetadata(path, providerURLs);
        refreshMetadataAsync(path, providerURLs);


    }


    private void refreshMetadata(String path, List<URL> providerURLs) {
        try {
            if (!providerURLs.isEmpty()) {
                URL url = providerURLs.get(0);
                String interfaceName = url.getServiceInterface();
                String group = url.getParameter(GROUP_KEY);
                String version = url.getParameter(VERSION_KEY);

                OptimizedMetadata.OptimizedService optimizedService = MetadataUtil
                        .callServiceMetadata(interfaceName, version, group);

                if (optimizedService == null) {
                    logger.warn("ExportServiceManager 获取服务 {} 元数据失败. ", interfaceName);
                    return;
                }
                serviceMetadataMap.put(interfaceName, optimizedService);
            } else {
                logger.warn("No provider found for specified service: {}", path);
            }
        } catch (Exception e) {
            logger.error("ExportServiceManager notify error " + e.getMessage(), e);
        }

        logServiceMetadataMap();
    }

    private void refreshMetadataAsync(String path, List<URL> providerURLs) {
        if (!providerURLs.isEmpty()) {
            URL url = providerURLs.get(0);
            String interfaceName = url.getServiceInterface();
            String group = url.getParameter(GROUP_KEY);
            String version = url.getParameter(VERSION_KEY);

            CompletableFuture<OptimizedMetadata.OptimizedService> resultFuture
                    = MetadataUtil.callServiceMetadataAsync(interfaceName, version, group);

            if (optimizedService == null) {
                logger.warn("ExportServiceManager 获取服务 {} 元数据失败. ", interfaceName);
                return;
            }
            serviceMetadataMap.put(interfaceName, optimizedService);
        }
    }

    /**
     * notify async
     */
    private void notifyAsync(String serviceName, List<URL> childrenUrls) {
        CompletableFuture.runAsync(() -> {
            notify(serviceName, null, childrenUrls, ChangeType.REGISTRY_INIT_CALLBACK);
        });
    }

    /**
     * 日志展示已缓存的服务元数据信息.
     */
    private void logServiceMetadataMap() {
        StringBuilder sb = new StringBuilder();
        sb.append("[已引用和缓存元数据的duubo服务展示]").append("\n");
        for (OptimizedMetadata.OptimizedService service : serviceMetadataMap.values()) {
            sb.append(service.service.namespace).append(".").append(service.service.name).append("\n");
        }
        sb.append("\n");
        logger.info(sb.toString());
    }
}
