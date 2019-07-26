package org.apache.dubbo.metadata.util;

import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.jsonserializer.util.TType;
import org.apache.dubbo.metadata.tag.DataType;
import org.apache.dubbo.metadata.tag.Service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Denim.leihz 2019-07-23 6:06 PM
 */
public class MetadataUtil {

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
}
