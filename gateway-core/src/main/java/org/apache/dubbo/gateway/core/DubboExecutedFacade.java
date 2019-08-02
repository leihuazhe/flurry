package org.apache.dubbo.gateway.core;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.gateway.GateWayService;
import org.apache.dubbo.gateway.GatewayServiceFactory;
import org.apache.dubbo.metadata.MetadataResolver;
import org.apache.dubbo.config.RpcRequest;
import org.apache.dubbo.metadata.OptimizedMetadata;
import org.apache.dubbo.metadata.tag.DataType;
import org.apache.dubbo.metadata.tag.Field;
import org.apache.dubbo.metadata.util.MetadataUtil;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.util.GatewayUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.apache.dubbo.util.GateConstants.GATEWAY_APPLICATION_NAME;
import static org.apache.dubbo.util.GateConstants.REGISTEY_PROTOCOL;


/**
 * @author Denim.leihz 2019-07-31 6:44 PM
 */
public class DubboExecutedFacade {

    private final String registryUrl;

    private final String dataId;

    private final MetadataResolver metadataResolver;

    public DubboExecutedFacade(String registryUrl, String dataId, boolean needInitMetadata) {
        this.registryUrl = registryUrl;
        this.dataId = dataId;
        this.metadataResolver = GatewayUtil.getSupportedExtension(MetadataResolver.class);

        init(needInitMetadata);
    }

    /**
     * @param needInitMetadata 是否需要初始化元数据搜集信息
     */
    public void init(boolean needInitMetadata) {
        ApplicationConfig application = new ApplicationConfig();
        application.setName(GATEWAY_APPLICATION_NAME);
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setProtocol(REGISTEY_PROTOCOL);
        registryConfig.setAddress(registryUrl);
        application.setRegistry(registryConfig);

        ApplicationConfigHolder.setApplication(application);

        if (needInitMetadata) {
            MetadataUtil.initMetadata(dataId, registryUrl, metadataResolver);
        }
    }


    public CompletableFuture<String> execute(String interfaceName, String methodName, String version, String requestJson) {
        OptimizedMetadata.OptimizedService optimizedService = metadataResolver.get(interfaceName, version);

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
