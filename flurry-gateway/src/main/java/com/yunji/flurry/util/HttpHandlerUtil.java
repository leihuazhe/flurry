package com.yunji.flurry.util;

import com.yunji.flurry.netty.http.request.RequestContext;
import com.yunji.flurry.netty.http.HttpResponseEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author maple 2018.08.28 下午4:53
 */
public class HttpHandlerUtil {
    private static Logger logger = LoggerFactory.getLogger(HttpHandlerUtil.class);

    //未完成请求计数
//    private static AtomicInteger requestCounter = new AtomicInteger(0);

    public static void sendHttpResponse(ChannelHandlerContext ctx, HttpResponseEntity entity, RequestContext context) {
        sendHttpResponse(ctx, entity.getContent(), context.request(), entity.getStatus());
    }

    /**
     * 返回信息给前端 http
     *
     * @param ctx     handler's context
     * @param content msg's info
     * @param request msg's request
     * @param status  http status
     * @link 不使用 Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
     * @link Unpooled.wrappedBuffer
     */
    public static void sendHttpResponse(ChannelHandlerContext ctx, String content, FullHttpRequest request, HttpResponseStatus status) {
        try {
            if (content == null) content = "";
            ByteBuf wrapBuf = ctx.alloc().buffer(content.length());
            wrapBuf.writeBytes(content.getBytes(CharsetUtil.UTF_8));

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, wrapBuf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            if (request == null) {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                boolean isKeepAlive = HttpUtil.isKeepAlive(request);
                if (isKeepAlive) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(response);
                } else {
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }
            }
        } finally {
            //请求返回，计数 -1
//            requestCounter.decrementAndGet();
        }
    }


    public static String wrapCode(GateWayErrorCode code) {
        return wrapCode(null, code);
    }


    public static String wrapExCodeResponse(FlurryException ex) {
        return wrapExCodeResponse(null, ex);
    }

    /**
     * wrap message response for json format.
     *
     * @param code
     * @return
     */
    public static String wrapCode(String url, GateWayErrorCode code) {
        String resp = wrapFailed(code.getCode(), code.getMsg());
        logger.info("mesh-response: url: {}, info: {}", url, resp);
        return resp;
    }

    /**
     * wrap message response for json format.
     */
    public static String wrapExCodeResponse(String url, FlurryException ex) {
        String resp = wrapFailed(ex.getCode(), ex.getMsg());
        logger.info("mesh-response: url: {}, info: {}", url, resp);
        return resp;
    }

    /**
     * wrap message response for json format.
     */
    public static String wrapSuccess(String url, Object msg) {
        String resp = "{\"data\":" + msg + ",\"code\":0}";
        logger.debug("gateway-response: url: {}, info: {}", url, resp);
        return resp;
    }

    /**
     * wrap message response for json format.
     */
    public static String logResponse(String url, Object msg) {
        logger.debug("mesh-log-response: url: {}, info: {}", url, msg);
        return msg.toString();
    }

//    public static AtomicInteger getRequestCounter() {
//        return requestCounter;
//    }


    private static String wrapFailed(int code, String errorMessage) {
        return "{\"data\":{},\"code\":" + code + ", \"errorMessage\":\"" + errorMessage + "\"}";
    }

}
