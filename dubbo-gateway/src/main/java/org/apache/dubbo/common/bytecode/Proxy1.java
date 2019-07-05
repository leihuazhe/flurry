package org.apache.dubbo.common.bytecode;

import com.yunji.gateway.service.GateWayService;
import org.apache.dubbo.rpc.service.EchoService;
import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class Proxy1 implements ClassGenerator.DC, GateWayService, EchoService {

    private static Method[] methods;

    private InvocationHandler handler;

    Proxy1(InvocationHandler handler) {
        this.handler = handler;
        Method[] genericMethods = GenericService.class.getMethods();
        Method[] echoMethods = EchoService.class.getMethods();

        methods = new Method[genericMethods.length + echoMethods.length];

        int capacity = 0;
        for (int i = 0; i < genericMethods.length; i++) {
            methods[capacity++] = genericMethods[i];
        }

        for (int i = 0; i < genericMethods.length; i++) {
            methods[capacity++] = echoMethods[i];
        }
    }

    public CompletableFuture invokeAsync(String method, String[] parameterTypes, Object[] args) throws GenericException {
        Object[] invokerArgs = new Object[3];
        invokerArgs[0] = method;
        invokerArgs[1] = parameterTypes;
        invokerArgs[2] = args;
        Object ret = null;
        try {
            ret = handler.invoke(this, methods[0], invokerArgs);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return (java.util.concurrent.CompletableFuture) ret;
    }

    public Object invoke(String method, String[] parameterTypes, Object[] args) throws GenericException {
        Object[] invokerArgs = new Object[3];
        invokerArgs[0] = method;
        invokerArgs[1] = parameterTypes;
        invokerArgs[2] = args;
        Object ret = null;
        try {
            ret = handler.invoke(this, methods[1], invokerArgs);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return ret;
    }

    public java.lang.Object $echo(java.lang.Object arg0) {
        Object[] args = new Object[1];
        args[0] = arg0;
        Object ret = null;
        try {
            ret = handler.invoke(this, methods[2], args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return ret;
    }
}
