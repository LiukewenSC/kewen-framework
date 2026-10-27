# storage-boot-sample

## 演示什么

`storage-spring-boot-starter` 的接入方式：七牛云上传凭证、服务端上传、下载链接与文件记录表。默认 H2 内存库，端口 8080；七牛密钥是占位值，上传前必须替换。

## 零配置启动

```bash
cd sample/storage-boot-sample
mvn spring-boot:run
```

默认 `h2` profile，H2 控制台：<http://localhost:8080/h2-console>。启动时会执行 `classpath:sql/basic-support.sql`，建好 `file_detail`、`file_part_detail`、`sys_storage_file` 三张表。

## 打开什么

示例没有自己的 Controller，能力来自 starter 的端点：

| 请求 | 用途 |
| --- | --- |
| `POST /storage/genUploadToken` | 生成七牛上传凭证 |
| `POST /storage/upload` | 服务端流式上传 |
| `GET /storage/getDownloadUrl?fileId=...` | 单文件下载链接 |
| `GET /storage/listDownloadUrl` | 批量下载链接 |
| `GET /storage/getFileById?fileId=...` | 按文件 ID 查下载信息 |
| `ANY /storage/upload/callback` | 七牛上传回调 |

## 验证清单

1. 启动后访问 `/storage/genUploadToken`，确认端点可达；没有真实七牛密钥时会返回上传策略或 SDK 相关错误，属于预期。
2. 把 `application.yml` 里的 `access-key` / `secret-key` / `bucket` / `download-domain` 换成真实值，再走一次上传 → 回调 → 下载。
3. 打开 H2 控制台，确认 `file_detail` / `sys_storage_file` 有记录。

## 不覆盖什么

- 不提供前端上传页面；上传由调用方用凭证直传七牛或调用服务端上传端点。
- 不演示其他云存储；`cu.storage.type` 当前未接线，固定七牛。
- 不覆盖七牛回调的公网可达性、签名校验与生产域名配置；这些需要在真实环境中验证。

## 相关文档

- [storage-spring-boot-starter](../../boot/storage-spring-boot-starter/README.md)
- [SQL 脚本](../../relation/README.md)
- [项目根 README](../../README.md)
