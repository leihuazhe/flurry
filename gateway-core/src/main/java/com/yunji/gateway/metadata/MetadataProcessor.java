package com.yunji.gateway.metadata;

import com.yunji.gateway.config.DiamondConfigService;
import com.yunji.gateway.core.ConfigListener;
import org.apache.dubbo.common.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.yunji.gateway.util.GateConstants.WHITE_SERVICES_KEY;

/**
 * @author Denim.leihz 2019-08-16 8:48 PM
 */
public class MetadataProcessor implements ConfigListener {

    private DiamondConfigService diamondConfigService;

    private List<String> whiteServiceList;

    public void init() {
        diamondConfigService = DiamondConfigService.getInstance();
        //load diamond properties and register callback.
        load(diamondConfigService.getConfig(this));
    }


    /**
     * 通知回调
     *
     * @param properties 外部化配置文件
     */
    @Override
    public void notify(Properties properties) {
        load(properties);
    }

    private void load(Properties properties) {
        String whiteList = properties.getProperty(WHITE_SERVICES_KEY);

        if (StringUtils.isNotEmpty(whiteList)) {
            String[] split = whiteList.split(",");
            whiteServiceList = Arrays.asList(split);
        }

    }
}
