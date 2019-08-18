package com.yunji.gateway.openapi;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Denim.leihz 2019-08-02 4:43 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(OpenApiConfigurationSelector.class)
public @interface EnableOpenApi {

}