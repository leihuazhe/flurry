package com.yunji.flurry.netty.http.request;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author maple 2018.09.18 上午10:20
 */
public class RequestContext {
    /**
     * httpRequest
     */
    private FullHttpRequest request;
    /**
     * 请求方式
     */
    private HttpMethod httpMethod;
    /**
     * 请求 url
     */
    private String requestUrl;
    /**
     * url prefix
     */
    private String urlPrefix;

    /**
     * 当前请求是否合法
     */
    private boolean isLegal = true;

    /**
     * 当前请求可能抛的异常及原因
     */
    private Optional<String> cause = Optional.empty();

    /**
     * service
     */
    private Optional<String> service = Optional.empty();

    /**
     * version
     */
    private Optional<String> version = Optional.empty();
    /**
     * method
     */
    private Optional<String> method = Optional.empty();
    /**
     * api-key
     */
    private Optional<String> apiKey = Optional.empty();
    /**
     * timestamp
     */

    private Optional<String> timestamp = Optional.empty();
    /**
     * secret
     */
    private Optional<String> secret = Optional.empty();
    /**
     * secret2
     */
    private Optional<String> secret2 = Optional.empty();
    /**
     * parameter
     */
    private Optional<String> parameter = Optional.empty();

    /**
     * arguments map
     */
    private Map<String, String> arguments = new HashMap<>();

    /**
     * cookies map
     */
    private Set<Cookie> cookies;


    public FullHttpRequest request() {
        return request;
    }

    public void request(FullHttpRequest request) {
        this.request = request;
    }

    public HttpMethod httpMethod() {
        return httpMethod;
    }

    public void httpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String requestUrl() {
        return requestUrl;
    }

    public void requestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String urlPrefix() {
        return urlPrefix;
    }

    public void urlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }


    public boolean isLegal() {
        return isLegal;
    }

    public void isLegal(boolean legal) {
        isLegal = legal;
    }

    public Optional<String> cause() {
        return cause;
    }

    public void cause(String cause) {
        this.cause = Optional.ofNullable(cause);
    }

    public Optional<String> service() {
        return service;
    }

    public void service(String service) {
        this.service = Optional.ofNullable(service);
    }

    public Optional<String> version() {
        return version;
    }

    public void version(String version) {
        this.version = Optional.ofNullable(version);
    }

    public Optional<String> method() {
        return method;
    }

    public void method(String method) {
        this.method = Optional.ofNullable(method);
    }

    public Optional<String> apiKey() {
        return apiKey;
    }

    public void apiKey(String apiKey) {
        this.apiKey = Optional.ofNullable(apiKey);
    }

    public Optional<String> timestamp() {
        return timestamp;
    }

    public void timestamp(String timestamp) {
        this.timestamp = Optional.ofNullable(timestamp);
    }

    public Optional<String> secret() {
        return secret;
    }

    public void secret(String secret) {
        this.secret = Optional.ofNullable(secret);
    }

    public Optional<String> secret2() {
        return secret2;
    }

    public void secret2(String secret2) {
        this.secret2 = Optional.ofNullable(secret2);
    }

    public Optional<String> parameter() {
        return parameter;
    }

    public void parameter(String parameter) {
        this.parameter = Optional.ofNullable(parameter);
    }

    public Map<String, String> arguments() {
        return arguments;
    }

    public void arguments(Map<String, String> arguments) {
        this.arguments.putAll(arguments);
    }

    public Set<Cookie> cookies() {
        return cookies;
    }

    public void cookies(Set<Cookie> cookies) {
        this.cookies = cookies;
    }

    public String argumentToString() {
        return arguments.entrySet().stream()
                .map(argument -> "KV:[" + argument.getKey() + " -> " + argument.getValue() + "]")
                .collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return "RequestContext{" +
                "request=" + request +
                ", httpMethod=" + httpMethod +
                ", requestUrl='" + requestUrl + '\'' +
                ", urlPrefix='" + urlPrefix + '\'' +
                ", isLegal=" + isLegal +
                ", cause=" + cause +
                ", service=" + service +
                ", version=" + version +
                ", method=" + method +
                ", apiKey=" + apiKey +
                ", timestamp=" + timestamp +
                ", secret=" + secret +
                ", secret2=" + secret2 +
                ", parameter=" + parameter +
                ", arguments=" + arguments +
                ", cookies=" + cookies +
                ", argument=" + argumentToString() +
                '}';
    }
}
