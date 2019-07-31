package org.apache.dubbo.config;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author Denim.leihz 2019-07-31 7:41 PM
 */
@Builder
@Data
public class MetaServiceInfo {

    private String serviceName;

    private String methodName;

    private String version;

    private String group;

    private Map<String, Object> referenceAttributes;

}
