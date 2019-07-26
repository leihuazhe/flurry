package org.apache.dubbo.metadata.discovery.zkclient;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.metadata.ServiceMetadataRepository;
import org.apache.dubbo.metadata.ServiceMetadataResolver;
import org.apache.dubbo.metadata.discovery.*;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static org.apache.dubbo.common.constants.CommonConstants.PATH_SEPARATOR;
import static org.apache.zookeeper.ZooKeeper.States.CONNECTED;

/**
 * ZookeeperClientDiscovery
 *
 * @author Denim.leihz 2019-07-23 8:30 PM
 */
public class ZookeeperClientDiscovery extends AbstractZookeeperDiscovery implements Watcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperClientDiscovery.class);

    private static final String ROOT_PATH = PATH_SEPARATOR + DEFAULT_ROOT;

    private static ServiceMetadataRepository repository = ServiceMetadataRepository.getRepository();

    private static final Set<String> rootServices = new ConcurrentHashSet<>();

    private static final Map<String, String> serviceProviders = new ConcurrentHashMap<>();

    private final String zookeeperHost;

    private ZooKeeper zookeeper;

    private static Set<String> whitelist = Collections.synchronizedSet(new HashSet<>());


    public ZookeeperClientDiscovery(URL url) {
        this.zookeeperHost = url.getHost() + ":" + url.getPort();
        init();
    }

    public synchronized void init() {
        connect();
        LOGGER.info("wait for lock");
    }

    private synchronized void destroy() {
        try {
            if (zookeeper != null) {
                zookeeper.close();
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        repository.resetCache();
        LOGGER.info("关闭连接，清空service info caches");
    }

    public synchronized void disconnect() {
        try {
            if (zookeeper != null) {
                zookeeper.close();
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        zookeeper = null;

        LOGGER.info("关闭当前zk连接");
    }

    public void loadAllServices() {
        getChildrenForWatcher(ROOT_PATH);
    }

    public void getChildrenForWatcher(String path) {
        try {
            List<String> children = zookeeper.getChildren(path, this);

            if (children.size() == 0) {
                LOGGER.info("{} 节点下面没有 service 信息", path);
            } else {
                LOGGER.info("获取 {} 的子节点成功", path);
                getDetailService(children);
                LOGGER.info("syncServiceRuntimeInfo 解析服务 {} 元数据信息结束", children.size());
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }


    @Override
    public void process(WatchedEvent event) {
        LOGGER.warn("ZookeeperClientDiscovery::process zkEvent: " + event);
        if (event.getPath() == null) {
            LOGGER.warn("ZookeeperClientDiscovery::process just ignore this event: " + event);
            return;
        }
        switch (event.getType()) {
            case NodeChildrenChanged:
                String path = event.getPath();
                LOGGER.info("Watcher process path {}", path);

                if (path.equals("/dubbo")) {
                    LOGGER.info("/dubbo 节点下的 children 发生变化,重新获取子节点");
                    getChildrenForWatcher(event.getPath());
                } else if (path.startsWith("/dubbo/")) {
                    LOGGER.info("ZookeeperClientDiscovery::process 服务path: " + event.getPath() + " 的子节点发生变化，重新获取信息");
                    processPerInstance(path);
                }
                break;
            default:
                LOGGER.info("just ignore");
        }

    }

    /**
     * 连接zookeeper
     * <p>
     * 需要加锁
     */
    private synchronized void connect() {
        try {
            if (zookeeper != null && zookeeper.getState() == CONNECTED) {
                return;
            }

            CountDownLatch semaphore = new CountDownLatch(1);

            zookeeper = new ZooKeeper(zookeeperHost, 15000, e -> {
                LOGGER.warn("ZookeeperClientDiscovery::connect zkEvent: " + e);
                switch (e.getState()) {
                    case Expired:
                        LOGGER.info("ZookeeperClientDiscovery::connect zookeeper Watcher 到zookeeper Server的session过期，重连");
                        disconnect();
                        connect();
                        break;

                    case SyncConnected:
                        LOGGER.info("ZookeeperClientDiscovery::connect Zookeeper Watcher 已连接 zookeeper Server,Zookeeper host: {}", zookeeperHost);
                        semaphore.countDown();
                        break;

                    case Disconnected:
                        LOGGER.info("Zookeeper Watcher 连接不上了");
                        disconnect();
                        connect();
                        break;

                    case AuthFailed:
                        LOGGER.info("Zookeeper connection auth failed ...");
                        destroy();
                        break;
                    default:
                        break;
                }

            });
            semaphore.await();

        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
        }
    }


    /**
     * serviceName下子节点列表即可用服务地址列表
     * 子节点命名为：host:port:versionName
     */
    private void getDetailService(List<String> children) {
        rootServices.addAll(children);
        LOGGER.info("/dubbo has changed,re-add service list to root set, size {}", rootServices.size());

        for (String child : children) {
            if (serviceProviders.get(child) != null) {
                LOGGER.debug("child {} is in serviceProviders,no need to addChildListener again.", child);
                return;
            }
            serviceProviders.put(child, child);
            String providerPath = "/dubbo/" + child + "/" + RegistryConstants.PROVIDER;
            LOGGER.info("notifyProviderList path: {}", providerPath);

            processPerInstance(providerPath);
        }
    }

    private void processPerInstance(String providerPath) {
        try {
            List<String> serviceChildren = zookeeper.getChildren(providerPath, this);
            if (serviceChildren != null && serviceChildren.size() > 0) {
                processInstance(serviceChildren);
                return;
            }

            if (providerPath.endsWith(RegistryConstants.PROVIDER)) {
                String[] information = providerPath.split("/");
                String serviceInterface = information[information.length - 2];
                repository.removeServiceByRoundInterface(serviceInterface);
            }

        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processInstance(List<String> currentChildren) {
        ServiceDefinition serviceInfo = buildServiceInfo(currentChildren);

        if (serviceInfo != null) {
            ServiceMetadataResolver.resolveServiceMetadata(serviceInfo, new MetadataListener() {
                @Override
                public void callback(boolean flag) {
                    if (!flag) {
                        LOGGER.warn("获取元数据 callback 显示失败,进行第二次重试(最后一次重试)");
                        ServiceMetadataResolver.resolveServiceMetadata(serviceInfo, null);
                    }
                }
            });
        }
        System.out.println("serviceInfo: " + serviceInfo);
    }


    @Override
    protected void subscribe(String serviceKey, RegistryDefinition definition) {

    }

    @Override
    protected void subscribeRootServices(String rootKey) {

    }
}
