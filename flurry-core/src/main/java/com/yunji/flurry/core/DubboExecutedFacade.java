package com.yunji.flurry.core;

import com.yunji.flurry.GateWayService;
import com.yunji.flurry.GatewayServiceFactory;
import com.yunji.flurry.config.DiamondConfigService;
import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.core.CuratorMetadataClient;
import com.yunji.flurry.metadata.core.ExportServiceManager;
import com.yunji.flurry.metadata.common.MetadataUtil;
import com.yunji.flurry.util.GateConstants;
import com.yunji.flurry.metadata.tag.Field;
import com.yunji.flurry.util.MixUtils;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.RpcRequest;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.RegistryService;
import org.apache.dubbo.remoting.zookeeper.ZookeeperTransporter;
import org.apache.dubbo.rpc.RpcContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;


/**
 * @author Denim.leihz 2019-07-31 6:44 PM
 */
public class DubboExecutedFacade {

    private final String registryUrl;
    /**
     * uniqueId,如果外部化配置是 diamondClient，则 uniqueId 指 dataId.
     */
    private final String uniqueId;
    //Application 名称
    private final String applicationName;

    //外部化配置
    private ConfigService configService;

    //可暴露服务管理类
    private ExportServiceManager exportServiceManager;

    //zookeeper client
    private RegistryMetadataClient registryMetadataClient;

    private ZookeeperTransporter zookeeperTransporter = ExtensionLoader
            .getExtensionLoader(ZookeeperTransporter.class)
            .getAdaptiveExtension();


    /**
     * @param registryUrl 注册中心 url
     * @param uniqueId    默认没有其他外部化配置组件，则为 dataId.
     */
    public DubboExecutedFacade(String registryUrl, String uniqueId) {
        this(registryUrl, uniqueId, GateConstants.DEFAULT_APPLICATION_NAME);
    }

    /**
     * @param registryUrl     注册中心 url
     * @param uniqueId        默认没有其他外部化配置组件，则为 dataId.
     * @param applicationName Dubbo application name
     */
    public DubboExecutedFacade(String registryUrl, String uniqueId, String applicationName) {
        this.registryUrl = registryUrl;
        this.uniqueId = uniqueId;
        this.applicationName = applicationName;
        init();
    }

    /**
     * 初始化程序
     */
    private void init() {
        //初始化 Application config.
        initApplicationIfAbsent();
        //初始化外部化配置 ConfigService.
        initConfigService();
        //init 去注册中心获取数据并监听的 zookeeper registry client
        intZookeeperClient();
        //初始化 refer/white list listener,并开始 get metadata.
        initConfigListener();
    }


    public CompletableFuture<String> execute(String interfaceName, String methodName, String version, String requestJson) {
        OptimizedService optimizedService = exportServiceManager.getMetadata(interfaceName, version);

        return execute(interfaceName, methodName, version, requestJson, optimizedService);
    }

    public CompletableFuture<String> execute(String interfaceName, String methodName, String version,
                                             String requestJson, OptimizedService optimizedService) {
        if (requestJson == null) requestJson = "{}";
        if (optimizedService != null) {
            List<Field> requestFields = optimizedService.getMethodMap().get(methodName).request.fields;

            String[] parameterTypes = new String[requestFields.size()];
            for (int i = 0; i < requestFields.size(); i++) {
                parameterTypes[i] = MetadataUtil.getDataKindType(requestFields.get(i).dataType);
            }

            RpcRequest rpcRequest = RpcRequest.builder()
                    .serviceName(interfaceName)
                    .method(methodName)
                    .version(version)
                    .paramsType(parameterTypes)
                    .paramsValue(new Object[]{requestJson})
                    .build();

            return send(rpcRequest);
        }

        throw new RuntimeException("Metadata service definition == null.");
    }

    public ConfigService getConfigService() {
        return configService;
    }

    /**
     * 初始化 ApplicationConfig
     */
    private void initApplicationIfAbsent() {
        ApplicationConfig application;
        //如果上下文中已经存在了 ApplicationConfig,跳过
        if (ConfigManager.getInstance().getApplication().isPresent()) {
            application = ConfigManager.getInstance().getApplication().get();
        } else {
            application = new ApplicationConfig();
            application.setName(applicationName);
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setProtocol(GateConstants.REGISTER_PROTOCOL);
            registryConfig.setAddress(registryUrl);
            application.setRegistry(registryConfig);
        }

        MixUtils.setApplication(application);
    }

    /**
     * 初始化外部化配置,首先加载 ConfigService 的 SPI 实现类，如果没有，则使用 默认的 DiamondConfigService.
     */
    private void initConfigService() {
        //Get时即初始化
        configService = MixUtils.getServiceInstance(ConfigService.class);

        if (configService == null) {
            configService = new DiamondConfigService();
        }
        configService.start(uniqueId);
    }

    /**
     * init 去注册中心获取数据并监听的 zookeeper registry client
     */
    private void intZookeeperClient() {
        Assert.notNull(registryUrl, "RegistryUrl == null");

        String address;
        if (registryUrl.contains("zookeeper")) {
            address = registryUrl;
        } else {
            address = "zookeeper://" + registryUrl;
        }

        if (address.length() > 0 && !RegistryConfig.NO_AVAILABLE.equalsIgnoreCase(address)) {
            Map<String, String> map = new HashMap<>();
            map.put("path", RegistryService.class.getName());
            map.put("interface", RegistryService.class.getName());
            map.put("dubbo", Version.getProtocolVersion());
            map.put(CommonConstants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
            if (ConfigUtils.getPid() > 0) {
                map.put(CommonConstants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
            }
            if (!map.containsKey("protocol")) {
                if (ExtensionLoader.getExtensionLoader(RegistryFactory.class).hasExtension("remote")) {
                    map.put("protocol", "remote");
                } else {
                    map.put("protocol", "dubbo");
                }
            }
            URL url = UrlUtils.parseURL(address, map);
            registryMetadataClient = new CuratorMetadataClient(url, zookeeperTransporter);
        } else {
            throw new IllegalArgumentException("Specify registry address is illegal.");
        }
    }

    /**
     * 初始化 refer/white list listener,并开始 get metadata.
     */
    private void initConfigListener() {
        exportServiceManager = ExportServiceManager.getInstance();
        exportServiceManager.setRegistryMetadataClient(registryMetadataClient);

        Properties properties = configService.getConfig(exportServiceManager);
        //这一步就去获取元数据信息了
        exportServiceManager.notify(properties);
    }


    private CompletableFuture<String> send(RpcRequest request) {
        GateWayService gateWayService = GatewayServiceFactory.create(request);
        gateWayService.invoke(request.getMethod(), request.getParamsType(), request.getParamsValue());

        return RpcContext.getContext().getCompletableFuture();
    }
}
