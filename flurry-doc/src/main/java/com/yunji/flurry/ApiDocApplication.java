package com.yunji.flurry;

import com.yunji.flurry.doc.properties.ApiDocProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 启动类
 *
 * @author maple.lei
 * @date 2018-01-12 20:00
 */
@SpringBootApplication
@EnableConfigurationProperties(ApiDocProperties.class)
@ImportResource(locations = {"classpath:spring.xml"/*, "classpath:dubbo-consumer.xml"*/})
public class ApiDocApplication /*implements CommandLineRunner*/ {

    public static void main(String[] args) {
        System.setProperty("dubbo.application.qos.enable", "false");
        new SpringApplicationBuilder()
                .bannerMode(Banner.Mode.OFF)
                .web(true)
                .sources(ApiDocApplication.class)
                .run(args);
    }

    /**
     * 针对前端ajax的消息转换器
     *
     * @return
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter mappingConverter = new MappingJackson2HttpMessageConverter();
        List<MediaType> list = new ArrayList<>();
        list.add(MediaType.TEXT_HTML);
        list.add(MediaType.TEXT_PLAIN);
        list.add(MediaType.APPLICATION_JSON_UTF8);
        mappingConverter.setSupportedMediaTypes(list);
        return mappingConverter;
    }
}
