package com.yunji.gateway.util;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Denim.leihz 2019-07-22 4:47 PM
 */
public class PropertyUtils {

    public static String getProperty(String key,String defaultValue) throws IOException {
        Properties prop = new Properties();
        prop.load(getClassLoader(PropertyUtils.class).getResourceAsStream("gateway.properties"));

        return prop.getProperty(key,defaultValue);
    }

    private static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = clazz.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }

        return cl;
    }
}
