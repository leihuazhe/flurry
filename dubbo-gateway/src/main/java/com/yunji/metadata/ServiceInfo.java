package com.yunji.metadata;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Denim.leihz 2019-07-08 9:51 PM
 */
public class ServiceInfo {

    public final String versionName;

    public final String host;

    public final int port;

    private AtomicInteger activeCount;

    public ServiceInfo(String host, Integer port, String versionName) {

        this.versionName = versionName;
        this.host = host;
        this.port = port;

        this.activeCount = new AtomicInteger(0);
    }

    public boolean equalTo(ServiceInfo sinfo) {
        if (!versionName.equals(sinfo.versionName))
            return false;

        if (!host.equals(sinfo.host))
            return false;

        if (port != sinfo.port)
            return false;

        return true;
    }

    public AtomicInteger getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(AtomicInteger activeCount) {
        this.activeCount = activeCount;
    }
}
