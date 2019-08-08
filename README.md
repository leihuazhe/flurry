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


1.环境变量中配置好 diamond 的信息,一般配置在服务器的 `/etc/proflie` 上
```properties
CONFIG_ENV=local
DIAMOND_SERVER_HOST=tdiamond.yunjiweidian.com
```
1.1如果是本地测试的话,可以通过-D参数指定
```properties
config_env=local
diamond_server_host=tdiamond.yunjiweidian.com
```

2.配置好 dataId,通过环境变量 `gateway_diamond_dataId` 注入 或者 `-Dgateway.diamond.dataId` 参数指定或不需要指定，
默认使用dataId 为  `dubbo_gateway_config`,用户使用时，这一步可以使用默认。

3.在 diamond 上配置好白名单信息，配置方式如下图:


### 2.选择注册中心，启动服务,启动网关( 本地测试再启动 gateway-doc)

选择注册中心,通过在配置文件或者环境变量中指定 `dubbo.registry.address`.

**注意：** gateway-app 网关 和 用户自己开发的 provider 需要使用同一个 
zookeeper 集群。


1.启动 [app-service-demo](https://gitlab.yunjiglobal.com/leihz/app-service-demo) 服务,启动成功。


2.启动 gateway-doc,访问 localhost:8000,就可以查看刚才启动的服务信息，并可以在线请求测试

我们需要了解的是，在通过 gateway-doc 测试请求的时候，请求是以 json 的模式来的，我们需要了解这个接口的请求参数的 json形式，并记下来，因为会在后面
网关上面使用到。

3。启动网关 gateway-app, 网关暴露的端口为 9000，通过 post 的方式请求后台服务，url格式如下:

```properties
127.0.0.1:9000/api/{serviceName}/{version}/{methodName}
```
然后在body中传入下面参数信息:
```properties
pramater={json}
```
parameter 传入就是上面说的 请求参数的json化格式。