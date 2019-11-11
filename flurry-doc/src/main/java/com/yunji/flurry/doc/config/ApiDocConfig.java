package com.yunji.flurry.doc.config;

import com.yunji.flurry.core.DubboExecutedFacade;
import com.yunji.flurry.process.Post;
import com.yunji.flurry.doc.repository.GatewayDocStreamPoster;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * desc: api doc config for metadata
 *
 * @author hz.lei
 * @since 2018年06月21日 下午12:25
 */
@Configuration
public class ApiDocConfig implements InitializingBean {

    @Value("${dubbo.registry.address}")
    private String registryUrl;

    @Value("${flurry.diamond.dataId}")
    private String diamondId;


    @Value("${config_env}")
    private String configEnv;

    @Value("${diamond_server_host}")
    private String diamondServerHost;


    @Bean
    public DubboExecutedFacade dubboExecutedFacade() {
        //如果环境变量没有 diamond 相关信息,进行设置
        if (System.getenv("CONFIG_ENV") == null) {
            System.setProperty("config_env", configEnv);
        }
        if (System.getenv("diamond_server_host") == null) {
            System.setProperty("diamond_server_host", diamondServerHost);
        }

        return new DubboExecutedFacade(registryUrl, diamondId);
    }

    @Bean
    public Post getPost(DubboExecutedFacade dubboExecutedFacade) {
        return new GatewayDocStreamPoster(dubboExecutedFacade);
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
