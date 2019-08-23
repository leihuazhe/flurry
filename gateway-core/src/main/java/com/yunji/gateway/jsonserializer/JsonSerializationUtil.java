package com.yunji.gateway.jsonserializer;

import com.yunji.gateway.metadata.OptimizedService;
import com.yunji.gateway.metadata.OptimizedStruct;
import com.yunji.gateway.metadata.tag.DataType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author Denim.leihz 2019-07-08 8:35 PM
 */
public class JsonSerializationUtil {
    private static DateTimeFormatter standardFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 暂时只支持key为整形或者字符串的map
     */
    public static boolean isValidMapKeyType(DataType.KIND kind) {
        return kind == DataType.KIND.INTEGER || kind == DataType.KIND.LONG
                || kind == DataType.KIND.SHORT || kind == DataType.KIND.STRING;
    }

    /**
     * 是否集合类型
     */
    public static boolean isCollectionKind(DataType.KIND kind) {
        return kind == DataType.KIND.LIST || kind == DataType.KIND.SET;
    }

    /**
     * 是否容器类型
     */
    public static boolean isMultiElementKind(DataType.KIND kind) {
        return isCollectionKind(kind) || kind == DataType.KIND.MAP;
    }

    /**
     * 是否复杂类型
     */
    public static boolean isComplexKind(DataType.KIND kind) {
        return isMultiElementKind(kind) || kind == DataType.KIND.STRUCT;
    }


    public static long parseDateString(String date) {
        LocalDateTime dateTime;
        try {

            dateTime = LocalDateTime.parse(date, standardFormat);
        } catch (Exception e) {

            dateTime = LocalDateTime.parse(date, dateFormat);
        }

        return dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    //======
    // Json Reader util 方法
    //======
    public static OptimizedStruct getOptimizedStruct(OptimizedService optimizedService, String namespace, String name) {
        return getOptimizedStruct(optimizedService, namespace + "." + name);
    }

    public static OptimizedStruct getOptimizedStruct(OptimizedService optimizedService, String qualifierName) {
        return optimizedService.optimizedStructs.get(qualifierName);
    }
}
