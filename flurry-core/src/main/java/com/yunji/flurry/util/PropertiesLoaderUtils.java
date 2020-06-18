package com.yunji.flurry.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

/**
 * PropertiesLoaderUtils
 *
 * @author leihz
 * @since 2020-06-17 10:32 上午
 */
public class PropertiesLoaderUtils {
    private static final Logger log = LoggerFactory.getLogger(PropertiesLoaderUtils.class);

    private static final String XML_FILE_EXTENSION = ".xml";

    public static Properties loadProperties(Resource resource) throws IOException {
        Properties props = new Properties();
        fillProperties(props, resource);
        return props;
    }

    public static void fillProperties(Properties props, Resource resource) throws IOException {
        InputStream is = resource.getInputStream();
        try {
            String filename = resource.getFilename();
            if (filename != null && filename.endsWith(XML_FILE_EXTENSION)) {
                props.loadFromXML(is);
            } else {
                props.load(is);
            }
        } finally {
            is.close();
        }
    }

    public static Properties loadAllProperties(String resourceName) throws IOException {
        return loadAllProperties(resourceName, null);
    }

    public static Properties loadAllProperties(String resourceName, ClassLoader classLoader) throws IOException {
        Assert.notNull(resourceName, "Resource name must not be null");
        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = ClassUtils.getDefaultClassLoader();
        }
        Enumeration<URL> urls = (classLoaderToUse != null ? classLoaderToUse.getResources(resourceName) :
                ClassLoader.getSystemResources(resourceName));
        Properties props = new Properties();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            URLConnection con = url.openConnection();
            ResourceUtils.useCachesIfNecessary(con);
            InputStream is = con.getInputStream();
            try {
                if (resourceName.endsWith(XML_FILE_EXTENSION)) {
                    props.loadFromXML(is);
                } else {
                    props.load(is);
                }
            } finally {
                is.close();
            }
        }
        return props;
    }

    public static void fillToSystemProperty(Properties props) {
        try {
            props.forEach((k, v) -> {
                String key = (String) k;
                String value = (String) v;
                System.setProperty(key, value);
            });
            log.info("Fill {} property to System properties ok.", props.size());

        } catch (Exception e) {
            log.info("Fill {} property to System properties erro: " + e.getMessage(), props.size());
        }
    }
}
