package org.apache.dubbo.metadata.discovery;


import org.apache.dubbo.remoting.zookeeper.ChildListener;

/**
 * @author Denim.leihz 2019-07-23 3:53 PM
 */
public class RegistryDefinition {

    private final String serviceInterface;
    private final String fullPath;

    private ChildListener childListener;

    public RegistryDefinition(String serviceInterface, String fullPath) {
        this.serviceInterface = serviceInterface;
        this.fullPath = fullPath;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setChildListener(ChildListener childListener) {
        this.childListener = childListener;
    }

    public ChildListener getChildListener() {
        return childListener;
    }
}
