package com.yunji.flurry.util;

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
     * Gateway 自定义修改的 hessian2 协议,排序超过 hessian2 本身,这样才会被 ignore.
     */
//    String SERIALIZATION_CUSTOM = "a_highly_hessian2";
    String SERIALIZATION_CUSTOM = "hessian3";
    /**
     * 默认获取元数据信息的方法
     */
    String METADATA_METHOD_NAME = "_getServiceMetadata";
    /**
     * ECHO method
     */
    String ECHO_METHOD = "$echo";
    /**
     * 注册中心环境变量
     */
    String REGISTRY_URL_KEY = "dubbo.registry.address";
    /**
     * Diamond Id
     */
    String DATA_ID_KEY = "flurry.diamond.dataId";

    String APPLICATION_NAME_KEY = "flurry.application";
    /**
     * 网关白名单配置
     */
    String WHITE_SERVICES_KEY = "gateway.white.service.list";


    //默认值
    String DEFAULT_REGISTRY_URL = "127.0.0.1:2181";

    String DEFAULT_DATA_ID = "dubbo_gateway_config";

    String DEFAULT_APPLICATION_NAME = "flurry-application";

    String REGISTER_PROTOCOL = "zookeeper";

    String GATEWAY_REFERENCE_TIME_OUT = "gateway.reference.timeout";


    int POLLING_INTERVAL_TIME = 10;

    long DIAMOND_TIME_OUT = 2000L;

    long METADATA_CALL_TIME_OUT = 2000L;


    int DEFAULT_RETRY = 3;

    /**
     * Nacos 相关
     */



    String NACOS_SEPARATOR = "|";
}

