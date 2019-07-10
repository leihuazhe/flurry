package com.yunji.gateway.service;

import org.apache.dubbo.rpc.service.GenericException;

import java.util.concurrent.CompletableFuture;

public interface GateWayService {

    /**
     * Generic invocation
     *
     * @param method         Method name, e.g. findPerson. If there are overridden methods, parameter info is
     *                       required, e.g. findPerson(java.lang.String)
     * @param parameterTypes Parameter types
     * @param args           Arguments
     * @return invocation return value
     * @throws GenericException potential exception thrown from the invocation
     */
    Object invoke(String method, String[] parameterTypes, Object[] args) throws GenericException;

    default CompletableFuture<Object> invokeAsync(String method, String[] parameterTypes, Object[] args) throws GenericException {
        Object object = invoke(method, parameterTypes, args);
        if (object instanceof CompletableFuture) {
            return (CompletableFuture<Object>) object;
        }
        return CompletableFuture.completedFuture(object);
    }

}