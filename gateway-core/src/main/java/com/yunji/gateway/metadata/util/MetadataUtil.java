package com.yunji.gateway.metadata.util;

import org.apache.dubbo.common.utils.StringUtils;
import com.yunji.gateway.jsonserializer.TType;
import com.yunji.gateway.metadata.tag.DataType;
import com.yunji.gateway.metadata.tag.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Denim.leihz 2019-07-23 6:06 PM
 */
public class MetadataUtil {
    private static Logger logger = LoggerFactory.getLogger(MetadataUtil.class);

    private static final String WHITE_SERVICES_KEY = "gateway.white.service.list";

    public static String getServiceKey(Service service) {
        return getServiceKey(service.getName(), service.getMeta().version);
    }

    public static String getServiceFullNameKey(Service service) {
        return getServiceKey(service.getNamespace() + "." + service.getName(), service.getMeta().version);
    }

    public static String getServiceKey(String name, String version) {
        return name + ":" + version;
    }

    public static byte dataType2Byte(DataType type) {
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


    public static List<String> parseConfig(String message) throws Exception {
        if (StringUtils.isNotEmpty(message)) {
            Properties properties = new Properties();
            properties.load(new StringReader(message));

            if (!properties.isEmpty()) {
                String servicesStr = properties.getProperty(WHITE_SERVICES_KEY);

                if (StringUtils.isNotEmpty(servicesStr)) {
                    String[] split = servicesStr.split(",");
                    return Arrays.asList(split);
                }
            }
        }
        return new ArrayList<>();
    }

    /*public static void initMetadata(String dataId, String registryUrl, MetadataResolver resolver) {
        try {
            ConfigContext context = new ConfigContext();
            DiamondClient diamondClient = diamondInit(dataId, context);
            List<String> serviceList = MetadataUtil.parseConfig(diamondClient.getConfig());
            context.setWhiteServiceSet(serviceList);
            context.setRegistryUrl(registryUrl);

            context.addListener(resolver);
            context.refresh();
        } catch (Exception e) {
            logger.error("Init diamondClient error,cause " + e.getMessage(), e);
        }

    }*/

   /* private static DiamondClient diamondInit(String dataId, ConfigContext context) {
        DiamondClient diamondClient = new DiamondClient();
        diamondClient.setDataId(dataId);
        diamondClient.setPollingIntervalTime(GateConstants.POLLING_INTERVAL_TIME);
        diamondClient.setTimeout(GateConstants.DIAMOND_TIME_OUT);
        diamondClient.init();
        diamondClient.setManagerListener(new WhiteServiceManagerListener(context));

        return diamondClient;
    }*/
}
