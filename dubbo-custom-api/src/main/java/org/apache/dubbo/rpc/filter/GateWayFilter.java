package org.apache.dubbo.rpc.filter;


import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.util.GatewayUtil;

import static org.apache.dubbo.jsonserializer.util.GateConstants.*;
import static org.apache.dubbo.rpc.Constants.GENERIC_KEY;

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
                && GatewayUtil.isGateWayInvoke(gateway)) {

            String name = ((String) invocation.getArguments()[0]).trim();
            String[] types = (String[]) invocation.getArguments()[1];
            Object[] args = (Object[]) invocation.getArguments()[2];

            invocation.setAttachment(GATEWAY_KEY, invoker.getUrl().getParameter(GATEWAY_KEY));
            invocation.setAttachment(GENERIC_KEY, invoker.getUrl().getParameter(GENERIC_KEY));
            invocation.setAttachment(PARAMETER_TYPE, StringUtils.join(types, ","));

            RpcInvocation newInv = new RpcInvocation(name, null, args, invocation.getAttachments());

            return invoker.invoke(newInv);
        }
        return invoker.invoke(invocation);
    }

}
