package com.yunji.flurry;

import org.apache.dubbo.rpc.service.GenericException;

public interface GateWayService {

    String invoke(String method, String[] parameterTypes, Object[] args) throws GenericException;

    /*default CompletableFuture<Object> invokeAsync(String method, String[] parameterTypes, Object[] args) throws GenericException {
        Object object = invoke(method, parameterTypes, args);
        if (object instanceof CompletableFuture) {
            return (CompletableFuture<Object>) object;
        }
        return CompletableFuture.completedFuture(object);
    }*/

}