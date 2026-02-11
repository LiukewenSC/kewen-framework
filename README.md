
kewen框架

# 框架说明

# 依赖关系

## 关系图

![avatar](docs/项目Maven依赖关系.png)

### 关系说明

带  <packaging>pom</packaging>的均为管理结构，没有jar包，


##  配置



### 需要用户自己配置的自定义参数

示例表明的为默认值

```yaml
kewen:
  message:
    fang-tang:
      key: ''
      domain: https://sctapi.ftqq.com
  request:
    message:
      fang-tang: true
    persistent:
      database: true
  tenant:
    open: false
```