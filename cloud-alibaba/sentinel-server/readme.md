
### 配置
    可以看sentinel源码，修改其中的配置，也可以在-D参数中传入

### 启动命令
```bash
java -Dserver.port=8801 -Dcsp.sentinel.dashboard.server=localhost:8801 -Dproject.name=sentinel-dashboard -jar cloud-alibaba\sentinel-server\sentinel-dashboard-1.8.6.jar
```