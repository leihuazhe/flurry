package com.yunji.flurry.netty.http;

import com.yunji.flurry.handler.GatewayAsyncSender;
import com.yunji.flurry.netty.http.request.RequestContext;
import com.yunji.flurry.util.GateWayErrorCode;
import com.yunji.flurry.util.FlurryException;
import com.yunji.flurry.util.HttpHandlerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * @author maple 2018.08.28 下午3:21
 */
public class HttpPostInvoker {
    private static final Logger logger = LoggerFactory.getLogger(HttpPostInvoker.class);

    private final GatewayAsyncSender asyncSender;

    public HttpPostInvoker(String registryUrl, String diamondId) {
        this.asyncSender = new GatewayAsyncSender(registryUrl, diamondId);
    }

    /**
     * Async HttpPostInvoker.
     */
    public void asyncInvoke(RequestContext context, ChannelHandlerContext ctx) throws RpcException {
        if (context.isLegal()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Http:{}, 请求参数: {} ", context.requestUrl(), context.argumentToString());
            }
            try {
                long st = System.currentTimeMillis();
                CompletableFuture<String> jsonResponse = asyncSender.sendAsync(context, ctx);

                //todo How to show  concrete and detail exception message.
                jsonResponse.whenComplete((result, t) -> {
                    long et = System.currentTimeMillis();
                    if (t != null) {
                        logger.error("GatewayAsyncSender handlerPostAsync result got error: {}, cost: {} ms", t.getMessage(), (et - st));

                        String errorMessage;
                        if (t instanceof RemotingException) {
                            errorMessage = HttpHandlerUtil.wrapCode(GateWayErrorCode.RemotingError);
                        } else if (t instanceof TimeoutException) {
                            errorMessage = HttpHandlerUtil.wrapCode(GateWayErrorCode.TimeOutError);
                        } else {
                            errorMessage = t.getMessage();
                        }
                        doResponse(ctx, errorMessage, context.request());
                    } else {
                        doResponse(ctx, HttpHandlerUtil.wrapSuccess(context.requestUrl(), result), context.request());
                        if (logger.isDebugEnabled()) {
                            logger.debug("handlerPostAsync result successful : {}, cost: {} ms", result, (et - st));
                        }
                    }
                });

            } catch (FlurryException e) {
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

    private void doResponse(ChannelHandlerContext ctx, String info, FullHttpRequest request) {
        HttpHandlerUtil.sendHttpResponse(ctx, info, request, HttpResponseStatus.OK);
    }
}
