package com.yunji.gateway.util;


import org.apache.dubbo.common.io.UnsafeStringWriter;
import org.apache.dubbo.common.utils.ClassUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ServiceLoader;

import static org.apache.dubbo.rpc.Constants.*;

/**
 * @author Denim.leihz 2019-07-09 11:53 AM
 */
public class MixUtils {
    private static final Logger logger = LoggerFactory.getLogger(MixUtils.class);

    public static boolean isGateWayInvoke(String generic) {
        return generic != null
                && !"".equals(generic)
                && (GENERIC_SERIALIZATION_DEFAULT.equalsIgnoreCase(generic)  /* Normal generalization cal */
                || GENERIC_SERIALIZATION_NATIVE_JAVA.equalsIgnoreCase(generic) /* Streaming generalization call supporting jdk serialization */
                || GENERIC_SERIALIZATION_BEAN.equalsIgnoreCase(generic)
                || GENERIC_SERIALIZATION_PROTOBUF.equalsIgnoreCase(generic));
    }


    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    public static boolean isGateway(String gateway) {
        return Boolean.valueOf(gateway);
    }


    public static String getDescOfString(String parameterTypes) {
        String[] types = parameterTypes.split(",");
        if (types.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(64);
        for (String c : types) {
            sb.append(getDesc(c));
        }

        return sb.toString();
    }


    public static <S> S getServiceInstance(Class<S> service) {
        Iterator<S> iterator = ServiceLoader.load(service, ClassUtils.getClassLoader()).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    private static String getDesc(String c) {
        return "L" + c.replace('.', '/') + ';';
    }


    //========================================
    //环境变量相关
    //========================================
    public static String getZkAddress() {
        String zkAddress = System.getenv(GateConstants.REGISTRY_URL_KEY.replace('.', '_'));
        if (zkAddress == null) {
            zkAddress = System.getProperty(GateConstants.REGISTRY_URL_KEY);
        }
        if (zkAddress == null) {
            zkAddress = GateConstants.DEFAULT_REGISTRY_URL;
            logger.error("zookeeper address not found. use default zookeeper address: {}", GateConstants.DEFAULT_REGISTRY_URL);
        } else {
            logger.info("Env util set current application zookeeper address: " + zkAddress);
        }
        return zkAddress;
    }

    public static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replaceAll("\\.", "_"));
        if (envValue == null)
            return System.getProperty(key, defaultValue);
        return envValue;
    }


    //========================================
    // DUBBO Application 相关
    //========================================
    private static ApplicationConfig application;

    public static void setApplication(ApplicationConfig application) {
        MixUtils.application = application;
    }

    public static ApplicationConfig getApplication() {
        return application;
    }

    //========================================
    // URL 注册模型相关
    //========================================
}
