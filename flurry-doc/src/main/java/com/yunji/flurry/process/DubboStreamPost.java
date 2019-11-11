package com.yunji.flurry.process;

import com.yunji.flurry.core.DubboExecutedFacade;
import com.yunji.flurry.metadata.OptimizedService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * @author Denim.leihz 2019-08-01 3:03 PM
 */
public class DubboStreamPost extends AbstractPost implements Post {

    private DubboExecutedFacade executedFacade;

    public DubboStreamPost(String registryUrl, String diamondId) {
        super(registryUrl, diamondId);
        executedFacade = new DubboExecutedFacade(registryUrl, diamondId);
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

            LOGGER.info("service: {}, method: {}, result:{}", service, method, result);

            return result;
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", 500, e.getMessage(), "{}");
        }
    }


}
