package com.yunji.gateway;

import com.yunji.gateway.util.GateConstants;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.common.utils.StringUtils;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Denim.leihz 2019-08-20 10:16 AM
 */
public class StringServiceTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(GateConstants.WHITE_SERVICES_KEY, "com.yunji.api.ApiService,,com.yunji.api.ApiService2,");
        Set<String> referService = getReferService(properties);

        System.out.println(referService);
    }

    public static Set<String> getReferService(Properties properties) {
        Assert.notNull(properties, "ConfigServer 获取到的外部化配置信息为空，请检查并配置相关信息.");
        String baseKey = GateConstants.WHITE_SERVICES_KEY;
        String whiteStr = properties.getProperty(baseKey);
        char ch = ',';

        if (StringUtils.isNotEmpty(whiteStr)) {
            Set<String> list = new HashSet<>();
            char c;
            int ix = 0, len = whiteStr.length();
            for (int i = 0; i < len; i++) {
                c = whiteStr.charAt(i);
                if (c == ch) {
                    list.add(whiteStr.substring(ix, i));
                    ix = i + 1;
                }
            }



            if (ix >= 0) {
                list.add(whiteStr.substring(ix));
            }
            return list;
        } else {
            throw new IllegalArgumentException("White list String is empty. Please specify the list string on config server.");
        }
    }
}
