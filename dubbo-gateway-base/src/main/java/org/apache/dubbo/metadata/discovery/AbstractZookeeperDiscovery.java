package org.apache.dubbo.metadata.discovery;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.metadata.MetadataResolver;
import org.apache.dubbo.metadata.whitelist.ConfigContext;
import org.apache.dubbo.remoting.zookeeper.ChildListener;
import org.apache.dubbo.util.GatewayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.dubbo.common.constants.CommonConstants.GROUP_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.VERSION_KEY;

/**
 * @author Denim.leihz 2019-07-24 10:14 AM
 */
public abstract class AbstractZookeeperDiscovery {
    private static Logger logger = LoggerFactory.getLogger(AbstractZookeeperDiscovery.class);

    protected MetadataResolver metadataResolver;

    protected ExecutorService executorService = Executors.newFixedThreadPool(10);

    protected static Set<String> lastServiceSet = new ConcurrentHashSet<>();

    private final ConcurrentMap<String, RegistryDefinition> subscribed = new ConcurrentHashMap<>();

    protected final ConcurrentMap<String, ChildListener> zkListeners = new ConcurrentHashMap<>();

    protected static final String DEFAULT_ROOT = "dubbo";

    protected String root;

    public ConcurrentMap<String, RegistryDefinition> getSubscribed() {
        return subscribed;
    }


    {
        metadataResolver = GatewayUtil.getSupportedExtension(MetadataResolver.class);
    }


    protected void recover(ConfigContext context) throws Exception {
        logger.info("recover the curator registry,root: {},context white list: {}",
                root, String.join(",", context.getWhiteServiceSet()));

        subscribeRootServices();
        // subscribe
        Map<String, RegistryDefinition> recoverSubscribed = new HashMap<>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("\n Recover subscribe url {}. \n", recoverSubscribed.keySet());
            }

            for (Map.Entry<String, RegistryDefinition> entry : recoverSubscribed.entrySet()) {
                String serviceKey = entry.getKey();
                RegistryDefinition definition = entry.getValue();
                subscribe(serviceKey, definition);
            }
        }
    }

    public void subscribeRootServices(/*List<String> whiteServiceList*/) {
        subscribeRootServices(root/*, whiteServiceList*/);
    }

    protected abstract void subscribe(String serviceKey, RegistryDefinition definition);

    protected abstract void subscribeRootServices(String rootKey/*, List<String> whiteServiceList*/);

    /**
     * @param serviceKey etc. org.apache.services.demo.DemoService
     * @param urls
     */
    protected void notify(String serviceKey, List<String> urls) {
        executorService.execute(() -> {
            if (urls != null && urls.size() > 0) {
                ServiceDefinition serviceInfo = buildServiceInfo(urls);
                if (serviceInfo != null) {
                    metadataResolver.resolveServiceMetadata(serviceInfo, new MetadataListener() {
                        @Override
                        public void callback(boolean flag) {
                            if (!flag) {
                                logger.warn("获取元数据 callback 显示失败,进行第二次重试(最后一次重试)");
                                metadataResolver.resolveServiceMetadata(serviceInfo, null);
                            } else {
                                logger.info("Notify get metadata successful !");
                            }
                        }
                    });
                }
            } else {
                metadataResolver.removeServiceMetadataCache(serviceKey);
                logger.warn("service {} 下已无任何实例,移除元数据服务 cache.", serviceKey);
            }
        });
    }


    protected ServiceDefinition buildServiceInfo(List<String> instanceUrls) {
        if (instanceUrls.size() > 0) {

            ServiceDefinition.Builder builder = new ServiceDefinition.Builder();
            for (int i = 0; i < instanceUrls.size(); i++) {
                URL url = URL.valueOf(URL.decode(instanceUrls.get(i)));

                if (i == 0) {
                    String serviceInterface = url.getServiceInterface();
                    String group = url.getParameter(GROUP_KEY);
                    String version = url.getParameter(VERSION_KEY);

                    builder.serviceInterface(serviceInterface);
                    builder.group(group);
                    builder.version(version);
                }

                String host = url.getHost();
                int port = url.getPort();
                builder.instance(new ServiceDefinition.Instance(host, port));
            }

            return builder.build();
        }
        return null;
    }
}
