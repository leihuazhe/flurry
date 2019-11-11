package com.yunji.flurry.core;

import java.util.Properties;

/**
 * @author Denim.leihz 2019-08-16 7:49 PM
 */
public interface ConfigListener {
    /**
     * 通知回调
     *
     * @param properties 外部化配置文件
     */
    void notify(Properties properties);
}
