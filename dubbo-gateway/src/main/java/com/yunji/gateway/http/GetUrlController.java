package com.yunji.gateway.http;

import com.yunji.gateway.util.GateWayErrorCode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get 请求的 url 映射 <-> 处理器
 *
 * @author maple 2018.09.05 下午4:05
 */
public class GetUrlController {
    private Logger logger = LoggerFactory.getLogger(GetUrlController.class);
    /***
     * netty mesh 容器状态,即将关闭时显示 GREEN
     */
    public static GatewayHealthStatus status = GatewayHealthStatus.GREEN;

    /**
     * @return resp
     */
    public HttpResponseEntity handlerHealth(String url) {
        logger.debug("handlerHealth check,container status: " + status);
        if (status == GatewayHealthStatus.YELLOW) {
            logger.info("handlerHealth check,container status: " + status);
            return new HttpResponseEntity(
                    HttpProcessorUtils.wrapErrorResponse(GateWayErrorCode.MeshShutdownSoon),
                    HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url,
                "health check container is running"),
                HttpResponseStatus.OK);
    }

    /**
     * 将康检查
     *
     * @return resp
     */
    public HttpResponseEntity getCheck(String url) {
        logger.debug("check support url request, uri: {}", url);
        return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, "dapeng-mesh is running"), HttpResponseStatus.OK);
    }

    /**
     * 客户端同步时间接口
     *
     * @return 服务端系统当前时间
     */
    public HttpResponseEntity syncSysTime(String url) {
        return new HttpResponseEntity(HttpProcessorUtils.logResponse(url, System.currentTimeMillis()), HttpResponseStatus.OK);
    }

}
