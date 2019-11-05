package com.yunji.gateway.metadata.core;

import com.google.common.collect.Sets;
import com.yunji.gateway.core.ConfigListener;
import com.yunji.gateway.core.RegistryMetadataClient;
import com.yunji.gateway.metadata.OptimizedService;
import com.yunji.gateway.metadata.jmx.JmxCache;
import com.yunji.gateway.metadata.common.ChangeType;
import com.yunji.gateway.metadata.common.MetadataUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.common.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.apache.dubbo.common.constants.CommonConstants.*;

/**
 * 网关暴露的服务 manager, 需要从外界调用网关，网关暴露的服务 list 需要通过外部化配置文件进行配置
 *
 * @author Denim.leihz 2019-08-16 9:11 PM
 */
public class ExportServiceManager implements RegistryListener, ConfigListener {
    private static final Logger logger = LoggerFactory.getLogger(ExportServiceManager.class);
    /**
     * Map of bean definition objects, keyed by bean name
     */
    private static final ConcurrentMap<String, OptimizedService> serviceMetadataMap = new ConcurrentHashMap<>(256);
    /**
     * 暂存最近一次的服务白名单信息.
     */
    private Set<String> lastRegistryServices = new HashSet<>();

    private RegistryMetadataClient registryMetadataClient;

    private static volatile ExportServiceManager INSTANCE = null;

    private ExportServiceManager() {
        registerJmx();
    }


    public void setRegistryMetadataClient(RegistryMetadataClient registryMetadataClient) {
        this.registryMetadataClient = registryMetadataClient;
    }

    public OptimizedService getMetadata(String interfaceName, String version) {
        return getMetadata(interfaceName);
    }

    public OptimizedService getMetadata(String interfaceName) {
        return serviceMetadataMap.get(interfaceName);
    }

    public Map<String, OptimizedService> getServiceMetadataMap() {
        return Collections.unmodifiableMap(serviceMetadataMap);
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

            if (StringUtils.isNotEmpty(serviceName)) {
                OptimizedService service = serviceMetadataMap.get(serviceName);
                if (service == null) {
                    synchronized (this) {
                        service = serviceMetadataMap.get(serviceName);
                        if (service == null) {
                            List<URL> urls = registryMetadataClient.subscribe(serviceName, this);
                            if (urls.isEmpty()) {
                                logger.warn("外化指定暴露服务: {} 未注册在注册中心,请检测服务是否启动.", serviceName);
                            } else {
                                this.notifyAsync(serviceName, urls);
                            }
                        } else {
                            logger.debug("目标服务: {} 元数据信息已缓存.", serviceName);
                        }
                    }
                } else {//
                    logger.debug("目标服务: {} 元数据信息已缓存.", serviceName);
                }
            }
        }

        //处理变少的 RegistryServices
        Sets.SetView<String> reduceService = Sets.difference(lastRegistryServices, referServiceList);
        if (!reduceService.isEmpty()) {
            logger.info("外化配置变更,移除以下服务:{}", reduceService.toString());
            for (String service : reduceService) {
                serviceMetadataMap.remove(service);
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
                logger.info("第一次订阅服务触发事件 REGISTRY_INIT_CALLBACK, service: {}, childrenUrl size : {}", serviceName, childrenUrls.size());
                break;
            case RECOVER:
                logger.info("Zookeeper Recover 恢复事件 RECOVER, service: {}, path: {}, childrenUrl size: {}", serviceName, path, childrenUrls.size());
                break;
            case REGISTRY_CALLBACK:
                logger.info("Zookeeper 订阅节点事件变更推送事件 REGISTRY_CALLBACK, service: {}, path: {}, childrenUrl size: {}", serviceName, path, childrenUrls.size());
                //need remove.
                if (serviceMetadataMap.containsKey(serviceName) && childrenUrls.isEmpty()) {
                    serviceMetadataMap.remove(serviceName);
                    logger.info("订阅Zookeeper 服务 {} 节点下没有可用服务,从元数据缓存Map中移除.", serviceName);
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

        refreshMetadataAsync(path, providerURLs);
    }


    private void refreshMetadata(String path, List<URL> providerURLs) {
        try {
            if (!providerURLs.isEmpty()) {
                URL url = providerURLs.get(0);
                String interfaceName = url.getServiceInterface();
                String group = url.getParameter(GROUP_KEY);
                String version = url.getParameter(VERSION_KEY);

                OptimizedService optimizedService = MetadataUtil
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

    protected void refreshMetadataAsync(String path, List<URL> providerURLs) {
        if (!providerURLs.isEmpty()) {
            URL url = providerURLs.get(0);
            String interfaceName = url.getServiceInterface();
            String group = url.getParameter(GROUP_KEY);
            String version = url.getParameter(VERSION_KEY);

            CompletableFuture<OptimizedService> resultFuture
                    = MetadataUtil.callServiceMetadataAsync(interfaceName, version, group);

            resultFuture.whenComplete((optimizedService, e) -> {
                if (e != null) {
                    logger.error("ExportServiceManager notify error " + e.getMessage(), e);
                } else {
                    if (optimizedService == null) {
                        logger.warn("ExportServiceManager 获取服务 {} 元数据失败. ", interfaceName);
                        return;
                    }

                    serviceMetadataMap.put(interfaceName, optimizedService);
                    logServiceMetadataMap();
                }
            });
        } else {
            logger.warn("No provider found for specified service: {}", path);
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
     * 注册 Jmx 信息
     */
    private void registerJmx() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName jmxName = new ObjectName(this.getClass().getName() + ":name=metadataMap");
            final JmxCache jmxCache = new JmxCache(serviceMetadataMap);
            mBeanServer.registerMBean(jmxCache, jmxName);
        } catch (Exception e) {
            logger.warn("RegisterJMX failed ....., cause: ", e);
        }
    }

    /**
     * 日志展示已缓存的服务元数据信息.
     */
    private void logServiceMetadataMap() {
        StringBuilder sb = new StringBuilder();
        sb.append("[已引用和缓存元数据的duubo服务展示]").append("\n");
        for (OptimizedService service : serviceMetadataMap.values()) {
            sb.append(service.service.namespace).append(".").append(service.service.name).append("\n");
        }
        sb.append("\n");
        logger.info(sb.toString());
    }
}
