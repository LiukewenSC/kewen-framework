# storage-spring-boot-starter

## 这是什么

文件存储自动装配，当前实现固定为七牛云：上传凭证、服务端上传、下载链接、上传回调与文件记录落库。

## 何时使用

- 使用七牛云对象存储，需要现成的上传/下载端点与文件记录表。
- 不适用：使用 OSS、COS 等其他云存储；`cu.storage.type` 当前不生效，换存储需要自己实现。

## 5 分钟接入

**依赖**：`storage-spring-boot-starter` + `basic-spring-boot-starter` + 数据库驱动。

```xml
<dependencies>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>basic-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>storage-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**建表**：执行 [relation/basic-support/basic-support.sql](../../relation/basic-support/basic-support.sql)，需要 `file_detail`、`file_part_detail`、`sys_storage_file` 三张表。

**最小配置**：

```yaml
cu:
  storage:
    access-key: your_qiniu_access_key
    secret-key: your_qiniu_secret_key
    bucket: your_bucket_name
    download-domain: your_download_domain
    region: region2          # region0/huadong、region1/huabei、region2/huanan、region3/beimei、region4/xinjiapo、auto
    is-public: true
```

**验证**：先用真实七牛密钥配置，调用 `POST /storage/genUploadToken` 生成上传凭证；上传成功后用 `GET /storage/getDownloadUrl?fileId=...` 取下载地址。没有七牛账号时只能验证应用启动与端点路由，上传/下载会因密钥无效失败。

完整示例见 [sample/storage-boot-sample](../../sample/storage-boot-sample/README.md)，默认 H2、端口 8080；七牛密钥是占位值，上传前需替换。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `StorageAutoConfiguration` | 无 | `QiNiuStorageTemplate`、`FileStorageEndpoint`、`FileStorageCallbackEndpoint`、`FileResponseBodyAdvance`、`DefaultFileStorageProcessor`；扫描 `com.cu.framework.storage.web.mp` 及其 Mapper |

## 配置项

| key | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cu.storage.type` | — | 否 | **未接线**：当前固定使用七牛，配置本项不会切换存储类型 |
| `cu.storage.access-key` | — | 使用时必填 | 七牛 AccessKey |
| `cu.storage.secret-key` | — | 使用时必填 | 七牛 SecretKey |
| `cu.storage.bucket` | — | 使用时必填 | 七牛存储桶名称 |
| `cu.storage.root-path` | `""` | 否 | 对象 key 前缀；空串表示不加前缀 |
| `cu.storage.is-public` | `true` | 否 | 公开空间；false 时生成的下载链接带有效期签名 |
| `cu.storage.download-domain` | — | 下载时必填 | 下载域名；生成下载链接时使用 |
| `cu.storage.upload-callback-url` | — | 否 | 上传成功回调地址；为空时不设置 callbackUrl 策略 |
| `cu.storage.region` | `region2` | 否 | 七牛区域；非法值启动失败 |
| `cu.storage.persistent-notify-url` | — | 否 | 数据处理异步通知地址；为空时不写入 persistentNotifyUrl 策略 |
| `cu.storage.use-https-download` | `false` | 否 | 下载链接是否使用 https；依赖 `download-domain` 已配好证书 |

完整键表见 [docs/application.yml](../../docs/application.yml)。

## 端点

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/storage/genUploadToken` | 生成上传凭证 |
| POST | `/storage/upload` | 服务端流式上传 |
| GET | `/storage/getDownloadUrl` | 单文件下载链接 |
| GET | `/storage/listDownloadUrl` | 批量下载链接 |
| ANY | `/storage/upload/callback` | 七牛上传回调 |
| POST | `/storage/upload/callbackAsync` | 异步回调入口 |
| GET | `/storage/getFileById` | 按文件 ID 查下载信息 |

## 前置条件与已知空转项

- **DataSource 是硬前提**：需要 `sys_storage_file`、`file_detail`、`file_part_detail` 三张表。
- 上传功能需要 `access-key` / `secret-key` / `bucket`；下载链接需要 `download-domain`。
- `region` 无法识别时 `StorageAutoConfiguration#resolveRegion` 直接抛 `IllegalArgumentException`，避免静默落到错误机房。
- `cu.storage.type` 当前无消费方，固定七牛。

## 相关文档

- [basic-spring-boot-starter](../basic-spring-boot-starter/README.md)
- [SQL 脚本](../../relation/README.md)
- [storage 示例](../../sample/storage-boot-sample/README.md)
- [全量配置参考](../../docs/application.yml)
