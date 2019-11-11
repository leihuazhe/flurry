package com.yunji.flurry.core;

import com.yunji.flurry.metadata.core.RegistryListener;
import org.apache.dubbo.common.URL;

import java.util.List;

/**
 * @author Denim.leihz 2019-08-17 7:00 PM
 */
public interface RegistryMetadataClient {

    List<URL> subscribe(String serviceName, RegistryListener registryListener);


    void unsubscribe(String serviceName);
}
