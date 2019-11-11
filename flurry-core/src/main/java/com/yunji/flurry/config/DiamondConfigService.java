package com.yunji.flurry.config;

import com.taobao.diamond.manager.ManagerListener;
import com.yunji.diamond.client.api.DiamondClient;
import com.yunji.flurry.core.ConfigListener;
import com.yunji.flurry.core.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Denim.leihz 2019-08-16 8:05 PM
 */
public class DiamondConfigService implements ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(DiamondConfigService.class);

    private DiamondClient diamondClient = new DiamondClient();
    private SharedManagerListener sharedManagerListener = new SharedManagerListener();
    private AtomicBoolean start = new AtomicBoolean(false);

    private Properties properties;
    private Set<ConfigListener> callback = new HashSet<>();

    /**
     * 启动 diamond 动态配置 client
     */
    @Override
    public void start(String uniqueId) {
        if (!start.get()) {
            synchronized (this) {
                if (!start.get()) {
                    DiamondBean diamondBean = new DiamondBean();
                    diamondBean.setDataId(uniqueId);
                    diamondClient.setDataId(diamondBean.getDataId());
                    diamondClient.setPollingIntervalTime(diamondBean.getPollingIntervalTime());
                    diamondClient.setTimeout(diamondBean.getTimeout());
                    /* 初始化diamond */
                    diamondClient.setManagerListener(sharedManagerListener);
                    diamondClient.init();
                    properties = load(diamondClient.getConfig());
                    sharedManagerListener.addCallBack(this);
                    start.compareAndSet(false, true);
                }
            }

        }
        if (!uniqueId.equals(diamondClient.getDataId())) {
            throw new IllegalArgumentException("diamondClient dataId:" + diamondClient.getDataId() + ",diamondBean dataId:" + uniqueId + " not equals!!!");
        }
    }

    /**
     * 得到配置信息并可回调
     *
     * @param listener
     * @return
     */
    @Override
    public Properties getConfig(ConfigListener listener) {
        if (listener != null) {
            this.addCallback(listener);
        }
        return this.properties;
    }

    /**
     * 得到配置信息
     */
    @Override
    public Properties getConfig() {
        return this.properties;
    }

    /**
     * 从 diamond notify 而来的配置信息
     */
    public void notify(String configInfo) {
        //todo 异常处理
        this.properties = load(configInfo);
        for (ConfigListener listener : callback) {
            listener.notify(this.properties);
        }
    }

    private Properties load(String configInfo) {
        logger.info(diamondClient.getDataId() + " load config:\n" + configInfo);
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(configInfo));
        } catch (IOException e) {
            logger.error("error", e);
        }
        return properties;
    }

    private synchronized void addCallback(ConfigListener listener) {
        callback.add(listener);
    }


    /**
     * Diamond 客户端需要传入的 ManagerListener 实现类
     */
    private static class SharedManagerListener implements ManagerListener {

        private DiamondConfigService diamondConfigService;

        @Override
        public Executor getExecutor() {
            return null;
        }

        public void addCallBack(DiamondConfigService diamondConfigService) {
            this.diamondConfigService = diamondConfigService;
        }

        /**
         * 接收来自 diamond 的配置信息
         */
        @Override
        public void receiveConfigInfo(String configInfo) {
            logger.info("检测到diamond中的数据发生变化-->", configInfo);
            if (diamondConfigService != null) {
                diamondConfigService.notify(configInfo);
            }
        }
    }
}
