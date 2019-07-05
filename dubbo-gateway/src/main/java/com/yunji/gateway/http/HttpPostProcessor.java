package com.yunji.gateway.http;

import com.yunji.gateway.http.request.RequestContext;
import com.yunji.gateway.process.PostUtil;
import com.yunji.gateway.util.GateWayErrorCode;
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
                String jsonResponse = PostUtil.post(context);
                HttpProcessorUtils.sendHttpResponse(ctx, jsonResponse, context.request(), HttpResponseStatus.OK);
            } catch (Exception e) {
                HttpProcessorUtils.sendHttpResponse(ctx, e.getMessage(), context.request(), HttpResponseStatus.OK);
            }
        } else {
            HttpProcessorUtils.sendHttpResponse(ctx,
                    HttpProcessorUtils.wrapErrorResponse(GateWayErrorCode.IllegalRequest),
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

            CompletableFuture<String> jsonResponse = PostUtil.postAsync(context);

            jsonResponse.whenComplete((result, ex) -> {
                if (ex != null) {
                    HttpProcessorUtils.sendHttpResponse(ctx, ex.getMessage(), context.request(), HttpResponseStatus.OK);
                } else {
                    HttpProcessorUtils.sendHttpResponse(ctx, result, context.request(), HttpResponseStatus.OK);
                }
            });

        } else {
            HttpProcessorUtils.sendHttpResponse(ctx,
                    HttpProcessorUtils.wrapErrorResponse(GateWayErrorCode.IllegalRequest),
                    context.request(),
                    HttpResponseStatus.OK);
        }
    }

}
