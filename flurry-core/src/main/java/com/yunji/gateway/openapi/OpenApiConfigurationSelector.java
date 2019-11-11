package com.yunji.gateway.openapi;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author Denim.leihz 2019-08-02 4:37 PM
 */
public class OpenApiConfigurationSelector implements ImportSelector {
    /**
     * Select and return the names of which class(es) should be imported based on
     * the {@link AnnotationMetadata} of the importing @{@link Configuration} class.
     *
     * @param importingClassMetadata
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        //加载 Bean
        return new String[]{DubboOpenApiConfiguration.class.getName()};
    }
}
