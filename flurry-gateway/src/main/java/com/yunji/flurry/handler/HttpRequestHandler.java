package com.yunji.flurry.handler;

import com.yunji.flurry.util.HttpHandlerUtil;
import com.yunji.flurry.netty.http.match.UrlMappingResolver;
import com.yunji.flurry.netty.http.request.RequestContext;
import com.yunji.flurry.util.GateWayErrorCode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author maple 2018.08.23 上午10:01
 */
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        RequestContext context = new RequestContext();
        context.request(request);
        try {
            HttpMethod httpMethod = request.method();
            String url = request.uri();

            context.httpMethod(httpMethod);
            context.requestUrl(url);
            // POST FIRST
            if (HttpMethod.POST.equals(httpMethod)) {
                UrlMappingResolver.handlerPostUrl(request, context);
            } else {
                boolean isGet = HttpMethod.GET.equals(httpMethod);
                if (isGet || HttpMethod.HEAD.equals(httpMethod)) {
                    logger.debug("For the time being, no message to log for Get-method");
                }
            }
            super.channelRead(ctx, context);
        } catch (Exception e) {
            logger.error("网关处理请求失败: " + e.getMessage(), e);
            HttpHandlerUtil.sendHttpResponse(ctx,
                    HttpHandlerUtil.wrapCode(GateWayErrorCode.ProcessReqFailed),
                    null,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
