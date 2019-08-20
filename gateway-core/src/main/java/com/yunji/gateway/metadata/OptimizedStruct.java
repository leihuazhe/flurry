package com.yunji.gateway.metadata;

import com.yunji.gateway.metadata.tag.Field;
import com.yunji.gateway.metadata.tag.Struct;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Denim.leihz 2019-08-19 8:51 PM
 */
public class OptimizedStruct {
    public final Struct struct;

    /**
     *
     */
    public final Map<String, Field> fieldMap = new HashMap<>(128);

    public final int tagBase; // maybe < 0

    /**
     * 数组方式， 更高效，需要注意，
     * 1. 不连续key很大的情况， 例如来了个tag为65546的field
     * 2. 有些结构体定时的时候没填tag， 结果生成元数据的时候就变成了负数
     * <p>
     * 所以目前采用Map的方式
     */
    private final Map<Short, Field> fieldMapByTag;
    private final Field[] fieldArrayByTag;

    public Struct getStruct() {
        return struct;
    }

    public OptimizedStruct(Struct struct) {
        this.struct = struct;

        int tagBase = 0;
        int maxTag = 0;

        for (Field f : struct.fields) {
            this.fieldMap.put(f.name, f);
            if (f.tag < tagBase) tagBase = f.tag;
            if (f.tag > maxTag) maxTag = f.tag;
        }

        this.tagBase = tagBase;
        Field[] array = null;
        Map<Short, Field> map = null;
        if (maxTag - tagBase + 1 <= 256) {
            array = new Field[maxTag - tagBase + 1];
            for (Field f : struct.fields) {
                array[f.tag - tagBase] = f;
            }
        } else {
            map = new HashMap<>();
            for (Field f : struct.fields) {
                map.put((short) f.tag, f);
            }
        }
        this.fieldArrayByTag = array;
        this.fieldMapByTag = map;
    }

    public Field get(short tag) {
        if (fieldArrayByTag != null && tag >= tagBase && tag - tagBase < fieldArrayByTag.length)
            return fieldArrayByTag[tag - tagBase];
        else return fieldMapByTag.get(tag);
    }

    //加入对 hessian2 的序列化模式的优化.
//    private Field[] orderFields = new Field[fieldMap.size()];
//    private int fieldPos = 0;
//
//    public void setOrderField(Field orderField) {
//        this.orderFields[fieldPos++] = orderField;
//    }
//
//    public Map<String, Field> getFieldMap() {
//        return fieldMap;
//    }
}