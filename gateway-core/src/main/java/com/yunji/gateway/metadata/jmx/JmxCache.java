package com.yunji.gateway.metadata.jmx;

import com.yunji.gateway.metadata.OptimizedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

public class JmxCache implements JmxCacheMBean {

    private static final Logger logger = LoggerFactory.getLogger(JmxCache.class);

    private ConcurrentMap<String, OptimizedService> serviceMetadataMap;

    public JmxCache(ConcurrentMap<String, OptimizedService> serviceMetadataMap) {
        this.serviceMetadataMap = serviceMetadataMap;
    }

    /*
     * 服务信息
     *
     */
    @Override
    public void print() {
        logger.info("start print info........................................................................");
        if (serviceMetadataMap.size() == 0) {
            logger.info("当前已缓存元数据服务接口数为0 .");

        } else {
            int[] order = {1};
            serviceMetadataMap.forEach((name, service) -> {
                logger.info("已缓存元数据服务接口: {}. [{}]", order[0]++, name);
            });
        }

        logger.info("end print info........................................................................");
    }
}
