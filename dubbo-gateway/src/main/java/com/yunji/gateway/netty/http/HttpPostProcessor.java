package com.yunji.gateway.netty.http;

import com.yunji.gateway.netty.http.request.RequestContext;
import com.yunji.gateway.handler.PostUtil;
import com.yunji.gateway.util.GateWayErrorCode;
import com.yunji.gateway.util.GatewayException;
import com.yunji.gateway.util.HttpHandlerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author maple 2018.08.28 下午3:21
 */
public class HttpPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HttpPostProcessor.class);

    /**
     * Sync
     */
    public void handlerPost(RequestContext context, ChannelHandlerContext ctx) throws RpcException {
        if (context.isLegal()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Http:{}, 请求参数: {} ", context.requestUrl(), context.argumentToString());
            }

            try {
                String jsonResponse = PostUtil.post(context, ctx);
                if (jsonResponse != null) {
                    HttpHandlerUtil.sendHttpResponse(ctx, jsonResponse, context.request(), HttpResponseStatus.OK);
                }
            } catch (Exception e) {
                HttpHandlerUtil.sendHttpResponse(ctx, e.getMessage(), context.request(), HttpResponseStatus.OK);
            }
        } else {
            HttpHandlerUtil.sendHttpResponse(ctx,
                    HttpHandlerUtil.wrapErrorResponse(GateWayErrorCode.IllegalRequest),
                    context.request(),
                    HttpResponseStatus.OK);
        }
    }


    /**
     * Async
     */
    public void handlerPostAsync(RequestContext context, ChannelHandlerContext ctx) throws RpcException {
        if (context.isLegal()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Http:{}, 请求参数: {} ", context.requestUrl(), context.argumentToString());
            }

            try {
                CompletableFuture<String> jsonResponse = PostUtil.postAsync(context, ctx);

                jsonResponse.whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.error("Post async result error: {}", ex.getMessage());
                        HttpHandlerUtil.sendHttpResponse(ctx, ex.getMessage(), context.request(), HttpResponseStatus.OK);
                    } else {
                        HttpHandlerUtil.sendHttpResponse(ctx, result, context.request(), HttpResponseStatus.OK);
                    }
                });

            } catch (GatewayException e) {
                HttpHandlerUtil.sendHttpResponse(ctx,
                        HttpHandlerUtil.wrapExCodeResponse(e),
                        context.request(),
                        HttpResponseStatus.OK);
            }

        } else {
            HttpHandlerUtil.sendHttpResponse(ctx,
                    HttpHandlerUtil.wrapErrorResponse(GateWayErrorCode.IllegalRequest),
                    context.request(),
                    HttpResponseStatus.OK);
        }
    }

}
