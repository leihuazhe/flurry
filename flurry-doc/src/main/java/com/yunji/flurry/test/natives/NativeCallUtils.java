package com.yunji.flurry.test.natives;

import com.google.gson.Gson;
import com.yunji.flurry.util.MixUtils;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;

import java.lang.reflect.Method;

/**
 * @author Denim.leihz 2019-10-29 2:44 PM
 */
public class NativeCallUtils {
    private static Gson gson = new Gson();

    public static <T> ReferenceConfig<T> createReference(Class<T> clazz) {
        RegistryConfig registry = new RegistryConfig();
        registry.setProtocol("zookeeper");
        registry.setAddress("127.0.0.1:2181");
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setRegistry(registry);
        reference.setApplication(MixUtils.getApplication());
        reference.setInterface(clazz);
        reference.setVersion("1.0.0");
        reference.setConnections(2);
//        reference.set
        return reference;
    }

    public static Object invokeMethod(Object service, String method, String... json) throws Exception {
        Method[] methods = service.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(method)) {
                if (json != null) {
                    Class<?>[] parameterTypes = m.getParameterTypes();
                    Object[] args = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        args[i] = gson.fromJson(json[i], parameterTypes[i]);
                    }
                    return m.invoke(service, args);
                }
                return m.invoke(service, (Object) null);
            }
        }
        throw new RuntimeException("No service or method found. " + service + " -> " + method);
    }
}
