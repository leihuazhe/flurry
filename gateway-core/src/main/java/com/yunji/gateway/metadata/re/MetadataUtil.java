package com.yunji.gateway.metadata.re;

import com.yunji.gateway.GateWayService;
import com.yunji.gateway.GatewayServiceFactory;
import com.yunji.gateway.metadata.OptimizedMetadata;
import com.yunji.gateway.metadata.discovery.RegistryConstants;
import com.yunji.gateway.metadata.tag.Service;
import com.yunji.gateway.util.GateConstants;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.MetaServiceInfo;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Denim.leihz 2019-08-16 9:37 PM
 */
public class MetadataUtil {
    private static final Logger logger = LoggerFactory.getLogger(MetadataUtil.class);

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);


    /**
     * 支持重试的模式获取 OptimizedService
     *
     * @param serviceName 服务接口全限定名
     * @param version     服务版本信息
     * @param group       服务分组,一般为 null
     * @return 服务元数据信息
     */
    public static CompletableFuture<OptimizedMetadata.OptimizedService> callServiceMetadataAsync(String serviceName, String version, String group) {
        return CompletableFuture.supplyAsync(() -> callServiceMetadata(serviceName, version, group), executorService);
    }

    /**
     * 支持重试的模式获取 OptimizedService
     *
     * @param serviceName 服务接口全限定名
     * @param version     服务版本信息
     * @param group       服务分组,一般为 null
     * @return 服务元数据信息
     */
    public static OptimizedMetadata.OptimizedService callServiceMetadata(String serviceName, String version, String group) {
        int retry = GateConstants.DEFAULT_RETRY;
        for (int i = 0; i < retry; i++) {
            try {
                GateWayService gateWayService = GatewayServiceFactory.create(
                        MetaServiceInfo.builder()
                                .serviceName(serviceName)
                                .methodName(RegistryConstants.METADATA_METHOD)
                                .version(version)
                                .group(group)
                                .build());
                //Invoke.
                gateWayService.invoke(RegistryConstants.METADATA_METHOD, new String[]{}, new Object[]{});

                String metaString = (String) RpcContext.getContext()
                        .getCompletableFuture()
                        .get(GateConstants.TIME_OUT, TimeUnit.MILLISECONDS);


                try (StringReader reader = new StringReader(metaString)) {
                    Service service = JAXB.unmarshal(reader, Service.class);
                    return new OptimizedMetadata.OptimizedService(service);
                }
            } catch (ExecutionException tx) {
                String detailMsg = tx.getMessage();
                if (detailMsg.contains("org.apache.dubbo.common.bytecode.NoSuchMethodException")) {
                    logger.error("目标服务:{} 没有增强方法 getServiceMetadata,无法获取到服务元数据.", serviceName);
                    return null;
                }
                logger.error("[" + serviceName + "]: ResolveServiceMetadata get error: " + tx.getMessage(), tx);
            } catch (Exception e) {
                logger.error("[" + serviceName + "]: ResolveServiceMetadata get error: " + e.getMessage(), e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        logger.error("目标:{} 获取服务元数据信息失败,一共调用失败 {} 次", serviceName, retry);

        return null;
    }

    /**
     * 根据配置信息获取到需要引用的服务信息,支持10个级别
     *
     * @param properties 配置信息 WHITE_SERVICES_KEY
     * @return 需要引用的服务接口 list
     */
    public static Set<String> getReferService(Properties properties) {
        Assert.notNull(properties, "ConfigServer 获取到的外部化配置信息为空，请检查并配置相关信息.");
        String baseKey = GateConstants.WHITE_SERVICES_KEY;
        //todo properties NullPointException.
        String whiteStr = properties.getProperty(baseKey);
        char ch = ',';

        if (StringUtils.isNotEmpty(whiteStr)) {
            Set<String> list = null;
            char c;
            int ix = 0, len = whiteStr.length();
            for (int i = 0; i < len; i++) {
                c = whiteStr.charAt(i);
                if (c == ch) {
                    if (list == null) {
                        list = new HashSet<>();
                    }
                    list.add(whiteStr.substring(ix, i));
                    ix = i + 1;
                }
            }
            if (ix > 0) {
                list.add(whiteStr.substring(ix));
            }
            return list == null ? new HashSet<>() : list;
        } else {
            throw new IllegalArgumentException("White list String is empty. Please specify the list string on config server.");
        }
    }
}
