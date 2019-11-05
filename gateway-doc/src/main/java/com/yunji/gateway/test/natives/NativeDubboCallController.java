package com.yunji.gateway.test.natives;

import com.yunji.fms.IPopInvoiceQueryService;
import com.yunji.multi.MultiParameterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.hello.HelloService;
import org.apache.dubbo.order.OrderService;
import org.apache.dubbo.test.TestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yunji.business.bug.IOpenPlatformExpressGateway;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Denim.leihz 2019-10-29 2:37 PM
 */
//@RestController
@RequestMapping("/")
@Slf4j
public class NativeDubboCallController {

    private static Map<String, Object> serviceMap = new HashMap<>();

    static {
        serviceMap.put("HelloService", NativeCallUtils.createReference(HelloService.class).get());
        serviceMap.put("IPopInvoiceQueryService", NativeCallUtils.createReference(IPopInvoiceQueryService.class).get());
        serviceMap.put("TestService", NativeCallUtils.createReference(TestService.class).get());
        serviceMap.put("MultiParameterService", NativeCallUtils.createReference(MultiParameterService.class).get());
        serviceMap.put("OrderService", NativeCallUtils.createReference(OrderService.class).get());
        serviceMap.put("IOpenPlatformExpressGateway", NativeCallUtils.createReference(IOpenPlatformExpressGateway.class).get());
    }

    @RequestMapping("/native")
    @Reference()
    public Object nativeCall(String service, String method, String json) throws Exception {
        String[] jsonList = null;
        if (StringUtils.isNotBlank(json)) {
            jsonList = json.split(";");
        }

        Object obj = serviceMap.get(service);
        Object result = NativeCallUtils.invokeMethod(obj, method, jsonList);
        log.info("Native call result: {}", result);
        return result;

    }

}
