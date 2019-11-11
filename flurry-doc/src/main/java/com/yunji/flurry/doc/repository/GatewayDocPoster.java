package com.yunji.flurry.doc.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.yunji.flurry.doc.util.MixUtils;
import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.tag.Method;
import com.yunji.flurry.process.Post;
import com.yunji.flurry.metadata.core.ExportServiceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public abstract class GatewayDocPoster implements Post {
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public String post(String service,
                       String version,
                       String method,
                       String parameter,
                       HttpServletRequest req) throws Exception {
        return post(service, version, method, parameter);
    }

    private String post(String service,
                        String version,
                        String method,
                        String parameter) throws Exception {

        if (MixUtils.ECHO_NAME.equals(method)) {
            return executeEcho(service, version);
        }

        OptimizedService bizService = ExportServiceManager.getInstance().getMetadata(service, version);
        if (bizService == null) {
            logger.error("bizService not found[service:" + service + ", version:" + version + "]");
            return String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", 500, "No matched service", "{}");
        }
        return doPost(service, bizService, method, version, parameter);
    }

    protected abstract String executeEcho(String service, String version) throws Exception;

    protected abstract String doPost(String service,
                                     OptimizedService bizService,
                                     String method,
                                     String finalVersion,
                                     String parameter);


    /**
     * method1(Long args0, Long args1) => Array[]{1,1}
     * method2(Object s) =>  map<String, Object></>
     * method3(Long args0, Object s) => Array[]{1, Map<String, Object>}
     *
     * @param service
     * @param method
     * @param request
     * @param objects
     * @return
     */
    public List<Object> toRequestParams(com.yunji.flurry.metadata.tag.Service service, com.yunji.flurry.metadata.tag.Method method, JSON request, List<Object> objects) {
        List<com.yunji.flurry.metadata.tag.Field> fields = method.getRequest().getFields();
        for (com.yunji.flurry.metadata.tag.Field field : fields) {
            Object o = ((JSONObject) request).get(field.name);
            if (o == null) {
                throw new IllegalArgumentException("Json 格式不正確, 找不到对应的结构体. o: " + request + " fieldName: " + field.name);
            }
            switch (field.getDataType().kind) {
                case STRING:
                    objects.add(o.toString());
                    break;
                case LONG:
                    if (o != null && !o.toString().isEmpty()) {
                        objects.add(Long.valueOf(o.toString()));
                    }
                    break;
                case INTEGER:
                    if (o != null && !o.toString().isEmpty()) {
                        objects.add(Integer.valueOf(o.toString()));
                    }
                    break;
                case DOUBLE:
                    if (o != null && !o.toString().isEmpty()) {
                        objects.add(Double.valueOf(o.toString()));
                    }
                    break;
                case BOOLEAN:
                    objects.add(Boolean.valueOf(o.toString()));
                    break;
                case BYTE:
                    objects.add(Byte.valueOf(o.toString()));
                    break;
                //return Byte.valueOf(o.toString());
                case LIST:
                    JSONArray jsonArray = (JSONArray) o;
                    List<Map<String, Object>> params = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Map<String, Object> param = new HashMap<>();
                        params.add(param);
                        toRequestFieldParam(service, method, field, field.getDataType().valueType, param, jsonArray.get(i));
                    }
                    objects.add(params);
//                    return (Object)params;
                default:
                    Map<String, Object> objParam = new HashMap<>();
                    toRequestFieldParam(service, method, field, field.getDataType(), objParam, o);
                    objects.add(objParam);
//                    return (Object)objParam;
            }

        }
        return objects;
    }

    private List<Object> toRequestFieldParam(com.yunji.flurry.metadata.tag.Service service, com.yunji.flurry.metadata.tag.Method method, com.yunji.flurry.metadata.tag.Field field,
                                             com.yunji.flurry.metadata.tag.DataType dataType, Map<String, Object> mParams, List<Object> lParams, Object fieldValue) {
        switch (dataType.kind) {
            case STRING:
                lParams.add(fieldValue.toString());
                break;
            case LONG:
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    lParams.add(Long.valueOf(fieldValue.toString()));
                }
                break;
            case INTEGER:
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    lParams.add(Integer.valueOf(fieldValue.toString()));
                }
                break;
            case DOUBLE:
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    lParams.add(Double.valueOf(fieldValue.toString()));
                }
                break;
            case BOOLEAN:
                lParams.add(Boolean.valueOf(fieldValue.toString()));
                break;
            case BYTE:
                lParams.add(Byte.valueOf(fieldValue.toString()));
                break;
            case LIST:
                JSONArray jsonArray = (JSONArray) fieldValue;
                List<Object> arr = new ArrayList<>();
                lParams.add(arr);
                mParams.put(field.name, lParams);
                for (int i = 0; i < jsonArray.size(); i++) {
                    toRequestFieldParam(service, method, field, field.getDataType().valueType, mParams, arr, jsonArray.get(i));
                }
                break;
            case MAP:
                JSONObject mapNode = (JSONObject) fieldValue;
                mapNode(service, method, field, mParams, mapNode);
                break;
            case STRUCT:
                mParams.put("class", dataType.qualifiedName);
                List<com.yunji.flurry.metadata.tag.Struct> structs = service.structDefinitions;
                List<com.yunji.flurry.metadata.tag.Struct> targetStruct = structs.stream().filter(i -> (i.namespace + '.' + i.name).equals(dataType.qualifiedName)).collect(Collectors.toList());
                if (targetStruct.size() == 0) {
                    throw new IllegalArgumentException("解析Json异常， 找不到对应结构体的元数据: " + dataType.qualifiedName);
                }
                com.yunji.flurry.metadata.tag.Struct struct = targetStruct.get(0);
                List<com.yunji.flurry.metadata.tag.Field> fields = struct.getFields();
                for (com.yunji.flurry.metadata.tag.Field f : fields) {
                    Object fJson = (fieldValue instanceof JSONObject) ? ((JSONObject) fieldValue).get(f.name) : fieldValue;
                    toRequestFieldParam(service, method, f, f.getDataType(), mParams, fJson);
                }
                break;
            default:
                logger.warn(" unsupported dataType: " + dataType.kind.name());
                break;
        }
        return lParams;

    }


    private Map<String, Object> toRequestFieldParam(com.yunji.flurry.metadata.tag.Service service, com.yunji.flurry.metadata.tag.Method method, com.yunji.flurry.metadata.tag.Field field,
                                                    com.yunji.flurry.metadata.tag.DataType dataType, Map<String, Object> params, Object fieldValue) {
        switch (dataType.kind) {
            case STRING:
                params.put(field.name, fieldValue.toString());
                break;
            case LONG:
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    params.put(field.name, Long.valueOf(fieldValue.toString()));
                }
                break;
            case INTEGER:
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    params.put(field.name, Integer.valueOf(fieldValue.toString()));
                }
                break;
            case DOUBLE:
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    params.put(field.name, Double.valueOf(fieldValue.toString()));
                }
                break;
            case BOOLEAN:
                params.put(field.name, Boolean.valueOf(fieldValue.toString()));
                break;
            case BYTE:
                params.put(field.name, Byte.valueOf(fieldValue.toString()));
                break;
            case LIST:
                JSONArray jsonArray = (JSONArray) fieldValue;
                List<Object> arr = new ArrayList<>();
                params.put(field.name, arr);
                for (int i = 0; i < jsonArray.size(); i++) {
                    toRequestFieldParam(service, method, field, field.getDataType().valueType, params, arr, jsonArray.get(i));
                }
                break;
            case MAP:
                JSONObject mapNode = (JSONObject) fieldValue;
                mapNode(service, method, field, params, mapNode);
                break;
            case STRUCT:
                params.put("class", dataType.qualifiedName);
                List<com.yunji.flurry.metadata.tag.Struct> structs = service.structDefinitions;
                List<com.yunji.flurry.metadata.tag.Struct> targetStruct = structs.stream().filter(i -> (i.namespace + '.' + i.name).equals(dataType.qualifiedName)).collect(Collectors.toList());
                if (targetStruct.size() == 0) {
                    throw new IllegalArgumentException("解析Json异常， 找不到对应结构体的元数据: " + dataType.qualifiedName);
                }
                com.yunji.flurry.metadata.tag.Struct struct = targetStruct.get(0);
                List<com.yunji.flurry.metadata.tag.Field> fields = struct.getFields();
                for (com.yunji.flurry.metadata.tag.Field f : fields) {
                    Object fJson = (fieldValue instanceof JSONObject) ? ((JSONObject) fieldValue).get(f.name) : fieldValue;
                    toRequestFieldParam(service, method, f, f.getDataType(), params, fJson);
                }
                break;
            default:
                logger.warn(" unsupported dataType: " + dataType.kind.name());
                break;
        }
        return params;
    }


    private void mapNode(com.yunji.flurry.metadata.tag.Service service, Method method, com.yunji.flurry.metadata.tag.Field field, Map<String, Object> params, JSONObject mapNode) {
        mapNode.forEach((key, value) -> {
            //toRequestFieldParam(service, method, field, field.getDataType().getKeyType(),params,i.getKey());
            Map<String, Object> values = new HashMap<>();
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put(key, values);
            params.put(field.name, objectMap);
            toRequestFieldParam(service, method, field, field.getDataType().getValueType(), values, value);
        });
    }
}

