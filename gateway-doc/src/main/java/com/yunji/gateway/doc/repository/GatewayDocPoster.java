package com.yunji.gateway.doc.repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunji.api.doc.compatible.ServiceInfo;
import com.yunji.api.doc.compatible.SoaCode;
import com.yunji.api.doc.openapi.cache.ZookeeperClient;
import com.yunji.api.doc.openapi.post.Post;
import com.yunji.gateway.metadata.OptimizedMetadata;
import com.yunji.gateway.metadata.auto.ServiceMetadataRepository;
import com.yunji.gateway.metadata.tag.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public abstract class GatewayDocPoster implements Post {

    private static final AtomicInteger serviceGetCounter = new AtomicInteger();

    protected final Logger LOGGER = LoggerFactory.getLogger(com.yunji.api.doc.openapi.post.AbstractPost.class);

    protected String registryUrl;

    protected String diamondId;

    public GatewayDocPoster(String registryUrl, String diamondId) {
        this.registryUrl = registryUrl;
        this.diamondId = diamondId;
    }

    @Override
    public String post(String service,
                       String version,
                       String method,
                       String parameter,
                       HttpServletRequest req) {
        return post(service, version, method, parameter);
    }

    private String post(String service,
                        String version,
                        String method,
                        String parameter) {

        String finalVersion;
        List<ServiceInfo> serviceInfos = ZookeeperClient.getServiceInfosByName(service);
        if (serviceInfos == null || serviceInfos.isEmpty()) {
            finalVersion = (version == null || version.isEmpty()) ? "1.0.0" : version;
        } else {
            //RouteRobin
            finalVersion = serviceInfos.get(serviceGetCounter.getAndIncrement() % serviceInfos.size()).versionName;
        }

        OptimizedMetadata.OptimizedService bizService = ServiceMetadataRepository.getRepository().getService(service, version);

        if (bizService == null) {
            LOGGER.error("bizService not found[service:" + service + ", version:" + version + "]");
            return String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", SoaCode.NoMatchedService.getCode(), SoaCode.NoMatchedService.getMsg(), "{}");
        }

        /*URL serviceURL = ServiceMetadataCache.get().serviceRegistryUrls.get(service);
        if (serviceURL == null) {
            LOGGER.error("registryService not found[service:" + service + ", version:" + version + "]");
            return String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", SoaCode.NoMatchedService.getCode(), SoaCode.NoMatchedService.getMsg(), "{}");
        }
*/
        return doPost(service, bizService, method, finalVersion, parameter);
    }

    protected abstract String doPost(String service,
                                     OptimizedMetadata.OptimizedService bizService,
                                     String method,
                                     String finalVersion,
                                     String parameter);


    /**
     * @param resp
     * @param updatedResp
     * @return
     */


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
    public List<Object> toRequestParams(Service service, Method method, JSON request, List<Object> objects) {
        List<Field> fields = method.getRequest().getFields();
        for (Field field : fields) {
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

    private List<Object> toRequestFieldParam(Service service, Method method, Field field,
                                             DataType dataType, Map<String, Object> mParams, List<Object> lParams, Object fieldValue) {
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
                List<Struct> structs = service.structDefinitions;
                List<Struct> targetStruct = structs.stream().filter(i -> (i.namespace + '.' + i.name).equals(dataType.qualifiedName)).collect(Collectors.toList());
                if (targetStruct.size() == 0) {
                    throw new IllegalArgumentException("解析Json异常， 找不到对应结构体的元数据: " + dataType.qualifiedName);
                }
                Struct struct = targetStruct.get(0);
                List<Field> fields = struct.getFields();
                for (Field f : fields) {
                    Object fJson = (fieldValue instanceof JSONObject) ? ((JSONObject) fieldValue).get(f.name) : fieldValue;
                    toRequestFieldParam(service, method, f, f.getDataType(), mParams, fJson);
                }
                break;
            default:
                LOGGER.warn(" unsupported dataType: " + dataType.kind.name());
                break;
        }
        return lParams;

    }


    private Map<String, Object> toRequestFieldParam(Service service, Method method, Field field,
                                                    DataType dataType, Map<String, Object> params, Object fieldValue) {
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
                List<Struct> structs = service.structDefinitions;
                List<Struct> targetStruct = structs.stream().filter(i -> (i.namespace + '.' + i.name).equals(dataType.qualifiedName)).collect(Collectors.toList());
                if (targetStruct.size() == 0) {
                    throw new IllegalArgumentException("解析Json异常， 找不到对应结构体的元数据: " + dataType.qualifiedName);
                }
                Struct struct = targetStruct.get(0);
                List<Field> fields = struct.getFields();
                for (Field f : fields) {
                    Object fJson = (fieldValue instanceof JSONObject) ? ((JSONObject) fieldValue).get(f.name) : fieldValue;
                    toRequestFieldParam(service, method, f, f.getDataType(), params, fJson);
                }
                break;
            default:
                LOGGER.warn(" unsupported dataType: " + dataType.kind.name());
                break;
        }
        return params;
    }


    private void mapNode(Service service, Method method, Field field, Map<String, Object> params, JSONObject mapNode) {
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

