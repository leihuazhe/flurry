package com.yunji.gateway.core;

import org.apache.dubbo.config.ApplicationConfig;

/**
 * @author Denim.leihz 2019-08-02 5:09 PM
 */
public class ApplicationConfigHolder {

    private static ApplicationConfig application;

    public static ApplicationConfig getApplication() {
        return application;
    }

    public static void setApplication(ApplicationConfig application) {
        ApplicationConfigHolder.application = application;
    }
}
