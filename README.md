
kewen框架

# 框架说明

# 依赖关系

## 关系图

![avatar](docs/项目Maven依赖关系.png)

### 关系说明

带  <packaging>pom</packaging>的均为管理结构，没有jar包，


##  配置



### 需要用户自己配置的自定义参数，示例未表明的为默认值
```yml
kewen:
  base:
    datasource:
      db-url: "liukewensc.mysql.rds.aliyuncs.com:3306/uucs"
      username: uucs
      password: UUCSuucs
    tenant:
      open: true
      tenant_id_column: tenant_id #租户列名
      ignore_prefix_tables: sys_log,sys_dict  # 排除的表字段前缀
  auth:
    login-endpoint: /login
    logout-endpoint: /logout
    permit-url: /auth/getWebSiteName
    type: security   # security/web
    web:
      store:
        type: token # session/token
```