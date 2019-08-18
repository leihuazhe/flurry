package com.yunji.gateway.metadata.discovery;

import com.yunji.gateway.core.RegistryMetadataClient;
import com.yunji.gateway.metadata.re.ChangeType;
import com.yunji.gateway.metadata.re.RegistryListener;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.remoting.zookeeper.ChildListener;
import org.apache.dubbo.remoting.zookeeper.StateListener;
import org.apache.dubbo.remoting.zookeeper.ZookeeperClient;
import org.apache.dubbo.remoting.zookeeper.ZookeeperTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.dubbo.common.constants.CommonConstants.*;
import static org.apache.dubbo.common.constants.RegistryConstants.*;

/**
 * @author Denim.leihz 2019-07-23 2:15 PM
 */
public class CuratorMetadataClient implements RegistryMetadataClient {
    private static Logger logger = LoggerFactory.getLogger(CuratorMetadataClient.class);

    private final ZookeeperClient zkClient;

    private String root;

    private static final String DEFAULT_ROOT = "dubbo";

    private Set<RegistryListener> listeners = new HashSet<>();

    public CuratorMetadataClient(URL url, ZookeeperTransporter zookeeperTransporter) {
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }

        String group = url.getParameter(RegistryConstants.GROUP_KEY, DEFAULT_ROOT);
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
//                        recover(context);
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
        logger.info("Subscribe service interface: [{}], subscribePath: [{}]", serviceName, subscribePath);

        listeners.add(registryListener);
        ChildListener childListener = new ChildListener() {
            @Override
            public void childChanged(String path, List<String> childrenPath) {
                List<URL> childrenUrls = toUrls(childrenPath);
                for (RegistryListener listener : listeners) {
                    listener.notify(serviceName, path, childrenUrls, ChangeType.REGISTRY_CALLBACK);
                }
            }
        };
        List<String> childrenUrls = zkClient.addChildListener(subscribePath, childListener);

        return toUrls(childrenUrls);
    }

    @Override
    public List<URL> unsubscribe(String serviceName) {
        return null;
    }

    /**
     * 重连后数据 recover.
     *
     * @throws Exception
     */
    protected void recover(/*ConfigContext context*/) throws Exception {
//        logger.info("recover the curator registry,root: {},context white list: {}",
//                root, String.join(",", context.getWhiteServiceSet()));

//        subscribeRootServices();
//        // subscribe
//        Map<String, RegistryDefinition> recoverSubscribed = new HashMap<>(getSubscribed());
//        if (!recoverSubscribed.isEmpty()) {
//            if (logger.isInfoEnabled()) {
//                logger.info("\n Recover subscribe url {}. \n", recoverSubscribed.keySet());
//            }
//
//            for (Map.Entry<String, RegistryDefinition> entry : recoverSubscribed.entrySet()) {
//                String serviceKey = entry.getKey();
//                RegistryDefinition definition = entry.getValue();
//                subscribe(serviceKey, definition);
//            }
//        }
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


//    protected void unsubscribe(String childUrl) {
//        RegistryDefinition definition = getSubscribed().remove(childUrl);
//        if (definition != null) {
//            logger.info("Unsubscribe childUrl {} ,fullPath: {}", childUrl, definition.getFullPath());
//            zkClient.removeChildListener(childUrl, definition.getChildListener());
//            metadataResolver.removeServiceMetadata(childUrl);
//        }
//    }

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
