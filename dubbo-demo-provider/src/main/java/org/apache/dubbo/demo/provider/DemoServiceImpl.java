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

import org.apache.dubbo.demo.Column;
import org.apache.dubbo.demo.DemoService;
import org.apache.dubbo.demo.Result;
import org.apache.dubbo.rpc.RpcContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DemoServiceImpl implements DemoService {
    private static final Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);

    private static final Map<String, Long> idNamesMap = new HashMap<>();


    static {
        idNamesMap.put("Maple", 1023L);
        idNamesMap.put("May", 327L);
        idNamesMap.put("Me", 928L);
        idNamesMap.put("Redis", 6379L);
    }


    @Override
    public String sayHello(String name) {
        logger.info("Hello " + name + ", request from consumer: " + RpcContext.getContext().getRemoteAddress());
        return "Hello " + name + ", response from provider: " + RpcContext.getContext().getLocalAddress();
    }

    @Override
    public List<Long> getMultiIds(String name) {
        return new ArrayList<>(idNamesMap.values());
    }

    @Override
    public Map<String, Long> getMultiIdMaps(String name) {
        return idNamesMap;
    }

    @Override
    public Result getResultByColumn(Column column, int id) {
        logger.info(column.toString());
        return new Result(1023 + id, column.getName() + "xxx");
    }

}
