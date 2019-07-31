package org.apache.dubbo.metadata;

import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.metadata.discovery.MetadataListener;
import org.apache.dubbo.metadata.discovery.ServiceDefinition;
import org.apache.dubbo.metadata.whitelist.WhiteServiceEvent;

import java.util.EventListener;

/**
 * @author Denim.leihz 2019-07-31 6:49 PM
 */
@SPI("auto-discovery")
@Adaptive
public interface MetadataResolver extends EventListener {

    OptimizedMetadata.OptimizedService get(String service, String version);

    void resolveServiceMetadata(ServiceDefinition serviceDefinition, MetadataListener metadataListener);

    void removeServiceMetadataCache(String path);

    void onServiceListChanged(WhiteServiceEvent writerEvent);

    void clear();

}
