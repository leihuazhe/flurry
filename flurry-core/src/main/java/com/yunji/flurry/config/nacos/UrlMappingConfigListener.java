package com.yunji.flurry.config.nacos;

import com.alibaba.nacos.api.config.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UrlMappingConfigListener
 *
 * @author leihz
 * @since 2020-06-16 4:53 下午
 */
public class UrlMappingConfigListener implements Listener {
    private Logger log = LoggerFactory.getLogger(getClass());
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public Executor getExecutor() {
        return executorService;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        log.info("Receive url mapping config info: {}", configInfo);
        UrlMappingContext.reloadMappingMap(configInfo);
    }
}
