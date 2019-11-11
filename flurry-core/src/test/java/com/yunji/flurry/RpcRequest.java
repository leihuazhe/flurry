package com.yunji.flurry;

import java.util.Arrays;

/**
 * @author Denim.leihz 2019-07-30 8:38 PM
 */
public class RpcRequest {
    private String serviceName;
    private String method;
    private String address;
    private String[] paramsType;
    private Object[] paramsValue;
    private String rpcContent;

    RpcRequest(String serviceName, String method, String address, String[] paramsType, Object[] paramsValue, String rpcContent) {
        this.serviceName = serviceName;
        this.method = method;
        this.address = address;
        this.paramsType = paramsType;
        this.paramsValue = paramsValue;
        this.rpcContent = rpcContent;
    }

    public static RpcRequestBuilder builder() {
        return new RpcRequestBuilder();
    }

    public RpcRequestBuilder toBuilder() {
        return new RpcRequestBuilder().serviceName(this.serviceName).method(this.method).address(this.address).paramsType(this.paramsType).paramsValue(this.paramsValue).rpcContent(this.rpcContent);
    }

    private RpcRequest() {
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getMethod() {
        return this.method;
    }

    public String getAddress() {
        return this.address;
    }

    public String[] getParamsType() {
        return this.paramsType;
    }

    public Object[] getParamsValue() {
        return this.paramsValue;
    }

    public String getRpcContent() {
        return this.rpcContent;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setParamsType(String[] paramsType) {
        this.paramsType = paramsType;
    }

    public void setParamsValue(Object[] paramsValue) {
        this.paramsValue = paramsValue;
    }

    public void setRpcContent(String rpcContent) {
        this.rpcContent = rpcContent;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RpcRequest)) {
            return false;
        }
        RpcRequest other = (RpcRequest) o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$serviceName = this.getServiceName();
        String other$serviceName = other.getServiceName();
        if (this$serviceName == null ? other$serviceName != null : !this$serviceName.equals(other$serviceName)) {
            return false;
        }
        String this$method = this.getMethod();
        String other$method = other.getMethod();
        if (this$method == null ? other$method != null : !this$method.equals(other$method)) {
            return false;
        }
        String this$address = this.getAddress();
        String other$address = other.getAddress();
        if (this$address == null ? other$address != null : !this$address.equals(other$address)) {
            return false;
        }
        if (!Arrays.deepEquals(this.getParamsType(), other.getParamsType())) {
            return false;
        }
        if (!Arrays.deepEquals(this.getParamsValue(), other.getParamsValue())) {
            return false;
        }
        String this$rpcContent = this.getRpcContent();
        String other$rpcContent = other.getRpcContent();
        if (this$rpcContent == null ? other$rpcContent != null : !this$rpcContent.equals(other$rpcContent)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(Object other) {
        return other instanceof RpcRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $serviceName = this.getServiceName();
        result = result * 59 + ($serviceName == null ? 43 : $serviceName.hashCode());
        String $method = this.getMethod();
        result = result * 59 + ($method == null ? 43 : $method.hashCode());
        String $address = this.getAddress();
        result = result * 59 + ($address == null ? 43 : $address.hashCode());
        result = result * 59 + Arrays.deepHashCode(this.getParamsType());
        result = result * 59 + Arrays.deepHashCode(this.getParamsValue());
        String $rpcContent = this.getRpcContent();
        result = result * 59 + ($rpcContent == null ? 43 : $rpcContent.hashCode());
        return result;
    }

    public String toString() {
        return "RpcRequest(serviceName=" + this.getServiceName() + ", method=" + this.getMethod() + ", address=" + this.getAddress() + ", paramsType=" + Arrays.deepToString(this.getParamsType()) + ", paramsValue=" + Arrays.deepToString(this.getParamsValue()) + ", rpcContent=" + this.getRpcContent() + ")";
    }

    public static class RpcRequestBuilder {
        private String serviceName;
        private String method;
        private String address;
        private String[] paramsType;
        private Object[] paramsValue;
        private String rpcContent;

        RpcRequestBuilder() {
        }

        public RpcRequestBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public RpcRequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RpcRequestBuilder address(String address) {
            this.address = address;
            return this;
        }

        public RpcRequestBuilder paramsType(String[] paramsType) {
            this.paramsType = paramsType;
            return this;
        }

        public RpcRequestBuilder paramsValue(Object[] paramsValue) {
            this.paramsValue = paramsValue;
            return this;
        }

        public RpcRequestBuilder rpcContent(String rpcContent) {
            this.rpcContent = rpcContent;
            return this;
        }

        public RpcRequest build() {
            return new RpcRequest(this.serviceName, this.method, this.address, this.paramsType, this.paramsValue, this.rpcContent);
        }

        public String toString() {
            return "RpcRequest.RpcRequestBuilder(serviceName=" + this.serviceName + ", method=" + this.method + ", address=" + this.address + ", paramsType=" + Arrays.deepToString(this.paramsType) + ", paramsValue=" + Arrays.deepToString(this.paramsValue) + ", rpcContent=" + this.rpcContent + ")";
        }
    }

}

/*
@Builder(toBuilder = true)
@Data
public class RpcRequest {

    private String serviceName;

    private String method;

    private String address;

    private String[] paramsType;

    private Object[] paramsValue;

    private String rpcContent;

}
 */