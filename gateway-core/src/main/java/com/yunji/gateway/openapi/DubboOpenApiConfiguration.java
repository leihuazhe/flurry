package com.yunji.gateway.openapi;

import com.yunji.gateway.core.DubboExecutedFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

import static com.yunji.gateway.util.GateConstants.*;

/**
 * @author Denim.leihz 2019-07-31 9:26 PM
 */
@Configuration
public class DubboOpenApiConfiguration {

    private String registryUrl;

    private String dataId;

    private String applicationName;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void setProperties() {
        this.registryUrl = environment.getProperty(REGISTRY_URL_KEY, DEFAULT_REGISTRY_URL);
        this.dataId = environment.getProperty(DATA_ID_KEY, DEFAULT_DATA_ID);
        this.applicationName = environment.getProperty(APPLICATION_NAME_KEY, DEFAULT_APPLICATION_NAME);

    }

    @Bean
    public DubboExecutedFacade executedFacade() {
        return new DubboExecutedFacade(registryUrl, dataId, applicationName);
    }

}
