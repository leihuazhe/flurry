package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.demo.DemoService;
import org.apache.dubbo.rpc.service.GenericService;

public class ServiceProcessor {

    private final DemoService demoService;

    private final GenericService genericService;


    public ServiceProcessor(DemoService demoService, GenericService genericService) {
        this.demoService = demoService;
        this.genericService = genericService;
    }
}
