package org.apache.dubbo.jsonserializer.metadata.discovery;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.jsonserializer.metadata.ServiceMetadataRepository;
import org.apache.dubbo.jsonserializer.metadata.ServiceMetadataResolver;
import org.apache.dubbo.remoting.zookeeper.ChildListener;
import org.apache.dubbo.remoting.zookeeper.StateListener;
import org.apache.dubbo.remoting.zookeeper.ZookeeperClient;
import org.apache.dubbo.remoting.zookeeper.ZookeeperTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.dubbo.common.constants.CommonConstants.*;

/**
 * @author Denim.leihz 2019-07-23 2:15 PM
 */
public class ZookeeperDiscovery {
    private static Logger logger = LoggerFactory.getLogger(ZookeeperDiscovery.class);

    private final static String DEFAULT_ROOT = "dubbo";

    private final String root;

    private final ZookeeperClient zkClient;

    private static final Set<String> rootServices = new ConcurrentHashSet<>();

    private static final Map<String, String> serviceProviders = new ConcurrentHashMap<>();

    public ZookeeperDiscovery(URL url, ZookeeperTransporter zookeeperTransporter) {
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
//                        recover();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    /**
     * children size: 5150
     */
    public void loadAllService() {
        List<String> children = zkClient.addChildListener(root, new ChildListener() {
            @Override
            public void childChanged(String path, List<String> currentChildren) {
                addServices(currentChildren);

                for (String child : currentChildren) {
                    child = URL.decode(child);

                    logger.info("Parent path: {}, child: {}", path, child);
                }
                System.out.println("path change: " + path);
            }
        });

        addServices(children);

        System.out.println("children size: " + children.size());
        for (String child : children) {
            System.out.println(URL.decode(child));

        }

    }

    private void addServices(List<String> children) {
        rootServices.addAll(children);
        logger.info("Root {} has changed,re-add service list to root set, size {}", root, rootServices.size());
        notifyProviderList(children);
    }

    /**
     * 对 服务下一级别节点进行监听
     *
     * @param children
     */
    private void notifyProviderList(List<String> children) {
        for (String child : children) {
            if (serviceProviders.get(child) != null) {
                logger.debug("child {} is in serviceProviders,no need to addChildListener again.", child);
                return;
            }
            String path = root + "/" + child + "/" + RegistryConstants.PROVIDER;

            logger.info("notifyProviderList path: {}", path);
            List<String> serviceChildren = zkClient.addChildListener(path, new ChildListener() {
                @Override
                public void childChanged(String path, List<String> currentChildren) {
                    logger.info("Parent path: {}, child: {}", path, currentChildren);
                    if (currentChildren != null) {
                        processInstance(currentChildren);
                    } else {
                        //移除
                        removeServiceInstance(path);
                    }
                }
            });
            if (serviceChildren != null) {
                serviceProviders.put(child, child);
                processInstance(serviceChildren);
            }
        }
    }

    /**
     * 移除MetadataService 缓存
     */
    private void removeServiceInstance(String path) {
        ServiceMetadataResolver.removeServiceMetadata(path);
    }

    private void processInstance(List<String> currentChildren) {
        ServiceDefinition serviceInfo = buildServiceInfo(currentChildren);

        if (serviceInfo != null) {
            ServiceMetadataResolver.resolveServiceMetadata(serviceInfo, new MetadataListener() {
                @Override
                public void callback(boolean flag) {
                    if (!flag) {
                        logger.warn("获取元数据 callback 显示失败,进行第二次重试(最后一次重试)");
                        ServiceMetadataResolver.resolveServiceMetadata(serviceInfo, null);
                    }
                }
            });
        }

        System.out.println("serviceInfo: " + serviceInfo);

    }

    private ServiceDefinition buildServiceInfo(List<String> instanceUrls) {
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
