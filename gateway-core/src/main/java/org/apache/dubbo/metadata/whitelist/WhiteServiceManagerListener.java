package org.apache.dubbo.metadata.whitelist;

import com.taobao.diamond.manager.ManagerListener;
import org.apache.dubbo.metadata.util.MetadataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author Denim.leihz 2019-07-26 10:27 AM
 */
public class WhiteServiceManagerListener implements ManagerListener {
    private Logger logger = LoggerFactory.getLogger(WhiteServiceManagerListener.class);

    private final ConfigContext context;

    public WhiteServiceManagerListener(ConfigContext context) {
        this.context = context;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    /**
     * 接收配置信息
     */
    @Override
    public void receiveConfigInfo(String configInfo) {
        logger.info("检测到diamond中的数据发生变化-->", configInfo);
        List<String> whiteServices = null;
        try {
            whiteServices = MetadataUtil.parseConfig(configInfo);
        } catch (Exception e) {
            logger.error("转换diamond变化数据失败", e);
        }

        //更新config
        if (whiteServices != null) {
            context.setWhiteServiceSet(whiteServices);
            context.refresh();
        }
    }
}
