# 1. 说明

此工程是[cu-framework-auth](https://gitee.com/LiuKewenSc/cu-framework-auth)框架的示例工程

API接口已经发布在**Apifox**上[cu-framework-auth框架接口](https://apifox.com/apidoc/shared-56ea7395-f230-45b6-b650-14d27d36ce28/api-183461098)，可以配合使用

通过会议室管理这个具体的案例演示描述了权限框架的使用

单独列出示例工程描述`cu-framework-auth`的实际场景，一方面解决开源许可的冲突，一方面更能反映出对框架的引用，更具有独立性

本文以会议室、会议室预约功能举例说明

# 2. 工程结构

```text
auth-rbac-boot-sample
├─src/main/java/com/cu/framework/auth/sample
│    ├─config                                   配置类
│    ├─controller                               会议室接口，含各类权限注解的演示
│    ├─model                                    请求/响应对象
│    ├─mp                                       MyBatis-Plus 生成的实体与 Mapper
│    ├─AuthBootSampleCurrentUserContextHolder   当前用户上下文
│    └─AuthRbacBootSampleApp                    启动类
└─src/main/resources
     ├─application.yml                          配置（h2 / mysql 两个 profile 段）
     ├─logback-spring.xml                       日志
     ├─saml                                     SAML 元数据
     └─spy.properties                           p6spy sql 打印配置
```

# 3. 初始化启动

默认使用 H2 内存库，不需要初始化数据库、也不需要改任何配置，直接启动即可。
要接自己的 MySQL，才需要下面这两步：先按 3.1 建表灌数据，再按 3.2 填好 `mysql` profile 的连接。

## 3.1. 初始化数据库

以下脚本在 `h2` profile 下由 `spring.sql.init` 自动执行，无需手动跑；手动执行只针对接自己 MySQL 的情况。

框架的初始化脚本统一放在仓库根目录的 `docs/sql/` 下，示例专用的建表和种子数据放在本模块的 `sql/` 下。
执行顺序是先建表、再灌数据：

| 脚本 | 内容 |
| --- | --- |
| `../../docs/sql/auth-rbac.sql` | 权限框架 10 张表的建表语句 + 查看用的视图（MySQL 与 H2 通用） |
| `../../docs/sql/basic-support.sql` | 文件存储与请求日志表，本示例可选 |
| `sql/schema.sql`（本模块） | 示例业务表建表（`testauth_annotation_business`、`meeting_room`） |
| `sql/seed-data.sql`（本模块） | 上述两表与权限框架 10 张表的种子数据 |

`../../docs/sql/auth-rbac.sql` 与 `sql/schema.sql` 必须先执行，`sql/seed-data.sql` 里的 INSERT 依赖这些表。

会话存储所需的表不在这里建：H2 由 `spring.session.jdbc.initialize-schema: embedded` 自动创建，
`mysql` profile 用 `never`，需要自行执行 spring-session jar 内的
`org/springframework/session/jdbc/schema-mysql.sql`。

默认创建admin用户，超级管理员角色，可以直接用admin/123456登录。同时包含了所有API菜单权限、路由菜单权限等。
默认创建一个会议室，可以管理和预约，可以直接使用。利用[kewen-web-admin](https://gitee.com/LiuKewenSc/kewen-web-admin)工程可以皮诶和直接在页面上直观的查看。

## 3.2. 配置

默认不配也能跑：`spring.profiles.active` 已是 `h2`，用内存库，建表和种子数据由 `h2` profile 段里的
`spring.sql.init` 自动执行。

接自己的 MySQL 时加 `--spring.profiles.active=mysql`，并把该 profile 段的占位值改掉：

```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

`mysql` profile 不执行任何建表脚本，需先按 3.1 手动建表并灌入种子数据。

SQL 打印看 `spy.properties`。当前 `mysql` profile 用的是官方 MySQL 驱动，不经过 p6spy；
要用 p6spy 抓 SQL，需自行把 URL 改成 `jdbc:p6spy:mysql://` 并把驱动换成
`com.p6spy.engine.spy.P6SpyDriver`（原先为绕开前缀问题写的 `KP6SpyDriver` 已删除）。

## 3.3. 启动

配置完成直接启动 `AuthRbacBootSampleApp` 类即可，端口 8081；命令行方式：

```bash
mvn -pl sample/auth-rbac-boot-sample spring-boot:run
```

启动后可以直接登录访问，默认账号密码 **admin/123456**

示例项目中包含了基本的注解使用，可以登录后调用相关接口验证。API 接口已经发布到 **Apifox** [cu-framework-auth框架接口](https://apifox.com/apidoc/shared-56ea7395-f230-45b6-b650-14d27d36ce28/api-183461098)，可以执行验证

也可以配合[kewen-web-admin](https://gitee.com/LiuKewenSc/kewen-web-admin)工程启动登录之后在页面上验证查看。
需要先下载nodejs（可以在官网下载，我下载的v18），配置好环境之后执行`npm install`安装依赖，然后执行 `npm run serve`

这里建议把前端工程启动起来验证，可以查看一个完整的流程，只不过前端的启动需要nodejs相关的前端知识。

# 4. 使用案例

我们需要的功能有创建会议室、删除会议室、查看会议室可编辑列表、编辑会议室、查看会议室可预约列表、预约会议室
我们将这些功能点进行分类，其中

- 创建会议室、删除会议室因为是数据有无的概念，应当由菜单权限控制，这是比编辑数据更高的权限要求
- 查看可编辑列表、编辑会议室是对会议室的基本信息的操作，其权限应当分为同一功能(meeting_room)下的一组操作类型(edit_info)
- 查看会议室可预约列表、预约会议室对应会议室的使用，其权限应当分为同一功能(meeting_room)下的另一组操作类型(appointment)

需要特别注意的是，编辑会议室可以编辑会议室的预约权限，这是合理的。

## 4.1. 创建会议室

创建会议室主要使用@AuthMenu注解校验是否拥有创建会议室的权限，由菜单API权限控制。
创建会议室完成后需要添加会议室的管理权限。生活中一般有两种方式来控制，
a. 一种方式是创建的人直接有权限处理，不需要每一个都分配管理人员，毕竟每条数据都分配一个权限会很麻烦，有的数据本身也不需要这么多维度的权限。
b. 另一种方式是每个会议室都会分配管理的人，因为会议室这种属于不经常变动的资产，需要有人来管理它。

我们这里使用第二种方案，即创建会议室时为会议室分配管理人员。

除此之外，也可以添加会议室预约的权限。这里就直接创建一个会议室，先不允许预约

```java
/**
 * 创建会议室
 * @param entity  需要实现接口 IdDataAuthEdit<ID> 
 * @return
 */
@PostMapping("/meetingRoomPrimary/add")
@Transactional(rollbackFor = Exception.class)
@AuthMenu
@AuthMenuDoc(name = "创建会议室")
@AuthDataAuthEdit(businessFunction = "meeting_room",operate = "edit_info",before = false)
public Result add(@RequestBody MeetingRoomAddReq entity){
   meetingRoomService.save(entity);
   return Result.success();
}
/**
 * 创建会议室,第二种写法，不依赖于注解
 * @param entity  需要实现接口 IdDataAuthEdit<ID> 
 * @return
 */
@PostMapping("/meetingRoomPrimary/add2")
@AuthMenu
@AuthMenuDoc(name = "创建会议室")
@Transactional(rollbackFor = Exception.class)
public Result add2(@RequestBody MeetingRoomAddReq entity){
   meetingRoomService.save(entity);
   //引入 com.cu.framework.auth.core.AuthDataAdaptor;
   authDataAdaptor.editDataAuths("meeting_room",entity.getDataId(),"edit_info",entity.getAuthObject());
   return Result.success();
}
```

**启动的时候会自动添加接口** `/meetingRoomPrimary;/meetingRoomPrimary/add;/meetingRoomPrimary/add2;`
数据库查看`sys_menu_api`可以看到以下新增的
|id|path|name|parent_id|auth_type|
|---|---|---|---|---|
|1823284778685722624|	/meetingRoomPrimary	|MeetingRoomPrimaryController	|0	|	1|
|1823284778685722625|	/meetingRoomPrimary/add	|MeetingRoomPrimaryController>创建会议室|	1823284778685722624		|2	|
|1823284778685722626|	/meetingRoomPrimary/add2	|MeetingRoomPrimaryController>创建会议室	|1823284778685722624	|	2	|

然后需要我们在数据库找到对应接口权限的地方添加权限映射，由于有上下级关系，我们只需要在`/meetingRoom`对应的数据**id=1823284778685722624**配置权限即可，在此class下都有权限访问

```sql
INSERT into sys_auth_menu(api_id,authority,description) VALUES (1823284778685722624,'ROLE_1','ROLE_超级管理员');
```

上述SQL把超级管理员角色加了进去，超级管理员就有权限访问了，其他角色我们也可以按照此添加即可，也可以在页面上添加。

然后启动执行创建会议室，就可以把数据添加进去了。

`/meetingRoom/add`或`/meetingRoom/add2`

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

## 4.2. 删除会议室

删除会议室同创建一致，注意菜单权限保持相同即可，（与创建会议均用父级class的权限就可以解决）
//todo 删除会议室需要删除相关权限，是不是考虑单独做一个删除注解比较好

`/meetingRoom/delete`

同样菜单API会添加

## 4.3. 编辑会议室

编辑会议室需要有会议室的管理权限，这里定义角色为 **会议室管理角色**

编辑会议室首先要校验`@AuthDataOperation`，判定是否有权限，然后再编辑会议室相关的信息及预约权限

```java
/**
 * 编辑会议室
 * @param entity
 * @return
 */
@PostMapping("/updateById")
@Transactional(rollbackFor = Exception.class)
@AuthDataOperation(businessFunction = "meeting_room",operate = "edit_info")
@AuthDataAuthEdit(businessFunction = "meeting_room",operate = "appointment")
public Result updateById(@RequestBody MeetingRoomUpdateReq entity){
   meetingRoomService.updateById(entity);
   return Result.success();
}
/**
 * 编辑会议室方式2，
 * 不依赖@AuthDataAuthEdit
 * @param entity
 * @return
 */
@PostMapping("/updateById")
@Transactional(rollbackFor = Exception.class)
@AuthDataOperation(businessFunction = "meeting_room",operate = "edit_info")
public Result updateById(@RequestBody MeetingRoomUpdateReq entity){
   meetingRoomService.updateById(entity);
   authDataService.editDataAuths("meeting_room",entity.getId(),"appointment",entity.getAuthObject());
   return Result.success();
}
```

编辑会议室除了编辑基本信息以外，还要编辑可预约权限，这里也可以使用两种方式进行
需要注意的是校验是校验数据的`edit_info`操作，而插入的是预约权限`appointment`

```json
{
  "id":1,
  "name": "1103大会议室",
  "userCount": 32,
  "place": "大门入口右转1103号大会议室",
  "remark": "全景落地窗，尊享位置 编辑之后",
  "isVideo": 1,
  "isProjector": 1,
  "isPhone": 1,
  "authObject": {
    "roles": [
      {
        "id": 4,
        "name": "会议室管理员"
      }
    ]
  }
}
```

预约权限改成了只有会议室管理员可以编辑，因此后续超级管理员调用预约的时候的时候就会报没有权限。
至于会议室编辑的权限，应当单独拎出来一个接口用创建会议室的权限来执行，这样权限才能统一

## 4.4. 会议室预约

```java
   /**
    * 预约会议室
    * @param req 会议室ID，其实还有其他的入参，比如时间段等，这里暂时不管
    * @return
    */
   @PostMapping(value = "/appointmentMeetingRoom")
   @AuthDataOperation(businessFunction = "meeting_room",operate = "appointment")
   public Result appointmentMeetingRoom(@RequestBody @Validated MeetingRoomAppointmentReq req) {
      String time = req.getTime();
      //预约会议室的逻辑
      log.info("预约成功{}", time);
      return Result.success(time);
   }
```

请求数据：

```json
{
  "id":1,
  "time":"2024-11-10"
}
```

现在，超级管理员已经没有办法预约会议室了，因为没有权限、（至于超级管理员应当有最大权限这回事则应该在角色层级里面处理，有超管角色就应当有其余所有角色）

我们在数据库的`sys_auth_data`中插入一条功能为会议室**meeting_room**ID为**1**的操作为**appointment**的数据，角色把超级管理员**ROLE_1**加进去

```sql
insert into sys_auth_data(business_function,data_id,operate,authority,description) 
VALUES ('meeting_room',1,'appointment','ROLE_1','ROLE_超级管理员');
```

再次访问接口，成功的预约上了。

以上就是一个完整的功能示例演示。主要需要注意的就是各接口需要的功能即操作类型不能搞混了。

## 4.5. 部门继承权限：授权挂在父部门，子孙部门生效

部门权限有两种标记，二者正交：`DEPT__<部门ID>` 覆盖**该部门自己的成员**，
`RELATIONDEPT__<部门ID>` 覆盖**该部门的严格子孙部门成员，不含本部门自己**。
后者的展开发生在装载用户权限的时候：用户的每个部门会实时向上查出严格上级链，
把链上每个部门都拼成一条 `RELATIONDEPT__` 串，所以调整部门层级不需要重刷任何人的授权数据。

这条用例的四要素：

| 要素 | 内容 |
| --- | --- |
| 声明位置 | `DeptInheritDemoController`（类上 `@AuthMenu` 批量管辖）；菜单行与授权行都预先写在 `sql/seed-data.sql` 里并给出固定 ID |
| 触发方式 | `curl -H 'X-Demo-User: <用户名>' http://localhost:8081/deptInherit/rooms` |
| 观察点 | 响应体的 `code`（权限拒绝不体现在 HTTP 状态码上） |
| 期望结果 | 见下表 |

种子数据里的部门层级是 `1 根部门 -> 2 总部 -> 3 研发部`，另有一棵独立的根 `4 外部合作部`；
菜单上只挂了一条 `RELATIONDEPT__2`（总部）：

| 身份（`X-Demo-User`） | 所属部门 | 与总部(2) 的关系 | 期望 `code` |
| --- | --- | --- | --- |
| `subordinate` | 研发部(3) | 总部的子部门 | `200` 放行 |
| `admin2` | 总部(2)、外部合作部(4) | 本人就在总部 | `403` 拒绝 |
| `outsider` | 外部合作部(4) | 无层级关系 | `403` 拒绝 |
| 不带该头 | 根部门(1)（缺省 admin） | 总部的上级 | `403` 拒绝 |

`admin2` 那行是关键对照：它隶属的部门之一正是被授权的总部，但继承标记不含本部门，
所以它进不来。要在同一个端点上放行它，得另外配一条 `DEPT__2`——
自动化用例 `DeptInheritanceSampleTest#grantingTheDeptsOwnFlagAdmitsItsOwnMember` 就是在运行时补上这一条、
验证同一个人随即被放行，以此证明判据走的是层级关系而不是写死的结果。

身份由请求头 `X-Demo-User` 声明，是因为本示例没有接入 Spring Security 的登录态，
`AuthBootSampleCurrentUserContextHolder` 直接按该用户名从 `sys_user` 体系实时装载权限；
不带该头时缺省仍是 `admin`。头部名与 auth-core 示例一致，为的是"换身份"在两个示例里是同一个动作。
这个口子只存在于示例，框架不读任何身份请求头。

自动化载体：`mvn -pl sample/auth-rbac-boot-sample test`，即上表四行各一条断言，
外加 `multiDeptUserLoadsOneAuthInfoCarryingBothDepts` 验证一个隶属多部门的用户装载出的是**一个**权限体、两个部门都在里面。

# 5. 权限粒度说明
- 数据创建、删除的权限应当由菜单权限控制，同时，需要有一个编辑接口专门编辑主权限（即创建之后交于其他人管理的最大权限，其他人应当可以管理除了主权限之外的信息），这样实现了管理分离
- 菜单权限拥有对接口的最大控制，没有接口权限不能访问任何数据。建议在高级接口才使用菜单权限控制，其余的数据的操作接口，可以设置为所有人均拥有权限（避免两种权限搞混）
- 对于只有单一控制的数据如日程（创建之后数据基本就形成了，没有再复杂的逻辑），因为数据本身就之归属于某个人或一组人，因此菜单权限保证数据的增删改查，数据权限就仅剩下数据对应的范围。
- 对于有多重复杂控制的数据如会议室（创建了之后还需要以此为中心执行其他的业务逻辑），因为数据本身固定，但会提供给其他，因此菜单建议控制添加、删除、修改主权限，其余的交予数据权限来控制。
