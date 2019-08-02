请求说明

```
http://gateway.today.cn/api/{serviceName}/{version}/{methodName}/{apikey}
参数：
parameter = {"body":{"request":{"pageRequest":{"start":0,"limit":10}}}}
timestamp=1525946628000
secret2=(apikey+tmiestamp+password+parameter=>MD5)(小写)

```