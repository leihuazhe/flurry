package com.yunji.gateway.metadata;

import com.yunji.gateway.metadata.tag.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Denim.leihz 2019-08-19 8:51 PM
 */
public class OptimizedService {
    public final Service service;
    public final Map<String, OptimizedStruct> optimizedStructs = new HashMap<>(1024);

    private final Map<String, Method> methodMap = new HashMap<>(128);
    private final Map<String, TEnum> enumMap = new HashMap<>(128);

    public OptimizedService(Service service) {
        this.service = service;
        for (Struct struct : service.structDefinitions) {
            optimizedStructs.put(struct.namespace + "." + struct.name, new OptimizedStruct(struct));
        }
        if (service.enumDefinitions != null) {
            for (TEnum tEnum : service.enumDefinitions) {
                enumMap.put(tEnum.namespace + "." + tEnum.name, tEnum);
            }
        }
        for (Method method : service.methods) {
            methodMap.put(method.name, method);
            optimizedStructs.put(method.request.namespace + "." + method.request.name, new OptimizedStruct(method.request));

            optimizedStructs.put(method.request.name + ".body", wrapperReq(method));
            optimizedStructs.put(method.response.namespace + "." + method.response.name, new OptimizedStruct(method.response));
        }
    }

    public Service getService() {
        return service;
    }

    public Map<String, Method> getMethodMap() {
        return Collections.unmodifiableMap(methodMap);
    }

    public Map<String, OptimizedStruct> getOptimizedStructs() {
        return Collections.unmodifiableMap(optimizedStructs);
    }

    public Map<String, TEnum> getEnumMap() {
        return Collections.unmodifiableMap(enumMap);
    }


    public OptimizedStruct getOptimizedStruct(String qualifiedName) {
        return optimizedStructs.get(qualifiedName);
    }

    private OptimizedStruct wrapperReq(Method method) {
        Struct reqWrapperStruct = new Struct();
        reqWrapperStruct.name = "body";
        reqWrapperStruct.namespace = method.name;
        reqWrapperStruct.fields = new ArrayList<>(2);
        Field reqField = new Field();
        reqField.tag = 0;
        DataType reqDataType = new DataType();
        reqDataType.kind = DataType.KIND.STRUCT;
        reqDataType.qualifiedName = method.request.name;
        reqField.dataType = reqDataType;
        reqWrapperStruct.fields.add(reqField);

        return new OptimizedStruct(reqWrapperStruct);
    }
}