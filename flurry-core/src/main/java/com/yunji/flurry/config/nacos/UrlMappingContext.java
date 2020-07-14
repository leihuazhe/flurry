package com.yunji.flurry.config.nacos;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UrlMappingContext
 *
 * @author leihz
 * @since 2020-06-16 5:21 下午
 */
public class UrlMappingContext {
    private static Logger log = LoggerFactory.getLogger(UrlMappingContext.class);

    private static final Gson GSON = new Gson();

    private static final Map<String, InvokeBean> urlServiceMappingMap = new ConcurrentHashMap<>();


    public static InvokeBean getInvokeByUrl(String url) {
        return urlServiceMappingMap.get(url);
    }

    public static synchronized void reloadMappingMap(String content) {
        try {
            Map<String, String> configMap = GSON.fromJson(content, new TypeToken<Map<String, String>>() {
            }.getType());
            log.info("[UrlMapping] Url映射配置改变,size:{}", configMap.size());
            Map<String, InvokeBean> copyMap = new HashMap<>();

            configMap.forEach((key, value) -> {
                copyMap.put(value, parseToBean(key));
            });
            urlServiceMappingMap.clear();
            urlServiceMappingMap.putAll(copyMap);
        } catch (Exception e) {
            log.error("[UrlMapping] reload url map error: " + e.getMessage(), e);
        }

    }

    private static InvokeBean parseToBean(String key) {
        InvokeBean invokeBean = new InvokeBean();
        String[] infos = key.split("@");
        invokeBean.application = infos[0];
        String serviceVersion = infos[1];
        if (serviceVersion.contains(":")) {
            String[] serviceVersionArray = serviceVersion.split(":");
            invokeBean.service = serviceVersionArray[0];
            invokeBean.version = serviceVersionArray[1];
        } else {
            invokeBean.service = infos[1];
            invokeBean.version = "1.0.0";
        }
        invokeBean.method = infos[2];

        return invokeBean;
    }


    public static class InvokeBean {
        public String application;

        public String service;

        public String method;

        public String version;
    }
}
