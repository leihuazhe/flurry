package com.yunji.flurry.config.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NacosConfigService
 *
 * @author leihz
 * @since 2020-06-16 4:53 下午
 */
public class NacosConfigService {
    private static final Logger logger = LoggerFactory.getLogger(NacosConfigService.class);
    private AtomicBoolean start = new AtomicBoolean(false);

    private static NacosConfigService instance = new NacosConfigService();

    private NacosConfigService() {
    }

    public static NacosConfigService getInstance() {
        return instance;
    }

    /**
     * 启动 diamond 动态配置 client
     */
    public void start() throws NacosException {
        if (!start.get()) {
            synchronized (this) {
                if (!start.get()) {
                    NacosBean nacosBean = new NacosBean();
                    Properties properties = nacosBean.buildProperties();
                    ConfigService configService = NacosServiceFactory
                            .instance()
                            .create(properties);

                    configService.addListener(nacosBean.getDataId(),
                            nacosBean.getGroup(),
                            new UrlMappingConfigListener()
                    );
                    start.compareAndSet(false, true);
                }
            }

        }
        logger.info("初始化Nacos ConfigServer ok.");
    }
}
