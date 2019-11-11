package com.yunji.flurry.netty.http;

import com.yunji.flurry.netty.http.request.RequestContext;
import com.yunji.flurry.util.GateWayErrorCode;
import com.yunji.flurry.util.HttpHandlerUtil;
import com.yunji.flurry.netty.http.util.Constants;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author maple 2018.08.28 下午3:21
 */
public class HttpGetHeadProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HttpGetHeadProcessor.class);

    private GetUrlController controller = new GetUrlController();

    public HttpResponseEntity handlerRequest(RequestContext context) {
        String url = processUrl(context.requestUrl());

        switch (url) {
            case Constants.GET_HEALTH_CHECK_URL:

                return controller.handlerHealth(url);
            case Constants.GET_CHECK:

                return controller.getCheck(url);
            case Constants.SYS_TIME_SYNC:

                return controller.syncSysTime(url);
            default:
                break;
        }
        logger.info("not support url request, uri: {}", url);
        return new HttpResponseEntity(
                HttpHandlerUtil.wrapCode(url, GateWayErrorCode.RequestUrlNotSupport),
                HttpResponseStatus.OK);
    }

    /**
     * 如果 Get Url 带有 "?" , 去问号之前的内容
     *
     * @param url input url
     * @return
     */
    private String processUrl(String url) {
        int i = url.lastIndexOf("?");
        if (i > 0) {
            return url.substring(0, i);
        }
        return url;
    }
}
