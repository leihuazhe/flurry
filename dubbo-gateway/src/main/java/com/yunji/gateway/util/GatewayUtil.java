package com.yunji.gateway.util;

import org.apache.dubbo.common.io.UnsafeStringWriter;
import org.apache.dubbo.common.utils.StringUtils;

import java.io.PrintWriter;

import static org.apache.dubbo.rpc.Constants.*;

public class GatewayUtil {

    public static boolean isGateWayInvoke(String generic) {
        return generic != null
                && !"".equals(generic)
                && (GENERIC_SERIALIZATION_DEFAULT.equalsIgnoreCase(generic)  /* Normal generalization cal */
                || GENERIC_SERIALIZATION_NATIVE_JAVA.equalsIgnoreCase(generic) /* Streaming generalization call supporting jdk serialization */
                || GENERIC_SERIALIZATION_BEAN.equalsIgnoreCase(generic)
                || GENERIC_SERIALIZATION_PROTOBUF.equalsIgnoreCase(generic));
    }


    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }
}


