package com.yunji.service.doc;

import org.apache.dubbo.metadata.MetadataResolver;
import org.apache.dubbo.metadata.OptimizedMetadata;
import org.apache.dubbo.metadata.discovery.MetadataListener;
import org.apache.dubbo.metadata.discovery.ServiceDefinition;
import org.apache.dubbo.metadata.whitelist.WhiteServiceEvent;

/**
 * @author Denim.leihz 2019-07-31 9:46 PM
 */
public class XmlMetadataResolver implements MetadataResolver {

    @Override
    public OptimizedMetadata.OptimizedService get(String service, String version) {
        return null;
    }

    @Override
    public void resolveServiceMetadata(ServiceDefinition serviceDefinition, MetadataListener metadataListener) {

    }

    @Override
    public void removeServiceMetadata(String path) {

    }

    @Override
    public void onServiceListChanged(WhiteServiceEvent writerEvent) {

    }

    @Override
    public void clear() {

    }
}
