package org.apache.dubbo.gateway;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Denim.leihz 2019-07-23 5:08 PM
 */
public class GlobalReferenceConfig {
    //todo 通过此配置对不同的 gateway service 创建时，可以设置不同的参数，比如超时时间等等.
    //todo 结合 diamondy.
    public static Map<String, Object> buildGlobalConfig() {

        return new HashMap<>();
    }
}
