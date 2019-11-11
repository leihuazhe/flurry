package com.yunji.flurry.netty.http.router;

import io.netty.handler.codec.http.HttpMethod;

/**
 * @author Denim.leihz 2019-11-04 10:14 AM
 */
public class HttpLabel {

    private String uri;
    private HttpMethod method;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }
}
