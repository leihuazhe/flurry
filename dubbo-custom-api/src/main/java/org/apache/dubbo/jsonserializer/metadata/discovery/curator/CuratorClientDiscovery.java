package org.apache.dubbo.jsonserializer.metadata.discovery.curator;

import com.google.common.collect.Sets;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.jsonserializer.metadata.ServiceMetadataResolver;
import org.apache.dubbo.jsonserializer.metadata.discovery.*;
import org.apache.dubbo.remoting.zookeeper.ChildListener;
import org.apache.dubbo.remoting.zookeeper.StateListener;
import org.apache.dubbo.remoting.zookeeper.ZookeeperClient;
import org.apache.dubbo.remoting.zookeeper.ZookeeperTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.dubbo.common.constants.CommonConstants.*;

/**
 * @author Denim.leihz 2019-07-23 2:15 PM
 */
public class CuratorClientDiscovery extends AbstractZookeeperDiscovery {
    private static Logger logger = LoggerFactory.getLogger(CuratorClientDiscovery.class);

    private final ZookeeperClient zkClient;

    public CuratorClientDiscovery(URL url, ZookeeperTransporter zookeeperTransporter) {
        if (url.isAnyHost()) {
            throw new IllegalStateException("registry address == null");
        }

        String group = url.getParameter(RegistryConstants.GROUP_KEY, DEFAULT_ROOT);
        if (!group.startsWith(PATH_SEPARATOR)) {
            group = PATH_SEPARATOR + group;
        }
        super.root = group;
        zkClient = zookeeperTransporter.connect(url);
        zkClient.addStateListener(new StateListener() {
            @Override
            public void stateChanged(int state) {
                if (state == RECONNECTED) {
                    try {
                        recover();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    @Override
    protected void subscribeRootServices(String rootKey) {
        List<String> children = zkClient.addChildListener(rootKey, new ChildListener() {
            @Override
            public void childChanged(String path, List<String> currentChildren) {
                if (!path.isEmpty()) {
                    rootPathNotify(path, currentChildren);
                }
            }
        });

        rootPathNotify(rootKey, children);
    }

    private synchronized void rootPathNotify(String path, List<String> serviceUrls) {

        executorService.execute(() -> {
            Set<String> lastPath = lastServiceSet;
            if (lastPath.size() == 0) {
                lastPath.addAll(serviceUrls);
                notifyAddedService(lastPath);
                return;
            }

            Set<String> currentServiceSet = new HashSet<>(serviceUrls);
            Sets.SetView<String> deletedService = Sets.difference(lastServiceSet, currentServiceSet);
            Sets.SetView<String> addedService = Sets.difference(currentServiceSet, lastServiceSet);
//        Sets.SetView<String> couldBeChanged = Sets.intersection(lastServiceSet, currentServiceSet);

            //删除已经去除的 service path.
            if (deletedService.size() > 0) {
                notifyDeleteService(deletedService.immutableCopy());
            }
            //增加新的 service path.
            if (addedService.size() > 0) {
                notifyAddedService(addedService.immutableCopy());
            }
        });
    }


    private void notifyAddedService(Set<String> childrenUrl) {
        lastServiceSet.addAll(childrenUrl);
        for (String childUrl : childrenUrl) {
            String fullPath = root + "/" + childUrl + "/" + RegistryConstants.PROVIDER;
            RegistryDefinition definition = new RegistryDefinition(childUrl, fullPath);
            subscribe(childUrl, definition);
            logger.info("Root subscribe notify service {}, fullPath: {}", childUrl, fullPath);
        }
    }

    private void notifyDeleteService(Set<String> childrenUrl) {
        lastServiceSet.removeAll(childrenUrl);
        for (String childUrl : childrenUrl) {
            unsubscribe(childUrl);
            logger.info("Root unsubscribe notify service {}", childUrl);
        }
    }

    protected void unsubscribe(String childUrl) {
        RegistryDefinition definition = getSubscribed().remove(childUrl);
        if (definition != null) {
            logger.info("Unsubscribe childUrl {} ,fullPath: {}", childUrl, definition.getFullPath());
            zkClient.removeChildListener(childUrl, definition.getChildListener());
            ServiceMetadataResolver.removeServiceMetadataCache(childUrl);
        }
    }

    @Override
    protected void subscribe(String serviceKey, RegistryDefinition definition) {
        String fullPath = definition.getFullPath();
        logger.info("subscribe service: {}, fullPath: {}", serviceKey, fullPath);

        ChildListener childListener = new ChildListener() {
            @Override
            public void childChanged(String path, List<String> childrenUrls) {
                if (!path.isEmpty()) {
                    CuratorClientDiscovery.this.notify(serviceKey, childrenUrls);
                }
            }
        };
        definition.setChildListener(childListener);
        getSubscribed().put(serviceKey, definition);

        List<String> childrenUrls = zkClient.addChildListener(fullPath, childListener);
        CuratorClientDiscovery.this.notify(serviceKey, childrenUrls);
    }

}
