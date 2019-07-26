package org.apache.dubbo.metadata.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Denim.leihz 2019-07-23 3:53 PM
 */
public class ServiceDefinition {
    private String serviceInterface;
    private String group;
    private String version;
    private List<Instance> instances = new ArrayList<>();

    public static class Instance {
        String host;
        int port;

        public Instance(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instance instance = (Instance) o;
            return port == instance.port &&
                    Objects.equals(host, instance.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port);
        }

        @Override
        public String toString() {
            return "Instance{ host='" + host + '\'' + ", port=" + port + '}';
        }
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public static class Builder {
        ServiceDefinition info = new ServiceDefinition();

        public ServiceDefinition serviceInterface(String serviceInterface) {
            info.serviceInterface = serviceInterface;
            return info;
        }

        public ServiceDefinition group(String group) {
            info.group = group;
            return info;

        }

        public ServiceDefinition version(String version) {
            info.version = version;
            return info;
        }

        public ServiceDefinition instance(Instance instance) {
            info.instances.add(instance);
            return info;
        }

        public ServiceDefinition build() {
            return info;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceDefinition that = (ServiceDefinition) o;
        return Objects.equals(serviceInterface, that.serviceInterface) &&
                Objects.equals(group, that.group) &&
                Objects.equals(version, that.version) &&
                Objects.equals(instances, that.instances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInterface, group, version, instances);
    }

    @Override
    public String toString() {
        return "ServiceDefinition{" +
                "serviceInterface='" + serviceInterface + '\'' +
                ", group='" + group + '\'' +
                ", version='" + version + '\'' +
                ", instances=" + instances +
                '}';
    }
}
