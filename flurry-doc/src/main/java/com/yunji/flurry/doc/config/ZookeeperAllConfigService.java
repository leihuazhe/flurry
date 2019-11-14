package com.yunji.flurry.doc.config;

import com.google.common.base.Joiner;
import com.yunji.flurry.config.DiamondConfigService;
import com.yunji.flurry.core.ConfigListener;
import com.yunji.flurry.core.ConfigService;
import com.yunji.flurry.util.GateConstants;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.dubbo.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.yunji.flurry.doc.util.MixUtils.DUBBO_ROOT_PREFIX;
import static com.yunji.flurry.doc.util.MixUtils.REGISTRY_ADDRESS;

/**
 * @author Denim.leihz 2019-11-14 4:59 PM
 */
public class ZookeeperAllConfigService implements ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(DiamondConfigService.class);

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
                    ZookeeperConfigClient zookeeperConfigClient = new ZookeeperConfigClient();
                    //todo
                    String registryAddress = System.getProperty(REGISTRY_ADDRESS, "127.0.0.1:2181");
                    String dubboRootDir = System.getProperty(DUBBO_ROOT_PREFIX, "/dubbo");

                    zookeeperConfigClient.init(registryAddress, dubboRootDir);
                    properties = load(zookeeperConfigClient.getConfigInfo());

                    zookeeperConfigClient.addCallBack(this);
                    start.compareAndSet(false, true);
                }
            }

        }
    }

    /**
     * 得到配置信息并可回调
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
        logger.info("ZookeeperAllConfigService load config:\n" + configInfo);
        if (StringUtils.isBlank(configInfo)) {
            throw new IllegalArgumentException("Config 配置信息为空,请检查");
        }
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
    private static class ZookeeperConfigClient {

        private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        private CuratorFramework client;

        private ZookeeperAllConfigService configService;

        private String configInfo;


        public void init(String registryAddress, String dubboRootDir) {
            // 1.Connect to zk
            client = CuratorFrameworkFactory.newClient(registryAddress, new RetryNTimes(10, 5000));
            client.start();
            monitorZookeeperChange(dubboRootDir);

            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    monitorZookeeperChange(dubboRootDir);
                }
            }, 30, 10, TimeUnit.SECONDS);
        }

        public void monitorZookeeperChange(String root) {
            String newConfig = null;
            try {
                List<String> children = client.getChildren().forPath(root);
                if (children != null && children.size() > 0) {
                    newConfig = Joiner.on(",").join(children);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (configInfo == null) {
                configInfo = newConfig;
                return;
            }

            if (newConfig != null && !configInfo.equals(newConfig)) {
                configInfo = newConfig;
                receiveConfigInfo(newConfig);
            }
        }


        public void addCallBack(ZookeeperAllConfigService localFileConfigService) {
            this.configService = localFileConfigService;
        }

        public String getConfigInfo() {
            return GateConstants.WHITE_SERVICES_KEY + "=" + configInfo;
        }

        /**
         * 接收来自 diamond 的配置信息
         */
        public void receiveConfigInfo(String configInfo) {
            logger.info("检测到zookeeper 数据发生变化-->", configInfo);
            if (configService != null) {
                configService.notify(GateConstants.WHITE_SERVICES_KEY + "=" + configInfo);
            }
        }


    }
}
