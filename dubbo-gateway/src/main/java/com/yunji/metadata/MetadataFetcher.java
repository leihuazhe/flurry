package com.yunji.metadata;

import org.apache.dubbo.common.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileReader;

/**
 * @author Denim.leihz 2019-07-08 9:58 PM
 */
public class MetadataFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataFetcher.class);


    public static String fetch() {
        File file;
        try {
            file = ResourceUtils.getFile("classpath:metadata/com.yunji.demo.api.HelloService.xml");
            String metaStr = IOUtils.read(new FileReader(file));
            LOGGER.info("Fetch metadata HelloService: " + metaStr);
            return metaStr;
        } catch (Exception e) {
            LOGGER.error("service-whitelist.xml in [classpath] and [/gateway-conf/] NotFound, please Settings", e);
            return null;
//            throw new RuntimeException("service-whitelist.xml in [classpath] and [/gateway-conf/] NotFound, please Settings");
        }
    }

}
