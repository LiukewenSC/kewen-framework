# Kewen Framework

> 基于 Spring Boot 的企业级权限管理框架，提供完整的 RBAC 权限控制、多租户支持、文件存储等企业级功能

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-8+-green.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)]()
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)]()

## 📖 目录

- [项目简介](#项目简介)
- [核心特性](#核心特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [权限系统详解](#权限系统详解)
- [模块说明](#模块说明)
- [配置指南](#配置指南)
- [使用示例](#使用示例)
- [常见问题](#常见问题)
- [参与贡献](#参与贡献)
- [许可证](#许可证)

## 🎯 项目简介

Kewen Framework 是一个基于 Java + Spring Boot 的企业级后端框架，专注于解决企业应用中的权限管理问题。通过引入 RBAC（基于角色的访问控制）模型和注解驱动的权限控制机制，开发者可以快速构建安全、灵活的权限管理系统，避免在每个业务系统中重复设计和实现权限体系。

### 为什么选择 Kewen Framework？

- **开箱即用**：通过 Spring Boot Starter 机制，只需引入依赖即可使用完整的权限系统
- **注解驱动**：使用简单的注解即可完成菜单权限和数据权限的控制
- **高度可扩展**：预留丰富的扩展接口，支持自定义权限模型和业务逻辑
- **完整生态**：提供多租户、文件存储、请求日志等企业级功能模块

## ✨ 核心特性

### 1. 权限管理系统

- ✅ **RBAC 权限模型**：基于用户-角色-部门的完整权限体系
- ✅ **菜单权限控制**：通过 `@AuthMenu` 注解实现接口访问权限控制
- ✅ **数据权限控制**：支持数据范围查询、数据操作权限校验
- ✅ **注解驱动**：仅通过注解即可完成复杂的权限验证逻辑
- ✅ **自动菜单注册**：应用启动时自动将 API 路径注册到数据库

### 2. 安全认证

- 🔐 **Spring Security 集成**：基于 Spring Security 构建的安全框架
- 🔐 **JSON 登录**：改造传统表单登录为适合前后端分离的 JSON 登录
- 🔐 **Token 认证**：支持 Token 方式的无状态认证
- 🔐 **会话管理**：支持最大会话数控制、防并发登录
- 🔐 **记住我功能**：支持 Remember-Me 长效登录

### 3. 企业级功能

- 📦 **多租户支持**：内置多租户架构，支持数据隔离
- 📦 **文件存储**：支持七牛云等多平台文件存储
- 📦 **请求日志**：完整的请求日志记录和追踪
- 📦 **消息通知**：集成方糖等消息推送服务
- 📦 **代码生成**：一键生成 MyBatis-Plus 相关代码

## 🛠 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **核心框架** | Spring Boot | 2.7.18 |
| | Spring | 5.x |
| | Spring Cloud | 2021.0.9 |
| | Spring Cloud Alibaba | 2021.0.5.0 |
| **安全框架** | Spring Security | - |
| **持久层** | MyBatis-Plus | 3.5.8 |
| | MySQL | 8.0.33 |
| **工具库** | Hutool | 5.8.18 |
| | FastJSON | 2.0.25 |
| | Lombok | 1.18.30 |
| | Guava | 32.1.3-jre |
| | Commons IO | 2.11.0 |
| **API 文档** | Swagger | 2.9.2 |
| **构建工具** | Maven | 3.6+ |
| **JDK** | Java | 8+ |

## 📁 项目结构

```
kewen-framework/
├── .gitee/                             # Gitee 相关配置
│   ├── ISSUE_TEMPLATE.zh-CN.md         # Issue 模板
│   └── PULL_REQUEST_TEMPLATE.zh-CN.md  # PR 模板
│
├── basic/                              # 基础模块
│   └── src/main/java/com/kewen/framework/basic/
│       ├── exception/                  # 异常处理（BizException, BackendException等）
│       ├── filter/                     # 过滤器（EarlyRequestFilter, TraceRequestFilter等）
│       ├── logger/                     # 日志管理（请求日志、追踪日志）
│       ├── model/                      # 通用模型（PageReq, PageResult, Result等）
│       └── utils/                      # 工具类（BeanUtil, TreeUtil, FileUtils等）
│
├── basic-support/                      # 基础支持模块
│   └── src/main/java/com/kewen/framework/basic/support/
│       ├── log/                        # 日志持久化支持
│       ├── message/                    # 消息推送支持（方糖等）
│       ├── mp/                         # MyBatis-Plus 支持
│       ├── utils/                      # 工具支持
│       └── BasicSupportEndpoint.java   # 基础支持端点
│
├── boot/                               # Spring Boot Starter 模块
│   ├── basic-spring-boot-starter/      # 基础功能 Starter
│   │   └── src/main/java/com/kewen/framework/boot/basic/
│   │       ├── config/                 # 自动配置类（Async, MyBatis-Plus, Filter等）
│   │       ├── context/                # 上下文管理
│   │       ├── handler/                # 处理器
│   │       ├── p6spy/                  # SQL 日志监控
│   │       └── properties/             # 配置属性
│   │
│   ├── storage-spring-boot-starter/    # 文件存储 Starter
│   ├── tenant-spring-boot-starter/     # 多租户 Starter
│   └── magic-spring-boot-starter/      # Magic API Starter
│
├── qy-auth/                            # 权限认证模块
│   ├── auth-core/                      # 权限核心（注解、模型、实体）
│   │   └── src/main/java/com/kewen/framework/auth/core/
│   │       ├── annotation/             # 权限注解（@AuthMenu, @AuthDataRange等）
│   │       ├── data/                   # 数据权限相关
│   │       ├── entity/                 # 基础实体（BaseAuth等）
│   │       ├── exception/              # 权限异常
│   │       ├── menu/                   # 菜单管理
│   │       └── util/                   # 权限工具类
│   │
│   ├── auth-core-spring-boot-starter/  # 权限核心 Starter
│   ├── auth-rbac/                      # RBAC 权限实现
│   │   └── src/main/java/com/kewen/framework/auth/rabc/
│   │       ├── composite/              # 复合用户对象
│   │       ├── controller/             # RBAC 控制器（用户、角色、部门等）
│   │       ├── extension/              # 扩展服务
│   │       ├── model/                  # RBAC 模型
│   │       ├── mp/                     # MyBatis-Plus 实体和Mapper
│   │       └── utils/                  # RBAC 工具类
│   │
│   ├── auth-rbac-spring-boot-starter/  # RBAC Starter
│   ├── auth-spring-boot-starter/       # 安全认证 Starter
│   │   └── src/main/java/com/kewen/framework/auth/security/
│   │       ├── annotation/             # 安全注解
│   │       ├── config/                 # 安全配置（SecurityConfig, SessionConfig等）
│   │       ├── exception/              # 安全异常处理
│   │       ├── extension/              # Spring Security 扩展
│   │       ├── filter/                 # 认证过滤器
│   │       ├── handler/                # 认证处理器
│   │       ├── model/                  # 安全模型
│   │       ├── password/               # 密码处理
│   │       ├── properties/             # 安全配置属性
│   │       ├── response/               # 响应处理
│   │       └── service/                # 用户DetailsService等
│   │
│   └── relation/                       # 权限相关资源
│       ├── properties/                 # 配置文件
│       └── sql/                        # 权限数据库脚本
│
├── qy-idaas/                           # IDaaS 统一认证模块
│   ├── idaas-authentications/          # SAML/OIDC 等认证集成
│   │   └── src/main/java/com/kewen/framework/idaas/
│   │       ├── oauth2/                 # OAuth2/OIDC 集成
│   │       └── saml/                   # SAML 协议集成
│   └── README.md                       # IDaaS 使用说明
│
├── sample/                             # 示例项目
│   ├── auth-boot-sample/               # 权限认证示例（会议室管理）
│   │   ├── relation/                   # 示例相关资源
│   │   ├── src/                        # 示例源码
│   │   └── README.md                   # 示例说明
│   ├── basic-boot-sample/              # 基础功能示例
│   ├── storage-boot-sample/            # 文件存储示例
│   ├── tenant-boot-sample/             # 多租户示例
│   └── idaas-sp-boot-sample/           # IDaaS SAML 示例
│
├── other/                              # 其他工具
│   └── code-generator/                 # 代码生成器（MyBatis-Plus代码生成）
│
├── docs/                               # 文档和脚本
│   ├── sql/                            # 数据库初始化脚本
│   │   ├── storage.sql                 # 文件存储模块表
│   │   └── sys_request_log.sql         # 请求日志表
│   ├── 过程文件/                        # 开发过程文档
│   │   └── oidc接入踩坑记录.md
│   └── application.yml                 # 配置示例
│
├── relation/                           # 项目根目录资源
│   └── auth.sql                        # 权限系统数据库脚本
│
├── logs/                               # 日志目录
├── .gitignore                          # Git 忽略配置
├── LICENSE                             # MIT 许可证
├── README.md                           # 项目主文档
├── README_Auth.md                      # 权限系统详细文档
├── application.yml                     # 根配置示例
└── clean-sensitive-files.sh            # 清理敏感文件脚本
```

## 🚀 快速开始

### 环境要求

- JDK 8 或更高版本
- Maven 3.6 或更高版本
- MySQL 5.7 或更高版本（推荐 8.0）

### 1. 安装框架到本地仓库

```bash
git clone https://gitee.com/LiuKewenSc/kewen-framework.git
cd kewen-framework
mvn clean install
```

### 2. 创建数据库

执行数据库初始化脚本：

```bash
# 权限系统表（位于 relation 目录）
mysql -u root -p your_database < relation/auth.sql

# 存储模块表（可选，位于 docs/sql 目录）
mysql -u root -p your_database < docs/sql/storage.sql

# 请求日志表（可选，位于 docs/sql 目录）
mysql -u root -p your_database < docs/sql/sys_request_log.sql
```

### 3. 在你的项目中引入依赖

在你的项目 `pom.xml` 中添加依赖：

```xml
<parent>
    <groupId>com.kewen.framework</groupId>
    <artifactId>kewen-framework</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>

<dependencies>
    <!-- 权限认证模块 -->
    <dependency>
        <groupId>com.kewen.framework.auth</groupId>
        <artifactId>auth-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- RBAC 权限实现 -->
    <dependency>
        <groupId>com.kewen.framework.auth</groupId>
        <artifactId>auth-rbac-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
```

### 4. 配置数据库连接

在 `application.yml` 中配置数据库：

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 5. 启动应用

创建 Spring Boot 启动类：

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

启动后，使用默认账号密码登录：**admin/123456**

## 🔐 权限系统详解

### 核心概念

#### 菜单权限
访问某个菜单或 API 接口需要的权限。通过对菜单的控制完成粗粒度的鉴权，达到可见/不可见的效果。

#### 数据权限
某条数据对应的权限。对于每一条数据，都应该有对应的数据权限来控制，有权限的可以执行操作，没有权限的在查询列表中不可见。

#### 数据权限-操作类型
数据的权限需要进一步划分，一条数据对应不同的操作应该有不同的权限。例如：会议室可以编辑，也可以预约，这两个操作的基础数据都是会议室，但编辑应由管理员执行，而预约应该大部分人都可以执行。

### 核心注解

框架提供 4 个核心注解来实现权限控制：

#### 1. `@AuthMenu` - 菜单权限注解

判断登录人是否能访问注解对应的 API 接口。

```java
@RestController
@RequestMapping("/api/meeting")
public class MeetingRoomController {
    
    @AuthMenu(name = "创建会议室")
    @PostMapping("/add")
    public Result add(@RequestBody MeetingRoom room) {
        meetingRoomService.save(room);
        return Result.success();
    }
}
```

**参数说明：**
- `name`：API 菜单名称，在数据库中作为描述使用

**使用方式：**
- 在 Controller 方法上使用：该方法需要菜单权限
- 在 Controller 类上使用：该类所有带 `@RequestMapping` 的方法都需要菜单权限

#### 2. `@AuthDataRange` - 数据范围查询注解

在查询数据列表时自动筛选登录人可见的数据，不可见的数据不展示。

```java
@RestController
@RequestMapping("/api/meeting")
public class MeetingRoomController {
    
    @AuthDataRange(businessFunction = "meeting_room")
    @GetMapping("/list")
    public Result list() {
        List<MeetingRoom> list = meetingRoomService.list();
        return Result.success(list);
    }
}
```

**参数说明：**
- `businessFunction`：业务功能标识（必填），用于区分权限所属
- `table`：表名，多表联查时指定关联表
- `tableAlias`：表别名，多表联查有相同表时指定
- `dataIdColumn`：业务主键字段名，默认 "id"
- `operate`：操作类型，默认 "unified"
- `matchMethod`：条件匹配方式（IN/EXISTS），默认 IN

**支持的 SQL 类型：**
- IN 子查询
- EXISTS 子查询
- WITH AS 子查询
- JOIN 查询

#### 3. `@AuthDataOperation` - 数据操作权限注解

校验是否对单条数据有操作权限，避免通过接口进行越权攻击。

```java
@RestController
@RequestMapping("/api/meeting")
public class MeetingRoomController {
    
    @AuthDataOperation(businessFunction = "meeting_room", operate = "edit_info")
    @PostMapping("/update")
    public Result update(@RequestBody MeetingRoom room) {
        meetingRoomService.updateById(room);
        return Result.success();
    }
}
```

**参数说明：**
- `businessFunction`：业务功能标识（必填）
- `operate`：操作类型，默认 "unified"

#### 4. `@AuthDataAuthEdit` - 数据权限编辑注解

直接将请求中的权限编辑到权限表中，依赖菜单权限校验。

```java
@RestController
@RequestMapping("/api/meeting")
public class MeetingRoomController {
    
    @AuthMenu(name = "创建会议室")
    @AuthDataAuthEdit(businessFunction = "meeting_room", operate = "edit_info", before = false)
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result add(@RequestBody MeetingRoomAddReq req) {
        meetingRoomService.save(req);
        return Result.success();
    }
}
```

**参数说明：**
- `businessFunction`：业务功能标识（必填）
- `operate`：操作类型，默认 "unified"
- `before`：在业务逻辑之前执行还是之后执行，默认 true

### 核心调用类

#### AnnotationAuthHandler<ID>

注解核心控制器，注解对应的所有实现由此 Handler 完成。泛型 ID 是指业务权限中的 data_id 类型（String/Integer/Long）。

#### AuthDataAdaptor<ID>

业务调用适配器，可以不使用注解 `@AuthDataAuthEdit` 来编辑权限，更加灵活。

```java
@Autowired
private AuthDataAdaptor<Long> authDataAdaptor;

public void editDataAuths(Long dataId) {
    authDataAdaptor.editDataAuths(
        "meeting_room", 
        dataId, 
        "edit_info", 
        authObject
    );
}
```

## 📦 模块说明

### 1. basic - 基础模块

提供通用的工具类和基础功能：

- **异常处理**：统一的异常处理机制
- **日志管理**：请求日志、追踪日志
- **通用模型**：PageReq、PageResult、Result 等
- **工具类**：BeanUtil、TreeUtil、FileUtils 等

### 2. boot - Spring Boot Starter 模块

提供即插即用的功能模块：

#### basic-spring-boot-starter
基础功能自动配置，包括日志、请求跟踪等。

#### storage-spring-boot-starter
文件存储模块，支持七牛云等平台。

```yaml
kewen:
  storage:
    qiniu:
      access-key: your-access-key
      secret-key: your-secret-key
      bucket: your-bucket
```

#### tenant-spring-boot-starter
多租户支持模块，实现数据隔离。

```yaml
kewen:
  tenant:
    open: true
```

#### magic-spring-boot-starter
Magic API 集成模块。

### 3. qy-auth - 权限认证模块

完整的 RBAC 权限管理系统：

#### auth-core
权限核心模块，包含注解定义、权限模型等。

#### auth-rbac
RBAC 权限实现，包含用户、角色、部门的管理逻辑。

#### auth-spring-boot-starter
安全认证 Starter，包含 Spring Security 配置、登录过滤器等。

#### auth-rbac-spring-boot-starter
RBAC 快速配置 Starter。

### 4. qy-idaas - IDaaS 统一认证模块

支持 SAML、OIDC 等标准认证协议的企业统一认证。

### 5. sample - 示例项目

提供完整的使用示例：

- **auth-boot-sample**：权限认证完整示例（会议室管理案例）
- **basic-boot-sample**：基础功能示例
- **storage-boot-sample**：文件存储示例
- **tenant-boot-sample**：多租户示例
- **idaas-sp-boot-sample**：IDaaS SAML 认证示例

## ⚙️ 配置指南

### 基础配置

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kewen_framework?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 权限模块配置

```yaml
kewen:
  auth:
    auth-data-table:
      table-name: sys_auth_data
    cache-auth: false
```

### 安全配置

```yaml
kewen:
  security:
    login:
      login-url: /login                 # 登录地址
      current-user-url: /currentUser    # 当前用户接口
      username-parameter: username      # 用户名参数
      password-parameter: password      # 密码参数
      token-parameter: Authorization    # Token 请求头
    session:
      maximum-sessions: 1               # 最大 session 数量
      max-sessions-prevents-login: true # 是否阻止新登录
    remember-me:
      enabled: true                     # 是否开启记住我
      remember-parameter: remember-me   # 记住我参数
      validity-seconds: 2592000         # 记住时间（秒），默认30天
```

### 请求日志配置

```yaml
kewen:
  request:
    persistent:
      database: true   # 是否持久化到数据库
    message:
      fang-tang: true  # 是否发送方糖消息
```

### 消息通知配置

```yaml
kewen:
  message:
    fang-tang:
      key: ''           # 方糖推送 Key
      domain: https://sctapi.ftqq.com
```

### 多租户配置

```yaml
kewen:
  tenant:
    open: false  # 是否开启多租户
```

## 💡 使用示例

### 示例：会议室管理系统

以会议室管理为例，演示完整的权限使用流程。

#### 1. 创建会议室

```java
@RestController
@RequestMapping("/api/meeting")
public class MeetingRoomController {
    
    @AuthMenu(name = "创建会议室")
    @AuthDataAuthEdit(businessFunction = "meeting_room", operate = "edit_info", before = false)
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result add(@RequestBody MeetingRoomAddReq req) {
        meetingRoomService.save(req);
        return Result.success();
    }
}
```

**请求示例：**

```json
{
  "name": "1103大会议室",
  "userCount": 32,
  "place": "大门入口右转1103号大会议室",
  "remark": "全景落地窗，尊享位置",
  "isVideo": 1,
  "isProjector": 1,
  "isPhone": 1,
  "authObject": {
    "roles": [
      {
        "id": 1,
        "name": "超级管理员"
      },
      {
        "id": 4,
        "name": "会议室管理员"
      }
    ]
  }
}
```

#### 2. 查询可编辑的会议室列表

```java
@RestController
@RequestMapping("/api/meeting")
public class MeetingRoomController {
    
    @AuthDataRange(businessFunction = "meeting_room", operate = "edit_info")
    @GetMapping("/editList")
    public Result editList() {
        List<MeetingRoom> list = meetingRoomService.list();
        return Result.success(list);
    }
}
```

#### 3. 编辑会议室

```java
@RestController
@RequestMapping("/api/meeting")
public class MeetingRoomController {
    
    @AuthDataOperation(businessFunction = "meeting_room", operate = "edit_info")
    @AuthDataAuthEdit(businessFunction = "meeting_room", operate = "appointment")
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public Result update(@RequestBody MeetingRoomUpdateReq req) {
        meetingRoomService.updateById(req);
        return Result.success();
    }
}
```

#### 4. 预约会议室

```java
@RestController
@RequestMapping("/api/meeting")
public class MeetingRoomController {
    
    @AuthDataOperation(businessFunction = "meeting_room", operate = "appointment")
    @PostMapping("/appointment")
    public Result appointment(@RequestBody MeetingRoomAppointmentReq req) {
        meetingRoomService.appointment(req);
        return Result.success();
    }
}
```

### 权限粒度说明

- **数据创建、删除**：由菜单权限控制
- **数据编辑**：由数据权限的操作类型控制（如 edit_info）
- **数据使用**：由数据权限的另一操作类型控制（如 appointment）
- **主权限管理**：创建后交于其他人管理的最大权限

## ❓ 常见问题

### 1. 如何自定义异常返回格式？

实现 `ResponseBodyResultResolver` 接口：

```java
@Component
public class CustomResponseBodyResultResolver implements ResponseBodyResultResolver {
    @Override
    public Object resolver(HttpServletRequest request, 
                          HttpServletResponse response, 
                          Object data) {
        // 转换成你项目的统一返回格式
        return Result.success(data);
    }
}
```

### 2. 如何处理认证和授权异常？

使用 Spring MVC 的异常处理：

```java
@RestControllerAdvice
public class ExceptionAdviceHandler {
    
    @ExceptionHandler(AccessDeniedException.class)
    public Result accessDeniedException(AccessDeniedException e) {
        return Result.failed(403, "没有访问权限");
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public Result authenticationException(AuthenticationException e) {
        return Result.failed(401, "认证失败");
    }
}
```

### 3. 如何扩展权限维度？

如果需要添加岗位等新维度：

1. 创建岗位类继承 `AbstractIdNameFlagAuthEntity`
2. 创建权限集合类继承 `SimpleAuthObject`
3. 实现 `SysUserComposite` 加载用户权限
4. 在编辑权限入参实体中加入新的权限对象

### 4. 白名单接口如何配置？

使用 `@SecurityIgnore` 注解或配置文件：

```java
@SecurityIgnore
@GetMapping("/public/info")
public Result publicInfo() {
    return Result.success("公开信息");
}
```

### 5. 启动后如何配置菜单权限？

应用启动时会自动将带 `@AuthMenu` 注解的 API 注册到数据库。你可以在 `sys_menu_api` 表中查看，然后在 `sys_auth_menu` 表中配置角色与菜单的映射关系。

## 🤝 参与贡献

我们非常欢迎各种形式的贡献：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📝 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📚 相关资源

- [详细权限文档](README_Auth.md)
- [示例项目](sample/)
- [数据库脚本](relation/auth.sql, docs/sql/)
- [前端模板](https://gitee.com/LiuKewenSc/kewen-web-admin)

## 📧 联系方式

- 作者：kewen
- Gitee：https://gitee.com/LiuKewenSc/kewen-framework

---

如果这个项目对你有帮助，请给一个 ⭐ Star 支持一下！
