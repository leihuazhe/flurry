package com.yunji.flurry.config.nacos;

import com.alibaba.nacos.api.config.listener.Listener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ConfigListener
 *
 * @author leihz
 * @since 2020-06-16 4:53 下午
 */
public class ConfigListener implements Listener {
    private Logger log = LoggerFactory.getLogger(getClass());
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public Executor getExecutor() {
        return executorService;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        receiveConfigInfo(configInfo, false);
    }

    public void firstReceiveConfig(String configInfo) {
        receiveConfigInfo(configInfo, true);
    }

    private void receiveConfigInfo(String configInfo, boolean first) {
        log.info("Receive url mapping config,isFirst:{}, content: {}", first, configInfo);
        if (StringUtils.isNotEmpty(configInfo)) {
            UrlMappingContext.reloadMappingMap(configInfo);
        }
    }
}
