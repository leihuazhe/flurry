package com.yunji.flurry.metadata.core;

import com.yunji.flurry.metadata.common.ChangeType;
import org.apache.dubbo.common.URL;

import java.util.List;

/**
 * @author Denim.leihz 2019-08-17 12:54 AM
 */
public interface RegistryListener {

    /**
     * 通知回调,这是我们订阅的服务在注册中心变更后,callback 回来的消息.
     *
     * @param serviceName  需要 subscribe 的服务名称
     * @param path         监听服务路径
     * @param childrenUrls providers 下面的内容
     */
    void notify(String serviceName, String path, List<URL> childrenUrls, ChangeType changeType);

}
