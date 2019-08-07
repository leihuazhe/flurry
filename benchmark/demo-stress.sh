#!/usr/bin/env bash

echo 'Start to invoke'

#wrk -t2 -c256 -d60s -T5 --script=./status.lua --latency http://172.31.2.185:9000/api/org.apache.dubbo.order.OrderService/1.0.0/createOrder



wrk -t2 -c256 -d60s -T5 --script=./statusWeb.lua --latency http://172.31.2.185:8080/invoke?parameter=%7b%22orderNo%22%3a%2219921023%22%2c%22productCount%22%3a123%2c%22totalAmount%22%3a%224561.35%22%2c%22storeId%22%3a%2212799001%22%2c%22orderDetialList%22%3a%5b%7b%22orderNo%22%3a%2219921023%22%2c%22detailSeq%22%3a1%2c%22Amount%22%3a%224561%22%2c%22remark%22%3a%22%e4%b8%bb%e8%b4%a6%e6%88%b7%22%7d%2c%7b%22orderNo%22%3a%2219921023%22%2c%22detailSeq%22%3a2%2c%22Amount%22%3a%220.35%22%2c%22remark%22%3a%22%e5%88%a9%e6%81%af%22%7d%5d%7d
