package org.apache.dubbo.config;

import lombok.Builder;
import lombok.Data;

/**
 * @author Denim.leihz 2019-07-31 3:20 PM
 */
@Builder
@Data
public class RpcRequest {

    private String serviceName;

    private String method;

    private String version;

    private String group;

    private String[] paramsType;

    private Object[] paramsValue;


}
