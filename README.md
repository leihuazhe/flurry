## Dubbo 异步服务网关

本工程主要分为3个模块:

- gateway-app 
    - 流式网关入口,启动后提供 web 调用,可部署多套,然后前端直接通过 http 请就可以直接请求到 dubbo 后台服务.

- gateway-core
   - 流式网关核心基础组建工程,包括序列化、元数据获取、动态服务发现等
  
- gateway-doc
    - 这是一个供用户测试的文档站点工程,连接好 zookeeper 地址后，可直接通过此站点来获取服务文档信息，并可以在线通过 json 请求的模式来请求服务。
    **注意**：生产环境不需要部署此工程.
    

## 使用介绍

### 1.配置 diamond 和 白名单信息
> 在使用网关时,我们需要确定网关要引用哪些dubbo服务，现网有几千上万个服务,我们不可能全部引入，所以这里会有一个白名单的概念。我们需要多少服务，就在白名单中配置这些服务信息，主要就是配置具体服务的接口
的**全限定名**,支持动态刷新的话，我们将其配置到 diamond 上。

- 确定需要引用的服务

比如我们开发了两个服务 `org.apache.dubbo.hello.HelloService`,`org.apache.dubbo.order.OrderService`,我们将其配置到 diamond 上。网关目前已经集成了 diamond了，
只需要我们配置下diamond的group和 host以及 dataId等即可。

![图片](benchmark/pic/doc_config_1.jpg)

