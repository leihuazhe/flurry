package com.yunji.gateway.config;

import static com.yunji.gateway.util.GateConstants.DEFAULT_DATA_ID;
import static com.yunji.gateway.util.GateConstants.DIAMOND_TIME_OUT;
import static com.yunji.gateway.util.GateConstants.POLLING_INTERVAL_TIME;

/**
 * @author Denim.leihz 2019-08-16 8:02 PM
 */
public class DiamondBean {
    /**
     * 环境变量没有显式设置,则 dataid
     */
    private String dataId = DEFAULT_DATA_ID;
    private int pollingIntervalTime = POLLING_INTERVAL_TIME;
    private long timeout = DIAMOND_TIME_OUT;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public int getPollingIntervalTime() {
        return pollingIntervalTime;
    }

    public void setPollingIntervalTime(int pollingIntervalTime) {
        this.pollingIntervalTime = pollingIntervalTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
