package com.yunji.gateway.util;

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
    /**
     * 默认获取元数据信息的方法
     */
    String METADATA_METHOD_NAME = "getServiceMetadata";
    /**
     * 注册中心环境变量
     */
    String REGISTRY_URL_KEY = "dubbo.registry.address";
    /**
     * Diamond Id
     */
    String DATA_ID_KEY = "gateway.diamond.dataId";

    String APPLICATION_NAME_KEY = "gateway.application.name";
    /**
     * 网关白名单配置
     */
    String WHITE_SERVICES_KEY = "gateway.white.service.list";


    //默认值
    String DEFAULT_REGISTRY_URL = "127.0.0.1:2181";

    String DEFAULT_DATA_ID = "dubbo_gateway_config";

    String DEFAULT_APPLICATION_NAME = "gateway-application";

    String REGISTEY_PROTOCOL = "zookeeper";

    int POLLING_INTERVAL_TIME = 10;

    long DIAMOND_TIME_OUT = 2000L;

    long TIME_OUT = 2000L;

    String REFERENCE_TIME_OUT = "reference.service.time.out";


    int DEFAULT_RETRY = 3;
}

