package org.apache.dubbo.rpc.filter;

import java.lang.reflect.Method;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.rpc.*;
import com.yunji.gateway.util.GatewayUtils;

import static com.yunji.gateway.util.Constants.*;

/**
 * GateWayFilter
 */
@Activate(group = CommonConstants.CONSUMER, value = GATEWAY_KEY, order = 20001)
public class GateWayFilter extends ListenableFilter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String gateway = invoker.getUrl().getParameter(GATEWAY_KEY);
        if ((invocation.getMethodName().equals(GATEWAY_SYNC) || invocation.getMethodName().equals(GATEWAY_ASYNC))
                && invocation.getArguments() != null
                && invocation.getArguments().length == 3
                && GatewayUtils.isGateWayInvoke(gateway)) {

            String name = ((String) invocation.getArguments()[0]).trim();
            String[] types = (String[]) invocation.getArguments()[1];
            Object[] args = (Object[]) invocation.getArguments()[2];
            try {
                Method method = ReflectUtils.findMethodByMethodSignature(invoker.getInterface(), name, types);
                invocation.setAttachment(GATEWAY_KEY, invoker.getUrl().getParameter(GATEWAY_KEY));

                return invoker.invoke(new RpcInvocation(method, args, invocation.getAttachments()));
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RpcException(e.getMessage(), e);
            }
        }
        return invoker.invoke(invocation);
    }

}
