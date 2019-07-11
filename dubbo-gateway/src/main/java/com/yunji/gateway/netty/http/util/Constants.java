package com.yunji.gateway.netty.http.util;

/**
 * desc: GateConstants
 *
 * @author maple 2018.08.27 22:37
 */
public class Constants {

    public static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() * 2, 32);


    public static final String GET_HEALTH_CHECK_URL = "/health/check";


    public static final String GET_CHECK = "/";

    public static final String RESP_STATUS = "status";

    public static final String SERVICE_LIST = "/api/list";
    public static final String SYS_TIME_SYNC = "/api/sysTime";
    public static final String ECHO_PREFIX = "/api/echo";


    public static final String ADMIN_SERVICE_NAME = "com.today.api.admin.service.OpenAdminService";
    public static final String ADMIN_VERSION_NAME = "1.0.0";
    public static final String ADMIN_METHOD_NAME = "checkGateWayAuth";


    public static final String COOKIES_PREFIX = "cookie_";


}
