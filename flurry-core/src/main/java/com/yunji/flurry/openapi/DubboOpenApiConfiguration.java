package com.yunji.flurry.openapi;

import com.yunji.flurry.core.DubboExecutedFacade;
import com.yunji.flurry.util.GateConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * @author Denim.leihz 2019-07-31 9:26 PM
 */
public class DubboOpenApiConfiguration {

    private String registryUrl;

    private String dataId;

    private String applicationName;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void setProperties() {
        this.registryUrl = environment.getProperty(GateConstants.REGISTRY_URL_KEY, GateConstants.DEFAULT_REGISTRY_URL);
        this.dataId = environment.getProperty(GateConstants.DATA_ID_KEY, GateConstants.DEFAULT_DATA_ID);
        this.applicationName = environment.getProperty(GateConstants.APPLICATION_NAME_KEY, GateConstants.DEFAULT_APPLICATION_NAME);

    }

    @Bean
    public DubboExecutedFacade executedFacade() {
        return new DubboExecutedFacade(registryUrl, dataId, applicationName);
    }

}
