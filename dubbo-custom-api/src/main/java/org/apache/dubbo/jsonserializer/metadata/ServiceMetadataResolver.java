package org.apache.dubbo.jsonserializer.metadata;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.gateway.GateWayService;
import org.apache.dubbo.gateway.GatewayServiceFactory;
import org.apache.dubbo.gateway.GlobalReferenceConfig;
import org.apache.dubbo.gateway.RestServiceConfig;
import org.apache.dubbo.jsonserializer.metadata.discovery.*;
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
        ZookeeperDiscovery registry = zookeeperDiscoveryFactory.createRegistry(url);
        registry.loadAllService();
    }

    public static void resolveServiceMetadata(ServiceDefinition serviceDefinition, MetadataListener metadataListener) {
        try {

            logger.info("ServiceMetadataResolver fetchAndStoreMetadata begin to fetch metadata. ");

            CompletableFuture<String> resultFuture = getServiceMetadata(serviceDefinition);
            resultFuture.whenComplete((obj, t) -> {
                if (t != null) {
                    if (metadataListener != null) {
                        metadataListener.callback(false);
                    }
                }

                try (StringReader reader = new StringReader(obj)) {
                    Service serviceData = JAXB.unmarshal(reader, Service.class);
                    //serviceName:version
                    String serviceKey = MetadataUtil.getServiceKey(serviceData);
                    //qualifier serviceName:version
                    String fullNameKey = MetadataUtil.getServiceFullNameKey(serviceData);

                    OptimizedMetadata.OptimizedService optimizedService = new OptimizedMetadata.OptimizedService(serviceData);

                    repository.putService(serviceKey, optimizedService);
                    repository.putFullService(fullNameKey, optimizedService);

                    Map<String, OptimizedMetadata.OptimizedService> storeServices = repository.getServices();
                    logger.info(" ----------------- [ service size :  " + storeServices.size() + "] ----");

                    StringBuilder logBuilder = new StringBuilder();
                    storeServices.forEach((k, v) -> logBuilder.append(k).append(",  "));
                    logger.info("服务实例列表: {}", logBuilder);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });
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
}
