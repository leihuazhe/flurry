package org.apache.dubbo.gateway.core;

import org.apache.dubbo.gateway.GateWayService;
import org.apache.dubbo.gateway.GatewayServiceFactory;
import org.apache.dubbo.metadata.MetadataResolver;
import org.apache.dubbo.config.RpcRequest;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.metadata.OptimizedMetadata;
import org.apache.dubbo.metadata.tag.DataType;
import org.apache.dubbo.metadata.tag.Field;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.util.GatewayUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Denim.leihz 2019-07-31 6:44 PM
 */
public class DubboAsyncClient {

    private MetadataResolver metadataResolver;

    {
        metadataResolver = GatewayUtil.getSupportedExtension(MetadataResolver.class);
    }

    public CompletableFuture<String> execute(String interfaceName, String methodName, String version, String requestJson) {

        OptimizedMetadata.OptimizedService optimizedService = metadataResolver.get(interfaceName, version);
        if (optimizedService != null) {
            List<Field> requestFields = optimizedService.getMethodMap().get(methodName).request.fields;

            String[] parameterTypes = new String[requestFields.size()];
            for (int i = 0; i < requestFields.size(); i++) {
                parameterTypes[i] = getDataKindType(requestFields.get(i).dataType);
            }

            RpcRequest rpcRequest = RpcRequest.builder()
                    .serviceName(interfaceName)
                    .method(methodName)
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
