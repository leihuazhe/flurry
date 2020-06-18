package com.yunji.flurry.metadata.common;

import com.yunji.flurry.GateWayService;
import com.yunji.flurry.GatewayServiceFactory;
import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.tag.DataType;
import com.yunji.flurry.metadata.tag.Service;
import com.yunji.flurry.util.GateConstants;
import com.yunji.flurry.jsonserializer.TType;

import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.MetaServiceInfo;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Denim.leihz 2019-07-23 6:06 PM
 */
public class MetadataUtil {
    private static Logger logger = LoggerFactory.getLogger(MetadataUtil.class);

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 支持重试的模式获取 OptimizedService
     *
     * @param serviceName 服务接口全限定名
     * @param version     服务版本信息
     * @param group       服务分组,一般为 nulls
     * @return 服务元数据信息
     */
    public static CompletableFuture<com.yunji.flurry.metadata.OptimizedService> callServiceMetadataAsync(String serviceName, String version, String group) {
        return CompletableFuture.supplyAsync(() -> callServiceMetadata(serviceName, version, group), executorService);
    }

    /**
     * 支持重试的模式获取 OptimizedService
     *
     * @param serviceName 服务接口全限定名
     * @param version     服务版本信息
     * @param group       服务分组,一般为 null
     * @return 服务元数据信息
     */
    public static com.yunji.flurry.metadata.OptimizedService callServiceMetadata(String serviceName, String version, String group) {
        int retry = GateConstants.DEFAULT_RETRY;
        for (int i = 0; i < retry; i++) {
            try {
                GateWayService gateWayService = GatewayServiceFactory.create(
                        MetaServiceInfo.builder()
                                .serviceName(serviceName)
                                .methodName(GateConstants.METADATA_METHOD_NAME)
                                .version(version)
                                .group(group)
                                .build());
                //Invoke.
                gateWayService.invoke(GateConstants.METADATA_METHOD_NAME, new String[]{}, new Object[]{});

                String metaString = (String) RpcContext.getContext()
                        .getCompletableFuture()
                        .get(GateConstants.METADATA_CALL_TIME_OUT, TimeUnit.MILLISECONDS);

                if (StringUtils.isNotEmpty(metaString)) {
                    try (StringReader reader = new StringReader(metaString)) {
                        com.yunji.flurry.metadata.tag.Service service = JAXB.unmarshal(reader, com.yunji.flurry.metadata.tag.Service.class);
                        return new OptimizedService(service);
                    }
                } else {
                    logger.error("订阅目标服务 {} 元数据获取为空,请检查.", serviceName);
                    return null;
                }

            } catch (ExecutionException tx) {
                String detailMsg = tx.getMessage();
                if (detailMsg != null && detailMsg.contains("org.apache.dubbo.common.bytecode.NoSuchMethodException")) {
                    logger.error("目标服务:{} 没有增强方法 _getServiceMetadata,无法获取到服务元数据.", serviceName);
                    return null;
                }
                logger.error("[" + serviceName + "]: ResolveServiceMetadata get error: " + tx.getMessage(), tx);
            } catch (Exception e) {
                logger.error("[" + serviceName + "]: ResolveServiceMetadata get error: " + e.getMessage(), e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        logger.error("目标:{} 获取服务元数据信息失败,一共调用失败 {} 次", serviceName, retry);

        return null;
    }

    /**
     * 根据配置信息获取到需要引用的服务信息,支持10个级别
     *
     * @param properties 配置信息 WHITE_SERVICES_KEY
     * @return 需要引用的服务接口 list
     */
    public static Set<String> getReferService(Properties properties) {
        Assert.notNull(properties, "ConfigServer 获取到的外部化配置信息为空，请检查并配置相关信息.");
        String baseKey = GateConstants.WHITE_SERVICES_KEY;
        String whiteStr = properties.getProperty(baseKey);
        char ch = ',';

        if (StringUtils.isNotEmpty(whiteStr)) {
            Set<String> list = new HashSet<>();
            char c;
            int ix = 0, len = whiteStr.length();
            for (int i = 0; i < len; i++) {
                c = whiteStr.charAt(i);
                if (c == ch) {
                    list.add(whiteStr.substring(ix, i));
                    ix = i + 1;
                }
            }

            if (ix >= 0) {
                list.add(whiteStr.substring(ix));
            }
            return list;
        } else {
//            throw new IllegalArgumentException("White list String is empty. Please specify the list string on config server.");
            logger.warn("White list String is empty. Please specify the list string on config server.");
            //返回空
            return new HashSet<>();
        }
    }


    public static byte dataType2Byte(com.yunji.flurry.metadata.tag.DataType type) {
        switch (type.kind) {
            case BOOLEAN:
                return TType.BOOL;

            case BYTE:
                return TType.BYTE;

            case DOUBLE:
                return TType.DOUBLE;

            case SHORT:
                return TType.I16;

            case INTEGER:
                return TType.I32;

            case LONG:
                return TType.I64;

            case STRING:
                return TType.STRING;

            case STRUCT:
                return TType.STRUCT;

            case MAP:
                return TType.MAP;

            case SET:
                return TType.SET;

            case LIST:
                return TType.LIST;

            case ENUM:
                return TType.I32;

            case VOID:
                return TType.VOID;

            case DATE:
                return TType.I64;

            case BIGDECIMAL:
                return TType.STRING;

            case BINARY:
                return TType.STRING;

            default:
                break;
        }

        return TType.STOP;
    }

    public static String getDataKindType(com.yunji.flurry.metadata.tag.DataType dataType) {
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
                return "java.util.List";
            case SET:
                return "java.util.Set";
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


    public static String getServiceKey(com.yunji.flurry.metadata.tag.Service service) {
        return getServiceKey(service.getName(), service.getMeta().version);
    }

    public static String getServiceFullNameKey(Service service) {
        return getServiceKey(service.getNamespace() + "." + service.getName(), service.getMeta().version);
    }

    public static String getServiceKey(String name, String version) {
        return name + ":" + version;
    }
}
