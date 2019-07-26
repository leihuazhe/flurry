package org.apache.dubbo.util;

public interface GateConstants {

    String GATEWAY_SYNC = "invoke";
    String GATEWAY_ASYNC = "invokeAsync";

    String GATEWAY_KEY = "gateway";

    String PARAMETER_TYPE = "parameterType";

    String INTERFACE = "interface";

    String PROTOCOL_CLIENT = "client";


    String PROTOCOL_CLIENT_CUSTOM = "custom";

    String PROXY = "proxy";
    /**
     * 网关自定义的字节码生成.
     */
    String PROXY_CUSTOM = "custom_javassist";
    /**
     * Gateway 自定义修改的 hessian2 协议
     */
    String SERIALIZATION_CUSTOM = "custom_hessian2";

    String METADATA_METHOD_NAME = "getServiceMetadata";

    String REGISTRY_URL = "dubbo.gateway.registry";

    String DEFAULT_REGISTRY_URL = "127.0.0.1:2181";


    String DATA_ID = "gateway_config";
    int POLLING_INTERVAL_TIME = 10;
    long TIME_OUT = 2000L;
}