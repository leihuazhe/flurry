package com.yunji.flurry.url;

import com.yunji.flurry.netty.http.match.UrlMappingResolver;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * UrlMappingTest
 *
 * @author leihz
 * @since 2020-06-19 5:49 下午
 */
public class UrlMappingTest extends TestCase {
    private static final Pattern POST_GATEWAY_PATTERN = Pattern.compile("/(api)/(.*?)/([0-9.]+)/(.*?)");

    private static final Pattern POST_GATEWAY_PATTERN_1 = Pattern.compile("/([^\\s|^/]*)(?:/([^\\s|^/]*))?");

    //    private static final Pattern URL_MAPPING_PATTERN = Pattern.compile("/(api)/(.*?)");
    private static final Pattern URL_MAPPING_PATTERN = Pattern.compile("^(?!/api)/(.*?)");

    private static final String[] WILDCARD_CHARS = {"/", "?", "&", "="};

    private static final String DEFAULT_URL_PREFIX = "api";

    /**
     * 解析 rest风格的请求,包括apiKey 和 没有 apiKey 的 请求
     * etc. /api/com.today.soa.idgen.service.IDService/1.0.0/genId/{apiKey}?cookie=234&user=maple
     * etc. /api/com.today.soa.idgen.service.IDService/1.0.0/genId
     * <p>
     * 解析 requestParam 风格的请求,包括apiKey 和 没有 apiKey 的 请求
     * etc. /api/{apiKey}?cookie=234&user=maple
     * etc. /api?cookie=234&user=maple
     */
    public void test1() {
        String url = "/demo/getUserById";
        boolean matches = URL_MAPPING_PATTERN.matcher(url).matches();

        System.out.println(matches);


    }

    public void test2() {
        String url = "/api/com.today.soa.idgen.service.IDService/1.0.0/genId/{apiKey}?cookie=234&user=maple";
        boolean matches = URL_MAPPING_PATTERN.matcher(url).matches();

        System.out.println(matches);


    }

    public void test3() {
        String url = "/api/demo/getUserById";
        boolean matches = URL_MAPPING_PATTERN.matcher(url).matches();

        System.out.println(matches);


    }

}
