package com.yunji.gateway.doc.repository;

import com.yunji.gateway.GateWayService;
import com.yunji.gateway.GatewayServiceFactory;
import com.yunji.gateway.core.DubboExecutedFacade;
import com.yunji.gateway.doc.util.MixUtils;
import com.yunji.gateway.metadata.OptimizedService;
import com.yunji.gateway.metadata.common.MetadataUtil;
import com.yunji.gateway.metadata.tag.Field;
import org.apache.dubbo.config.RpcRequest;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.EchoService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * <pre>
 * {
 *   "data": null,
 *   "errorCode": 1002,
 *   "errorMessage": "ErrorCode:-1;ErrorMsg:请传入有效的优惠券ID",
 *   "logisticsRemindContent": "",
 *   "orderCancelTime": 0,
 *   "orderId": "YJ150492010063638528",
 *   "payMode": 1
 * }
 * </pre>
 *
 * @author Denim.leihz 2019-08-07 11:49 PM
 */
public class GatewayDocStreamPoster extends GatewayDocPoster {

    private DubboExecutedFacade executedFacade;

    public GatewayDocStreamPoster(String registryUrl, String diamondId) {
        super(registryUrl, diamondId);
        executedFacade = new DubboExecutedFacade(registryUrl, diamondId);
    }

    @Override
    protected String executeEcho(String service, String version) throws Exception {
        String[] parameterTypes = new String[]{};
        RpcRequest request = RpcRequest.builder()
                .serviceName(service)
                .method(MixUtils.ECHO_NAME)
                .version(version)
                .paramsType(parameterTypes)
                .paramsValue(new Object[]{})
                .build();
        //new String[]{}, new Object[]{}
        GateWayService gateWayService = GatewayServiceFactory.create(request);

        EchoService echoService = (EchoService) gateWayService;
        echoService.$echo("Health check.");

        CompletableFuture<String> completableFuture = RpcContext.getContext().getCompletableFuture();
        return completableFuture.get();
    }

    @Override
    protected String doPost(String service,
                            OptimizedService bizService,
                            String method,
                            String finalVersion,
                            String parameter) {
        CompletableFuture<String> resultFuture = executedFacade.execute(service, method, finalVersion, parameter, bizService);
        try {
            String result = resultFuture.get();

            logger.info("service: {}, method: {}, result:{}", service, method, result);

            return result;
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", "ClientUnKnown", e.getMessage(), "{}");
        }
    }
}
