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
     * 启动配置
     */
    void start(DiamondBean diamondBean);


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