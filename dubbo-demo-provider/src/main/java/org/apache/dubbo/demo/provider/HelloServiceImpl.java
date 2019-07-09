/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.demo.provider;

import com.yunji.demo.api.Hello;
import com.yunji.demo.api.HelloService;
import com.yunji.demo.api.OrderRequest;
import com.yunji.demo.api.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);
    private static final Map<String, Long> idNamesMap = new HashMap<>();


    @Override
    public String sayHello(String s) {
        return "Hello " + s;
    }

    @Override
    public String sayHello2(Hello hello) {
        System.out.println("receive hello: " + hello.toString());
        return "Hello,Id: " + hello.getId() + ", name: " + hello.getName();
    }

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        System.out.println("orderRequest: " + orderRequest);
        OrderResponse resp = new OrderResponse();
        resp.setOrderNo(orderRequest.getOrderNo());
        return resp;
    }
}
