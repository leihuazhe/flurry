package com.yunji.flurry;

import com.yunji.erlang.bean.TraceBean;
import com.yunji.flurry.netty.http.request.RequestContext;
import io.netty.channel.ChannelHandlerContext;
import org.apache.dubbo.rpc.RpcException;

/**
 * Invoker
 *
 * @author leihz
 * @since 2020-07-09 3:54 下午
 */
public interface AsyncInvoker {

    void invoke(RequestContext context, ChannelHandlerContext ctx) throws RpcException;

    void onResponse(TraceBean traceBean, String result, Throwable tx);
}
