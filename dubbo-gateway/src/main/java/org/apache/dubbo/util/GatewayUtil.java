package org.apache.dubbo.util;


import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;

import java.lang.reflect.Field;

/**
 * @author Denim.leihz 2019-07-09 11:53 AM
 */
public class GatewayUtil {

    public static boolean isGateway(String gateway) {
        return Boolean.valueOf(gateway);
    }

    public static byte[] getHessian2Byte(ObjectOutput output) {
        try {
            Hessian2ObjectOutput out = (Hessian2ObjectOutput) output;
            Field field = out.getClass().getDeclaredField("mH2o");
            field.setAccessible(true);
            Hessian2Output hessian2Output = (Hessian2Output) field.get(out);
            Field buffer = hessian2Output.getClass().getDeclaredField("_buffer");
            buffer.setAccessible(true);
            return (byte[]) buffer.get(hessian2Output);
        } catch (Exception ignored) {
        }
        return null;
    }
}
