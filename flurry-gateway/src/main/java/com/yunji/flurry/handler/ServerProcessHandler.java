package com.yunji.flurry.handler;

import com.yunji.flurry.netty.http.HttpGetHeadProcessor;
import com.yunji.flurry.netty.http.HttpPostProcessor;
import com.yunji.flurry.util.HttpHandlerUtil;
import com.yunji.flurry.netty.http.HttpResponseEntity;
import com.yunji.flurry.netty.http.request.RequestContext;
import com.yunji.flurry.util.GateWayErrorCode;
import com.yunji.flurry.util.FlurryException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author maple 2018.08.23 上午10:01
 */
@ChannelHandler.Sharable
public class ServerProcessHandler extends SimpleChannelInboundHandler<RequestContext> {
    private static Logger logger = LoggerFactory.getLogger(ServerProcessHandler.class);

    private final HttpPostProcessor postHandler;
    private final HttpGetHeadProcessor getHandler = new HttpGetHeadProcessor();

    public ServerProcessHandler(String registryUrl, String diamondId) {
        postHandler = new HttpPostProcessor(registryUrl, diamondId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestContext context) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("current request context: {}", context.toString());
        }

        try {
            dispatchRequest(context, ctx);
        } catch (FlurryException e) {
            logger.error("网关请求SoaException：" + e.getMessage());
            HttpHandlerUtil.sendHttpResponse(ctx, HttpHandlerUtil.wrapExCodeResponse(e), null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("网关处理请求失败: " + e.getMessage(), e);
            HttpHandlerUtil.sendHttpResponse(ctx,
                    HttpHandlerUtil.wrapCode(GateWayErrorCode.ProcessReqFailed),
                    null,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void dispatchRequest(RequestContext context, ChannelHandlerContext ctx) throws FlurryException {
        HttpMethod httpMethod = context.httpMethod();
        if (HttpMethod.POST.equals(httpMethod)) {
            postHandler.handlerPostAsync(context, ctx);
            return;
        }
        boolean isGet = HttpMethod.GET.equals(httpMethod);
        if (isGet || HttpMethod.HEAD.equals(httpMethod)) {
            handlerGetAndHead(context, ctx);
            return;
        }
        HttpHandlerUtil.sendHttpResponse(ctx,
                HttpHandlerUtil.wrapCode(GateWayErrorCode.RequestTypeNotSupport),
                context.request(),
                HttpResponseStatus.OK);
    }


    /**
     * handler get 和 head 请求
     */
    private void handlerGetAndHead(RequestContext context, ChannelHandlerContext ctx) {
        HttpResponseEntity entity = getHandler.handlerRequest(context);
        HttpHandlerUtil.sendHttpResponse(ctx, entity, context);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("连接的客户端地址:{}", ctx.channel().remoteAddress());
        }
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("网关handler exceptionCaught未知异常: " + cause.getMessage(), cause);
        HttpHandlerUtil.sendHttpResponse(ctx,
                HttpHandlerUtil.wrapCode(GateWayErrorCode.MeshUnknownError),
                null,
                HttpResponseStatus.INTERNAL_SERVER_ERROR);

        ctx.close();
    }
}
