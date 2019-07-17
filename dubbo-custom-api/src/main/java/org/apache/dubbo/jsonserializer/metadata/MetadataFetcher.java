package org.apache.dubbo.jsonserializer.metadata;

import org.apache.dubbo.jsonserializer.metadata.tag.Service;
import org.apache.dubbo.common.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Denim.leihz 2019-07-08 9:58 PM
 */
public class MetadataFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataFetcher.class);
    /**
     * 以服务的SimpleName和version拼接作为key，保存服务元数据的map
     * etc. AdminSkuPriceService:1.0.0 -> 元信息
     */
    private static Map<String, OptimizedMetadata.OptimizedService> services = Collections.synchronizedMap(new TreeMap<>());
    /**
     * 以服务的全限定名和version拼接作为key，保存服务元数据的map
     * etc. AdminSkuPriceService:1.0.0 -> 元信息
     */
    private static Map<String, OptimizedMetadata.OptimizedService> fullNameService = Collections.synchronizedMap(new TreeMap<>());


    public static void init() throws Exception {
        loadAllMetadataOfDir();
    }

    private static void loadAllMetadataOfDir() throws Exception {
        File file = ResourceUtils.getFile("classpath:metadata");

        if (!file.isDirectory()) {
            throw new RuntimeException("Specific dir name is not a directory.");
        }
        List<File> subFileList = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(File::isFile).collect(Collectors.toList());

        for (File subFile : subFileList) {

            String metadata = IOUtils.read(new FileReader(subFile));
            LOGGER.info("begin to fetch metadataClient ...");

            try (StringReader reader = new StringReader(metadata)) {

                Service serviceData = JAXB.unmarshal(reader, Service.class);
                String serviceKey = getKey(serviceData);

                String fullNameKey = getFullNameKey(serviceData);

                OptimizedMetadata.OptimizedService optimizedService = new OptimizedMetadata.OptimizedService(serviceData);

                services.put(serviceKey, optimizedService);

                LOGGER.info("----------------- service size :  " + services.size());

                StringBuilder logBuilder = new StringBuilder();
                services.forEach((k, v) -> logBuilder.append(k).append(",  "));

                LOGGER.info("服务实例列表: {}", logBuilder);

                fullNameService.put(fullNameKey, optimizedService);

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void resetCache() {
        services.clear();
        fullNameService.clear();
    }

    public static void removeServiceCache(String servicePath, boolean needLoadUrl) {
        String serviceName = servicePath.substring(servicePath.lastIndexOf(".") + 1);
        String fullServiceName = servicePath.substring(servicePath.lastIndexOf("/") + 1);

        removeByServiceKey(serviceName, services);
        removeByServiceKey(fullServiceName, fullNameService);
    }

    /**
     * 根据服务简名，移除掉每一个map里的与之相关的服务信息
     *
     * @param serviceName 模糊  HelloService
     * @param map
     */
    public static <T> void removeByServiceKey(String serviceName, Map<String, T> map) {
        try {
            if (map.size() == 0) {
                throw new RuntimeException("map 为空，不需要进行移除");
            }

            LOGGER.debug("<< begin >>>  根据serviceName:{} 移除不可用实例 移除前map size：{}", serviceName, map.size());
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String serviceKey = it.next();
                String ignoreVersionKey = serviceKey.substring(0, serviceKey.indexOf(":"));

                if (ignoreVersionKey.equals(serviceName)) {
                    it.remove();
                    LOGGER.info("根据 serviceName:{}, 移除不可用实例: key {}, service:{}", serviceName, serviceKey, ignoreVersionKey);
                }
            }
            LOGGER.debug("<< end >>>  根据serviceName:{} 移除不可用实例 移除后map size：{}", map.size());
        } catch (RuntimeException e) {
            LOGGER.info(e.getMessage(), "map size 为 0 ，不需要进行移除 ...");
        }

    }

    /**
     * 根据服务简名，移除掉每一个map里的与之相关的服务信息
     *
     * @param serviceName 模糊  HelloService
     * @param map
     */
    public static <T> void removeByServiceKeyValue(String serviceName, Map<String, T> map) {
        try {
            if (map.size() == 0) {
                throw new RuntimeException("urlMappings map 为空，不需要进行移除");
            }
            LOGGER.debug("<< begin >>>  根据serviceName:{} 移除不可用实例 移除前map size：{}", serviceName, map.size());
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String serviceValue = (String) map.get(it.next());
                if (serviceValue.contains(serviceName)) {
                    it.remove();
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("移除不可用实例信息 struct {}", serviceValue);
                }
            }
            LOGGER.debug("<< end >>>  根据serviceName:{} 移除不可用实例 移除后map size：{}", map.size());
        } catch (RuntimeException e) {
            LOGGER.info(e.getMessage(), "map size 为 0 ，不需要进行移除 ...");
        }

    }

    public void destory() {
        services.clear();
    }


    public static OptimizedMetadata.OptimizedService getService(String name, String version) {
        if (version == null) version = "1.0.0";
        if (name.contains(".")) {
            return fullNameService.get(getKey(name, version));
        } else {
            return services.get(getKey(name, version));
        }
    }

    private static String getKey(Service service) {
        return getKey(service.getName(), service.getMeta().version);
    }

    private static String getFullNameKey(Service service) {
        return getKey(service.getNamespace() + "." + service.getName(), service.getMeta().version);
    }

    private static String getKey(String name, String version) {
        return name + ":" + version;
    }

    public static Map<String, OptimizedMetadata.OptimizedService> getServices() {
        return services;
    }
}
