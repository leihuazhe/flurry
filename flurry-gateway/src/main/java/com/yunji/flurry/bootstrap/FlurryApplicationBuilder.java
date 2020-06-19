package com.yunji.flurry.bootstrap;

import com.yunji.flurry.config.nacos.NacosConfigService;
import com.yunji.flurry.util.PropertiesLoaderUtils;

import java.util.Properties;

/**
 * @author Denim.leihz 2019-07-29 2:12 PM
 */
public class FlurryApplicationBuilder {

    private static final String DEFAULT_PROPERTIES_NAME = "application.properties";

    private final FlurryApplication application;

    public FlurryApplicationBuilder() {
        this.application = new FlurryApplication();
        loadProperties();
    }

    public FlurryApplicationBuilder logLogBanner() {
        String banner =
                "\n\n __   __  _   _   _   _       _   ___     ____    _   _   ____    ____     ___       ____      _      _____   _____  __        __     _     __   __\n" +
                        " \\ \\ / / | | | | | \\ | |     | | |_ _|   |  _ \\  | | | | | __ )  | __ )   / _ \\     / ___|    / \\    |_   _| | ____| \\ \\      / /    / \\    \\ \\ / /\n" +
                        "  \\ V /  | | | | |  \\| |  _  | |  | |    | | | | | | | | |  _ \\  |  _ \\  | | | |   | |  _    / _ \\     | |   |  _|    \\ \\ /\\ / /    / _ \\    \\ V / \n" +
                        "   | |   | |_| | | |\\  | | |_| |  | |    | |_| | | |_| | | |_) | | |_) | | |_| |   | |_| |  / ___ \\    | |   | |___    \\ V  V /    / ___ \\    | |  \n" +
                        "   |_|    \\___/  |_| \\_|  \\___/  |___|   |____/   \\___/  |____/  |____/   \\___/     \\____| /_/   \\_\\   |_|   |_____|    \\_/\\_/    /_/   \\_\\   |_|  \n" +
                        "                                                                                                                                                   \n";
        this.application.setBanner(banner);
        return this;
    }

    /**
     * 注册中心 url
     */
    public FlurryApplicationBuilder registryUrl(String registryUrl) {
        this.application.setRegistryUrl(registryUrl);
        return this;
    }

    public FlurryApplicationBuilder registerShutdownHook() {
        this.application.registerShutdownHook();
        return this;
    }

    public FlurryApplicationBuilder serverPort(int port) {
        this.application.setPort(port);
        return this;
    }

    public FlurryApplicationBuilder diamondId(String dataId) throws Exception {
        this.application.setDiamondId(dataId);
        return this;
    }

    public void loadProperties() {
        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties(DEFAULT_PROPERTIES_NAME);
            PropertiesLoaderUtils.fillToSystemProperty(properties);
        } catch (Exception e) {
            throw new IllegalArgumentException("Classpath 下缺少配置文件: " + DEFAULT_PROPERTIES_NAME);
        }
    }

    public FlurryApplicationBuilder startNacos() {
        try {
            NacosConfigService.getInstance().start();
            return this;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void start() throws Exception {
        this.application.run();
    }
}
