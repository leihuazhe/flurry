package com.yunji.flurry;

import com.yunji.flurry.metadata.OptimizedService;
import com.yunji.flurry.metadata.tag.Service;

import javax.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.dubbo.common.utils.ClassUtils.getClassLoader;

/**
 * @author Denim.leihz 2019-09-18 11:57 PM
 */
public class UnmarshalTest {

    public static void main(String[] args) {
        String metaString = readConfigFiles("com.yunji.oms.agent.api.AgentOpenApiService.xml");

        try (StringReader reader = new StringReader(metaString)) {
            Service service = JAXB.unmarshal(reader, Service.class);
            OptimizedService optimizedService = new OptimizedService(service);
            System.out.println(optimizedService);
        }
    }

    public static String readConfigFiles(String providerInterface) {
        String fileName = providerInterface;
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = getClassLoader(UnmarshalTest.class);
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                if (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    return loadResource(resourceURL);
                }
            }
            return "";
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    private static String loadResource(java.net.URL resourceURL) throws IOException {
        StringBuilder append = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                append.append(line);
            }
        }

        return append.toString();
    }
}
