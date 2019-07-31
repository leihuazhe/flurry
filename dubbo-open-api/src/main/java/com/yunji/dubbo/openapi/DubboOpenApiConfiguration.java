package com.yunji.dubbo.openapi;

import org.apache.dubbo.metadata.MetadataResolver;
import org.apache.dubbo.metadata.util.MetadataUtil;
import org.apache.dubbo.util.GatewayUtil;
import org.apache.dubbo.util.PropertyUtils;
import org.springframework.beans.factory.InitializingBean;


import static org.apache.dubbo.util.GateConstants.*;

/**
 * @author Denim.leihz 2019-07-31 9:26 PM
 */
public class DubboOpenApiConfiguration implements InitializingBean {
    private String registryUrl;

    private String dataId;

    private MetadataResolver metadataResolver;

    public DubboOpenApiConfiguration() {
        this(PropertyUtils.getProperty(REGISTRY_URL, DEFAULT_REGISTRY_URL),
                PropertyUtils.getProperty(DATA_ID_CONSTANT, DEFAULT_DATA_ID));
    }

    public DubboOpenApiConfiguration(String registryUrl) {
        this(registryUrl, PropertyUtils.getProperty(DATA_ID_CONSTANT, DEFAULT_DATA_ID));
    }

    public DubboOpenApiConfiguration(String registryUrl, String dataId) {
        this.registryUrl = registryUrl;
        this.dataId = dataId;
        this.metadataResolver = GatewayUtil.getSupportedExtension(MetadataResolver.class);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        MetadataUtil.initMetadata(dataId, registryUrl, metadataResolver);
    }


}
