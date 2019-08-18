package com.yunji.gateway.core;

import com.yunji.gateway.GateWayService;
import com.yunji.gateway.GatewayServiceFactory;
import com.yunji.gateway.config.DiamondBean;
import com.yunji.gateway.config.DiamondConfigService;
import com.yunji.gateway.metadata.discovery.CuratorMetadataClient;
import com.yunji.gateway.metadata.re.ExportServiceManager;
import com.yunji.gateway.util.GateConstants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.RpcRequest;
import com.yunji.gateway.metadata.OptimizedMetadata;
import com.yunji.gateway.metadata.tag.DataType;
import com.yunji.gateway.metadata.tag.Field;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.remoting.zookeeper.ZookeeperTransporter;
import org.apache.dubbo.rpc.RpcContext;
import com.yunji.gateway.util.MixUtils;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;


/**
 * @author Denim.leihz 2019-07-31 6:44 PM
 */
public class DubboExecutedFacade {

    private ZookeeperTransporter zookeeperTransporter = ExtensionLoader
            .getExtensionLoader(ZookeeperTransporter.class)
            .getAdaptiveExtension();

    private final String registryUrl;

    private final String dataId;

    //    private final MetadataResolver metadataResolver;
    //Application 名称
    private final String applicationName;

    //外部化配置
    private ConfigService configService;

    //可暴露服务监听器
    private ExportServiceManager exportServiceManager;

    //zookeeper client
    private RegistryMetadataClient registryMetadataClient;


    public DubboExecutedFacade(String registryUrl, String dataId) {
        this(registryUrl, dataId, GateConstants.DEFAULT_APPLICATION_NAME);
    }

    public DubboExecutedFacade(String registryUrl, String dataId, String applicationName) {
        this(registryUrl, dataId, applicationName, true);
    }

    public DubboExecutedFacade(String registryUrl, String dataId, boolean needInitMetadata) {
        this(registryUrl, dataId, GateConstants.DEFAULT_APPLICATION_NAME, needInitMetadata);
    }

    public DubboExecutedFacade(String registryUrl, String dataId, String applicationName, boolean needInitMetadata) {
        this.registryUrl = registryUrl;
        this.dataId = dataId;
//        this.metadataResolver = MixUtils.getSupportedExtension(MetadataResolver.class);
        this.applicationName = applicationName;
        init(needInitMetadata);
    }

    /**
     * @param needInitMetadata 是否需要初始化元数据搜集信息
     */
    public void init(boolean needInitMetadata) {
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
        OptimizedMetadata.OptimizedService optimizedService = exportServiceManager.getMetadata(interfaceName, version);

        return execute(interfaceName, methodName, version, requestJson, optimizedService);
    }

    public CompletableFuture<String> execute(String interfaceName, String methodName, String version,
                                             String requestJson, OptimizedMetadata.OptimizedService optimizedService) {
        if (optimizedService != null) {
            List<Field> requestFields = optimizedService.getMethodMap().get(methodName).request.fields;

            String[] parameterTypes = new String[requestFields.size()];
            for (int i = 0; i < requestFields.size(); i++) {
                parameterTypes[i] = getDataKindType(requestFields.get(i).dataType);
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
            registryConfig.setProtocol(GateConstants.REGISTEY_PROTOCOL);
            registryConfig.setAddress(registryUrl);
            application.setRegistry(registryConfig);
        }

        ApplicationConfigHolder.setApplication(application);
    }

    /**
     * 初始化外部化配置,首先加载 ConfigService 的 SPI 实现类，如果没有，则使用 默认的 DiamondConfigService.
     */
    private void initConfigService() {
        //Get时即初始化
        configService = MixUtils.getServiceInstance(ConfigService.class);

        if (configService == null) {
            configService = new DiamondConfigService();
            DiamondBean diamondBean = new DiamondBean();
            diamondBean.setDataId(dataId);
            configService.start(diamondBean);
        }
    }

    /**
     * init 去注册中心获取数据并监听的 zookeeper registry client
     */
    private void intZookeeperClient() {
        Assert.notNull(registryUrl, "RegistryUrl == null");
        String urlStr = "zookeeper://" + registryUrl + "/org.apache.services.registry.RegistryService?services=2.0.2&interface=org.apache.services.registry.RegistryService";
        URL url = URL.valueOf(urlStr);

        registryMetadataClient = new CuratorMetadataClient(url, zookeeperTransporter);
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


    private static String getDataKindType(DataType dataType) {
        String qualifiedName = dataType.qualifiedName;
        if (qualifiedName != null) {
            return qualifiedName;
        }
        DataType.KIND kind = dataType.kind;

        switch (kind) {
            case VOID:
                return "java.lang.Void";
            case BOOLEAN:
                return "java.lang.Boolean";
            case BYTE:
                return "java.lang.Byte";
            case SHORT:
                return "java.lang.Short";
            case INTEGER:
                return "java.lang.Integer";
            case LONG:
                return "java.lang.Long";
            case DOUBLE:
                return "java.lang.Double";
            case STRING:
                return "java.lang.String";
            case BINARY:
                return null;
            case MAP:
                return "java.util.Map";
            case LIST:
                return "java.lang.List";
            case SET:
                return "java.lang.Set";
            case ENUM:
                return "java.lang.Enum";
            case STRUCT:
                return null;
            case DATE:
                return "java.util.Date";
            case BIGDECIMAL:
                return "java.math.BigDecimal";

            default:
                return null;
        }
    }
}
