package com.yunji.gateway.doc.repository;

import com.yunji.api.doc.compatible.SoaCode;
import com.yunji.gateway.core.DubboExecutedFacade;
import com.yunji.gateway.metadata.OptimizedMetadata;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Denim.leihz 2019-08-07 11:49 PM
 */
public class GatewayDocStreamPoster extends GatewayDocPoster {

    private DubboExecutedFacade executedFacade;

    public GatewayDocStreamPoster(String registryUrl, String diamondId) {
        super(registryUrl, diamondId);
        executedFacade = new DubboExecutedFacade(registryUrl, diamondId, false);
    }

    @Override
    protected String doPost(String service,
                            OptimizedMetadata.OptimizedService bizService,
                            String method,
                            String finalVersion,
                            String parameter) {

        CompletableFuture<String> resultFuture = executedFacade.execute(service, method, finalVersion, parameter, bizService);


        try {
            String result = resultFuture.get();

            LOGGER.info("service: {}, method: {}, result:{}", service, method, result);

            return result;
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", SoaCode.ClientUnKnown.getCode(), e.getMessage(), "{}");
        }
    }
}
