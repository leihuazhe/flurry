package com.yunji.gateway.http;

/**
 * GatewayHealthStatus 网关 健康状况
 *
 * @author maple 2018.08.28 下午3:21
 */
public enum GatewayHealthStatus {
    /**
     * 不健康的运行状态
     */
    YELLOW(500),
    /**
     * 正常运行状态
     */
    GREEN(200);

    private int status;

    GatewayHealthStatus(int status) {
        this.status = status;
    }

    public static GatewayHealthStatus findByValue(int value) {
        switch (value) {
            case 500:
                return YELLOW;

            case 200:
                return GREEN;
            default:
                return null;
        }
    }
}
