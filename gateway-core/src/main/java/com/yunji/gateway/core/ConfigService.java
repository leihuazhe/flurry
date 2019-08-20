package com.yunji.gateway.core;

import com.yunji.gateway.config.DiamondBean;

import java.util.Properties;

/**
 * 配置服务
 *
 * @author Denim.leihz 2019-08-16 8:01 PM
 */
public interface ConfigService {

    /**
     * 启动配置,如果是 diamond，uniqueId对应 diamodId，如果是其他实现，请自行解析.
     */
    void start(String uniqueId);


    /**
     * 得到配置信息并可回调
     *
     * @param listener
     * @return
     */
    Properties getConfig(ConfigListener listener);

    /**
     * 得到配置信息
     *
     * @return
     */
    Properties getConfig();

}