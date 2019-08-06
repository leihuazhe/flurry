package org.apache.dubbo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.dubbo.util.GateConstants.*;

/**
 * @author Denim.leihz 2019-08-06 2:33 PM
 */
public class EnvUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvUtil.class);

    public static String getZkAddress() {
        String zkAddress = System.getenv(REGISTRY_URL_CONSTANT.replace('.', '_'));
        if (zkAddress == null) {
            zkAddress = System.getProperty(REGISTRY_URL_CONSTANT);
        }
        if (zkAddress == null) {
            zkAddress = DEFAULT_REGISTRY_URL;
            LOGGER.error("zookeeper address not found. use default zookeeper address: {}", DEFAULT_REGISTRY_URL);
        } else {
            LOGGER.info("Env util set current application zookeeper address: " + zkAddress);
        }
        return zkAddress;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replaceAll("\\.", "_"));
        if (envValue == null)
            return System.getProperty(key, defaultValue);
        return envValue;
    }
}
