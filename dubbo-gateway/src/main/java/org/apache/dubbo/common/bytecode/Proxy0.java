package org.apache.dubbo.common.bytecode;

import java.lang.reflect.InvocationHandler;

public class Proxy0 extends Proxy implements ClassGenerator.DC {

    @Override
    public Proxy1 newInstance(InvocationHandler handler) {
        return new Proxy1(handler);
    }
}
