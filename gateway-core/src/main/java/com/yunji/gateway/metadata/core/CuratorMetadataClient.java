package com.yunji.gateway.metadata.core;

import com.yunji.gateway.core.RegistryMetadataClient;
import com.yunji.gateway.metadata.common.ChangeType;
import com.yunji.gateway.util.MixUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.remoting.zookeeper.ChildListener;
import org.apache.dubbo.remoting.zookeeper.StateListener;
import org.apache.dubbo.remoting.zookeeper.ZookeeperClient;
import org.apache.dubbo.remoting.zookeeper.ZookeeperTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.apache.dubbo.common.constants.CommonConstants.*;
import static org.apache.dubbo.common.constants.RegistryConstants.*;

/**
 * @author Denim.leihz 2019-07-23 2:15 PM
 */
public class CuratorMetadataClient implements RegistryMetadataClient {
    private static Logger logger = LoggerFactory.getLogger(CuratorMetadataClient.class);

    private final ZookeeperClient zkClient;

    private String root;

    private String GROUP_KEY = "group";

    private static final String DEFAULT_ROOT = "dubbo";

    private Set<RegistryListener> listeners = new HashSet<>();
    //key 是订阅的服务路径 path,不是服务名称 eg:
    private final ConcurrentMap<String, ChildListener> childListeners = new ConcurrentHashMap<>(64);


    public CuratorMetadataClient(URL url, ZookeeperTransporter zookeeperTransporter) {
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }

        String group = url.getParameter(GROUP_KEY, DEFAULT_ROOT);
        if (!group.startsWith(PATH_SEPARATOR)) {
            group = PATH_SEPARATOR + group;
        }
        this.root = group;
        zkClient = zookeeperTransporter.connect(url);
        zkClient.addStateListener(new StateListener() {
            @Override
            public void stateChanged(int state) {
                if (state == RECONNECTED) {
                    try {
                        logger.info("Zookeeper status is reconnected,recover watcher,serviceRegistryListeners size: {}",
                                childListeners.size());
                        recover();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    @Override
    public List<URL> subscribe(String serviceName, RegistryListener registryListener) {
        String subscribePath = toServicePath(serviceName) + PATH_SEPARATOR + PROVIDERS_CATEGORY;
//        logger.info("Subscribe service interface: [{}], subscribePath: [{}]", serviceName, subscribePath);
        listeners.add(registryListener);

        ChildListener childListener = childListeners.get(subscribePath);
        if (childListener == null) {
            childListeners.putIfAbsent(subscribePath, new ChildListener() {
                @Override
                public void childChanged(String path, List<String> childrenPath) {
                    List<URL> childrenUrls = toUrls(childrenPath);
                    for (RegistryListener listener : listeners) {
                        listener.notify(serviceName, path, childrenUrls, ChangeType.REGISTRY_CALLBACK);
                    }
                }
            });
            childListener = childListeners.get(subscribePath);
        }
        List<String> childrenUrls = zkClient.addChildListener(subscribePath, childListener);

        return toUrls(childrenUrls);
    }

    @Override
    public void unsubscribe(String serviceName) {
        String subscribePath = toServicePath(serviceName) + PATH_SEPARATOR + PROVIDERS_CATEGORY;
        ChildListener childListener = childListeners.remove(subscribePath);

        if (childListener != null) {
            zkClient.removeChildListener(subscribePath, childListener);
            logger.info("Unsubscribe service: {} ,subscribePath: {}", serviceName, subscribePath);
        }
    }

    /**
     * 重连后数据 recover.
     *
     * @throws Exception
     */
    private void recover() throws Exception {
        //
        childListeners.forEach((subscribePath, childListener) -> {
            String serviceName = MixUtils.getServiceNameByPath(subscribePath);
//            logger.info("Recover Subscribe path, 重新恢复订阅service {}, 路径: {}", serviceName, subscribePath);
            List<String> urlString = zkClient.addChildListener(subscribePath, childListener);
            List<URL> childrenUrls = toUrls(urlString);
            for (RegistryListener listener : listeners) {
                listener.notify(serviceName, subscribePath, childrenUrls, ChangeType.RECOVER);
            }
        });
    }

    private List<URL> toUrls(List<String> providers) {
        List<URL> urls = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(providers)) {
            for (String provider : providers) {
                provider = URL.decode(provider);
                if (provider.contains(PROTOCOL_SEPARATOR)) {
                    URL url = URL.valueOf(provider);
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    private String toServicePath(String serviceName) {
        return toRootDir() + URL.encode(serviceName);
    }

    private String toRootDir() {
        if (root.equals(PATH_SEPARATOR)) {
            return root;
        }
        return root + PATH_SEPARATOR;
    }


}
