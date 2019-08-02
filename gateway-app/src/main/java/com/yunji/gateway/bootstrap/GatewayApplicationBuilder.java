package com.yunji.gateway.bootstrap;

/**
 * @author Denim.leihz 2019-07-29 2:12 PM
 */
public class GatewayApplicationBuilder {

    private final GatewayApplication application;

    public GatewayApplicationBuilder() {
        this.application = new GatewayApplication();
    }

    public GatewayApplicationBuilder logLogBanner() {
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
    public GatewayApplicationBuilder registryUrl(String registryUrl) {
        this.application.setRegistryUrl(registryUrl);
        return this;
    }

    public GatewayApplicationBuilder registerShutdownHook() {
        this.application.registerShutdownHook();
        return this;
    }

    public GatewayApplicationBuilder serverPort(int port) {
        this.application.setPort(port);
        return this;
    }

    public GatewayApplicationBuilder diamondId(String dataId) throws Exception {
        this.application.setDiamondId(dataId);
        return this;
    }

    public void start() throws Exception {
        this.application.run();
    }
}
