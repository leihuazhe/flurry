package org.apache.dubbo.openapi;

import org.apache.dubbo.gateway.core.DubboExecutedFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

import static org.apache.dubbo.util.GateConstants.*;

/**
 * @author Denim.leihz 2019-07-31 9:26 PM
 */
@Configuration
public class DubboOpenApiConfiguration {

    private String registryUrl;

    private String dataId;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void setProperties() {
        this.registryUrl = environment.getProperty(REGISTRY_URL_CONSTANT, DEFAULT_REGISTRY_URL);
        this.dataId = environment.getProperty(DATA_ID_CONSTANT, DEFAULT_DATA_ID);
    }

    @Bean
    public DubboExecutedFacade executedFacade() {
        return new DubboExecutedFacade(registryUrl, dataId);
    }

}
