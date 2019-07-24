package org.apache.dubbo.jsonserializer.metadata;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.gateway.GateWayService;
import org.apache.dubbo.gateway.GatewayServiceFactory;
import org.apache.dubbo.gateway.GlobalReferenceConfig;
import org.apache.dubbo.gateway.RestServiceConfig;
import org.apache.dubbo.jsonserializer.metadata.discovery.*;
import org.apache.dubbo.jsonserializer.metadata.discovery.curator.CuratorClientDiscovery;
import org.apache.dubbo.jsonserializer.metadata.discovery.zkclient.ZookeeperClientDiscovery;
import org.apache.dubbo.jsonserializer.metadata.tag.Service;
import org.apache.dubbo.jsonserializer.metadata.util.MetadataUtil;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Denim.leihz 2019-07-23 5:38 PM
 */
public class ServiceMetadataResolver {
    private static final Logger logger = LoggerFactory.getLogger(ServiceMetadataResolver.class);

    private static ServiceMetadataRepository repository = ServiceMetadataRepository.getRepository();


    public static void init(URL url) {
        ZookeeperDiscoveryFactory zookeeperDiscoveryFactory = new ZookeeperDiscoveryFactory();
        CuratorClientDiscovery registry = zookeeperDiscoveryFactory.createRegistry(url);
        registry.subscribeRootServices();
//        ZookeeperClientDiscovery registry = zookeeperDiscoveryFactory.createOriginalRegistry(url);
//        registry.loadAllServices();
    }

    public static void resolveServiceMetadata(ServiceDefinition serviceDefinition, MetadataListener metadataListener) {
        logger.info("ServiceMetadataResolver fetchAndStoreMetadata begin to fetch metadata. ");
        int tryCount = 0;
        while (tryCount <= 3) {
            try {
                CompletableFuture<String> resultFuture = getServiceMetadata(serviceDefinition);
                String metadata = resultFuture.get();

                processMetaString(metadata, serviceDefinition);
                break;
            } catch (Exception e) {
                tryCount++;
                logger.error("ResolveServiceMetadata get error: " + e.getMessage(), e);
                /*if (metadataListener != null) {
                    metadataListener.callback(false);
                }*/
            }
            logger.info("已经重试 tryCount: {} 次 ", tryCount);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }


    }

    public static void removeServiceMetadataCache(String path) {
        try {
            logger.info("ServiceMetadataResolver removeServiceMetadataCache path: {}", path);
            repository.removeServiceCache(path, false);

            logServiceMap(repository.getServices());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }


    private static void processMetaString(String metaStr, ServiceDefinition serviceDefinition) {
        try (StringReader reader = new StringReader(metaStr)) {
            Service serviceData = JAXB.unmarshal(reader, Service.class);
            //serviceName:version
            String serviceKey = MetadataUtil.getServiceKey(serviceData);
            //qualifier serviceName:version
            String fullNameKey = MetadataUtil.getServiceFullNameKey(serviceData);

            OptimizedMetadata.OptimizedService optimizedService = new OptimizedMetadata.OptimizedService(serviceData);

            repository.putService(serviceKey, serviceDefinition.getServiceInterface(), optimizedService);
            repository.putFullService(fullNameKey, serviceDefinition.getServiceInterface(), optimizedService);

            Map<String, OptimizedMetadata.OptimizedService> storeServices = repository.getServices();
            logger.info(" ----------------- [ service size :  " + storeServices.size() + "] ----");

            logServiceMap(storeServices);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static CompletableFuture<String> getServiceMetadata(ServiceDefinition serviceInfo) {
        GateWayService gateWayService = GatewayServiceFactory.create(buildConfig(serviceInfo));
        String[] parameterTypes = new String[0];
        gateWayService.invoke(RegistryConstants.METADATA_METHOD, parameterTypes, new Object[]{});

        return RpcContext.getContext().getCompletableFuture();
    }

    private static RestServiceConfig buildConfig(ServiceDefinition serviceInfo) {
        return new RestServiceConfig(serviceInfo.getServiceInterface(),
                serviceInfo.getVersion(),
                serviceInfo.getVersion(),
                GlobalReferenceConfig.buildGlobalConfig());
    }

    private static void logServiceMap(Map<String, OptimizedMetadata.OptimizedService> storeServices) {
        StringBuilder logBuilder = new StringBuilder();
        storeServices.forEach((k, v) -> logBuilder.append(k).append(",  "));
        logger.info("\n\n --------------- 服务实例列表: {} --------\n\n", logBuilder);
    }
}
