package com.yunji.flurry.doc.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author maple.lei
 */
@ConfigurationProperties(prefix = "soa.zookeeper")
public class ApiDocProperties {
    private static Logger logger = LoggerFactory.getLogger(ApiDocProperties.class);

    public static final String ENV_SOA_ZOOKEEPER_HOST = "soa_zookeeper_host";

    public static final String PROP_SOA_ZOOKEEPER_HOST = "soa.zookeeper.host";

    public static final String PARENT_PATH = "/soa/runtime/services";


    private String host ;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}


