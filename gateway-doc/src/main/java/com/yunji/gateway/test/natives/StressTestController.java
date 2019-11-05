package com.yunji.gateway.test.natives;

import org.apache.dubbo.order.OrderRequest;
import org.apache.dubbo.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Denim.leihz 2019-10-29 10:57 PM
 */

@RestController
@RequestMapping("/")
public class StressTestController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/stress")
    public Object stressTest(@RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }
}
