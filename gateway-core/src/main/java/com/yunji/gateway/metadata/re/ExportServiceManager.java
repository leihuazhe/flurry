package com.yunji.gateway.metadata.re;

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
//                    INSTANCE.init();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * 通知回调
     *
     * @param properties 外部化配置文件
     */
    @Override
    public void notify(Properties properties) {
        //得到元数据服务接口全限定名信息.
        List<String> referServiceList = MetadataUtil.getReferService(properties);

        for (String serviceName : referServiceList) {
            OptimizedMetadata.OptimizedService service = serviceMetadataMap.get(serviceName);
            if (service == null) {
                synchronized (this) {
                    service = serviceMetadataMap.get(serviceName);
                    if (service == null) {
                        try {
                            List<URL> urls = registryMetadataClient.subscribe(serviceName, this);
                            this.notify(serviceName, null, urls, ChangeType.REGISTRY_INIT_CALLBACK);
                        } catch (Exception e) {
                            //todo maybe retry or print detail log message.
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            } else {//
                logger.info("Config white service list,current service [{}]'s metadata  already cached.");
            }
        }
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
            case SERVICE_LIST:
            case REGISTRY_CALLBACK:
                logger.info("Registry center notified event,service: [{}], path: [{}], childrenUrls: {}", serviceName, path, childrenUrls);
                //need remove.
                if (serviceMetadataMap.containsKey(serviceName) && childrenUrls.isEmpty()) {
                    serviceMetadataMap.remove(serviceName);
                    logger.info("Remove service:{} metadata cache, because there is no provider alive." + serviceName);
                    return;
                }
                break;
            default:
                break;
        }

        try {
            //providers
            List<URL> providerURLs = childrenUrls.stream()
                    .filter(Objects::nonNull)
                    .filter(UrlUtils::isProvider)
                    .collect(Collectors.toList());
            // providers
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
