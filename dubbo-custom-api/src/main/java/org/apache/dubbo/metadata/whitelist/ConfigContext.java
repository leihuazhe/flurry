package org.apache.dubbo.metadata.whitelist;


import org.apache.dubbo.metadata.ServiceMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Denim.leihz 2019-07-26 10:14 AM
 */
public class ConfigContext {
    private static final Logger logger = LoggerFactory.getLogger(ConfigContext.class);

    private String registryUrl;

    private Set<String> whiteServiceList;


    private List<ServiceMetadataResolver> resolvers = new ArrayList<>();


    public Set<String> getWhiteServiceSet() {
        return whiteServiceList;
    }

    public void setWhiteServiceSet(List<String> whiteServiceList) {
        this.whiteServiceList = new HashSet<>(whiteServiceList);
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    public void addListener(ServiceMetadataResolver metadataResolver) {
        resolvers.add(metadataResolver);
    }

    public void removeListener(ServiceMetadataResolver metadataResolver) {
        resolvers.remove(metadataResolver);
    }

    public void refresh() {
        logger.info("ConfigContext context changed,refresh the white list");
        notifyWhiteListChange();
    }

    private void notifyWhiteListChange() {
        WhiteServiceEvent writerEvent = new WhiteServiceEvent(this);
        for (ServiceMetadataResolver resolver : resolvers) {
            resolver.onServiceListChanged(writerEvent);
        }
    }
}
