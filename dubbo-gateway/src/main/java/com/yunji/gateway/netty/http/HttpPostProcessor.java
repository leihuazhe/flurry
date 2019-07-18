package com.yunji.gateway.netty.http;

import com.yunji.gateway.netty.http.request.RequestContext;
import com.yunji.gateway.handler.JsonSender;
import com.yunji.gateway.util.GateWayErrorCode;
import com.yunji.gateway.util.GatewayException;
import com.yunji.gateway.util.HttpHandlerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static com.yunji.gateway.util.GateWayErrorCode.*;
import static com.yunji.gateway.util.HttpHandlerUtil.*;

/**
 * @author maple 2018.08.28 下午3:21
 */
public class HttpPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HttpPostProcessor.class);

    /**
     * Async
     */
    public void handlerPostAsync(RequestContext context, ChannelHandlerContext ctx) throws RpcException {
        if (context.isLegal()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Http:{}, 请求参数: {} ", context.requestUrl(), context.argumentToString());
            }
            try {
                String url = context.requestUrl();

                CompletableFuture<String> jsonResponse;

                if (url.endsWith("getServiceMetadata")) {
                    jsonResponse = JsonSender.getServiceMetadata(context, ctx);
                } else {
                    jsonResponse = JsonSender.sendAsync(context, ctx);
                }

                //todo How to show  concrete and detail exception message.
                jsonResponse.whenComplete((result, t) -> {
                    if (t != null) {
                        logger.error("JsonSender handlerPostAsync result got error: {}", t.getMessage());

                        String errorMessage;
                        if (t instanceof RemotingException) {
                            errorMessage = wrapCode(RemotingError);
                        } else if (t instanceof TimeoutException) {
                            errorMessage = wrapCode(TimeOutError);
                        } else {
                            errorMessage = t.getMessage();
                        }
                        doResponse(ctx, errorMessage, context.request(), HttpResponseStatus.OK);
                    } else {
                        doResponse(ctx, wrapSuccess(context.requestUrl(), result), context.request(), HttpResponseStatus.OK);
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
                    HttpHandlerUtil.wrapCode(GateWayErrorCode.IllegalRequest),
                    context.request(),
                    HttpResponseStatus.OK);
        }
    }

    private void doResponse(ChannelHandlerContext ctx, String info, FullHttpRequest request, HttpResponseStatus status) {
        HttpHandlerUtil.sendHttpResponse(ctx, info, request, status);
    }

}
