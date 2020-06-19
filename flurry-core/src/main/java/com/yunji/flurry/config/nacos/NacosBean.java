package com.yunji.flurry.config.nacos;

import java.util.Properties;

/**
 * @author Denim.leihz 2002-06-16 8:02 PM
 */
public class NacosBean {

    private static final String NAMESPACE_KEY = "nacos.namespace";
    private static final String SERVER_ADDR_KEY = "nacos.address";
    private static final String DEFAULT_DATA_ID = "gateway-url-mapping";
    private static final String DEFAULT_GROUP = "URL_MAPPING_GROUP";

    /**
     * 环境变量没有显式设置,则 dataid
     */
    private String dataId;

    private String group;

    private String serverAddr;
    private String namespace;

    private String encode;

    public NacosBean() {
        init();
    }

    public void init() {
        this.dataId = DEFAULT_DATA_ID;
        this.serverAddr = System.getProperty(SERVER_ADDR_KEY);
        this.namespace = System.getProperty(NAMESPACE_KEY);
        this.encode = "UTF-8";
        this.group = DEFAULT_GROUP;
    }

    public Properties buildProperties() {
        if (serverAddr == null || namespace == null) {
            throw new IllegalArgumentException("Start nacos failed, cause serverAddr or namespace is null.");
        }
        Properties props = new Properties();
        props.put("serverAddr", serverAddr);
        props.put("namespace", namespace);
        props.put("encode", encode);
        return props;
    }


    public String getDataId() {
        return dataId;
    }

    public String getGroup() {
        return group;
    }
}
