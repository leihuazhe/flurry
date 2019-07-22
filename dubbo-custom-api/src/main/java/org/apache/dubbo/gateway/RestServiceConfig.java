package org.apache.dubbo.gateway;

import java.util.Map;

/**
 * @author Denim.leihz 2019-07-21 4:17 PM
 */
public class RestServiceConfig {

    private String interfaceName;

    private String version;

    private String group;

    private Map<String, Object> referenceAttributes;

    public RestServiceConfig(String interfaceName, String version, String group, Map<String, Object> referenceAttributes) {
        this.interfaceName = interfaceName;
        this.version = version;
        this.group = group;
        this.referenceAttributes = referenceAttributes;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Map<String, Object> getReferenceAttributes() {
        return referenceAttributes;
    }

    public void setReferenceAttributes(Map<String, Object> referenceAttributes) {
        this.referenceAttributes = referenceAttributes;
    }
}
