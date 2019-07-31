package com.yunji.dubbo.openapi;

import org.apache.dubbo.metadata.MetadataResolver;
import org.apache.dubbo.metadata.util.MetadataUtil;
import org.apache.dubbo.util.GatewayUtil;
import org.apache.dubbo.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;


import static org.apache.dubbo.util.GateConstants.*;

/**
 * @author Denim.leihz 2019-07-31 9:26 PM
 */
public class DubboOpenConfiguration implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(DubboOpenConfiguration.class);

    private String registryUrl;

    private String dataId;

    private MetadataResolver metadataResolver;

    public DubboOpenConfiguration() {
        this(PropertyUtils.getProperty(REGISTRY_URL, DEFAULT_REGISTRY_URL), PropertyUtils.getProperty(DATA_ID_CONSTANT, DEFAULT_DATA_ID));
    }

    public DubboOpenConfiguration(String registryUrl) {
        this(registryUrl, PropertyUtils.getProperty(DATA_ID_CONSTANT, DEFAULT_DATA_ID));
    }

    public DubboOpenConfiguration(String registryUrl, String dataId) {
        this.registryUrl = registryUrl;
        this.dataId = dataId;
        this.metadataResolver = GatewayUtil.getSupportedExtension(MetadataResolver.class);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        MetadataUtil.initMetadata(dataId, registryUrl, metadataResolver);
    }


}
