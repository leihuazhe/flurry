package com.yunji.flurry.netty.http.request;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author maple httpRequest请求参数
 * @since 2018年08月23日 上午10:01
 */
public final class RequestParser {

    private RequestParser() {
    }

    /**
     * 解析请求参数
     */
    public static Map<String, String> parse(FullHttpRequest req) {
        Map<String, String> params = new HashMap<>();
        // 是POST请求
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);
        List<InterfaceHttpData> postList = decoder.getBodyHttpDatas();
        for (InterfaceHttpData data : postList) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }
        // resolve memory leak
        decoder.destroy();
        return params;
    }

    /**
     * parse http params
     */
    public static void fastParse(FullHttpRequest httpRequest, RequestContext context) {
        String content = httpRequest.content().toString(StandardCharsets.UTF_8);
        QueryStringDecoder qs = new QueryStringDecoder(content, StandardCharsets.UTF_8, false);
        Map<String, List<String>> parameters = qs.parameters();

        List<String> defaultStr = new ArrayList<>();

        defaultStr.add("");
        String serviceName = parameters.getOrDefault("serviceName", defaultStr).get(0);
        String version = parameters.getOrDefault("version", defaultStr).get(0);
        String methodName = parameters.getOrDefault("methodName", defaultStr).get(0);

        String parameter = parameters.getOrDefault("parameter", defaultStr).get(0);

        String timestamp = parameters.getOrDefault("timestamp", defaultStr).get(0);

        String secret = parameters.getOrDefault("secret", defaultStr).get(0);
        String secret2 = parameters.getOrDefault("secret2", defaultStr).get(0);

        context.service(serviceName);
        context.version(version);
        context.method(methodName);
        context.parameter(parameter);

        context.timestamp(timestamp);
        context.secret(secret);
        context.secret2(secret2);
        context.parameter(parameter);


    }


    /**
     * 解析 http 请求携带参数
     *
     * @param httpRequest
     * @param condition
     * @return
     */
    public static String fastParseParam(FullHttpRequest httpRequest, String condition) {
//        String contentType = HttpUtil.getMimeType(httpRequest).toString();
        String contentType = HttpUtil.getMimeType(httpRequest).toString();

        if ("application/json".equals(contentType)) {
            return httpRequest.content().toString(StandardCharsets.UTF_8);
        }
        String content = httpRequest.content().toString(StandardCharsets.UTF_8);
        QueryStringDecoder qs = new QueryStringDecoder(content, StandardCharsets.UTF_8, false);
        Map<String, List<String>> parameters = qs.parameters();
        List<String> result = parameters.get(condition);

        if (result != null) {
            return result.get(0);
        }
        return null;
    }

    /**
     * 解析post请求参数
     */
    /*private Map<String, List<String>> decodePostParams(FullHttpRequest request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
                new DefaultHttpDataFactory(false), request);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas(); //
        if (postData.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> params = new LinkedHashMap<>(postData.size());
        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                // Often there's only 1 value.
                List<String> values = params.computeIfAbsent(attribute.getName(), k -> new ArrayList<String>(1));
                values.add(attribute.getValue());
            }
        }
        return params;
    }*/


    /**
     * parse http params
     */
    public static Map<String, List<String>> fastParseToMap(FullHttpRequest httpRequest) {
        String content = httpRequest.content().toString(StandardCharsets.UTF_8);
        QueryStringDecoder qs = new QueryStringDecoder(content, StandardCharsets.UTF_8, false);
        return qs.parameters();
    }


}