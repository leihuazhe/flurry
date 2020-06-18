package com.yunji.flurry.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.*;
import static com.alibaba.nacos.api.PropertyKeyConst.ENCODE;
import static com.yunji.flurry.util.GateConstants.NACOS_SEPARATOR;

/**
 * NacosServiceFactory
 *
 * @author leihz
 * @since 2020-06-16 4:10 下午
 */
public class NacosServiceFactory {
    private final Map<String, ConfigService> configServicesCache = new LinkedHashMap<>(2);

    private static volatile NacosServiceFactory SINGLETON = new NacosServiceFactory();

    private NacosServiceFactory() {
    }

    public static NacosServiceFactory instance() {
        return SINGLETON;
    }

    public ConfigService create(Properties properties)
            throws NacosException {
        String cacheKey = identify(properties);
        ConfigService configService = configServicesCache.get(cacheKey);

        if (configService == null) {
            synchronized (this) {
                configService = configServicesCache.get(cacheKey);
                if (configService == null) {
                    configService = NacosFactory.createConfigService(properties);
                }
                configServicesCache.put(cacheKey, configService);
            }
        }
        return configService;
    }

    private static String identify(Map<?, ?> properties) {
        String namespace = (String) properties.get(NAMESPACE);
        String serverAddress = (String) properties.get(SERVER_ADDR);
        String contextPath = (String) properties.get(CONTEXT_PATH);
        String clusterName = (String) properties.get(CLUSTER_NAME);
        String endpoint = (String) properties.get(ENDPOINT);
        String accessKey = (String) properties.get(ACCESS_KEY);
        String secretKey = (String) properties.get(SECRET_KEY);
        String encode = (String) properties.get(ENCODE);

        return build(namespace, clusterName, serverAddress, contextPath, endpoint,
                accessKey, secretKey, encode);

    }

    private static String build(Object... values) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Object value : values) {

            String stringValue = value == null ? null : String.valueOf(value);
            if (StringUtils.hasText(stringValue)) {
                stringBuilder.append(stringValue);
            }
            stringBuilder.append(NACOS_SEPARATOR);
        }
        return stringBuilder.toString();
    }
}
