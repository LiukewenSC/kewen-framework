# 权限注解与权限编辑

本文面向使用 `auth-core` 注解的开发者，说明当前代码里实际存在的四个注解、它们各自的声明位置、启动期校验和已知限制，以及不使用注解时如何通过 `AuthDataAdaptor` 编辑权限。

当前代码没有 `@AuthDataAuthEdit`。旧文档里的这个注解已经删除，数据权限编辑统一走 `AuthDataAdaptor.editDataAuths(...)`；`AnnotationAuthHandler` 也不存在，数据侧扩展点是 `AuthDataHandler`，菜单侧是 `AuthMenuHandler`。

## 四个注解

| 注解 | 目标 | 作用 | 声明位置 |
| --- | --- | --- | --- |
| `@AuthMenu` | 方法、类、注解类型 | 声明该端点或该类全部端点受菜单权限管辖 | Controller 方法或类 |
| `@AuthMenuDoc` | 方法、类、注解类型 | 给菜单树提供显示文案，不参与鉴权 | 与 `@AuthMenu` 相同 |
| `@AuthDataRange` | 方法 | 给一条查询语句注入数据范围条件 | MyBatis Mapper 查询方法 |
| `@AuthDataOperation` | 方法 | 校验本次调用涉及的业务数据 ID 是否都有操作权限 | Controller / Service 的写方法 |

## @AuthMenu 与 @AuthMenuDoc

`@AuthMenu` 只声明“管不管辖”，没有属性；`@AuthMenuDoc.name()` 只负责菜单树标签。

```java
@RestController
@RequestMapping("/meetingRoom")
@AuthMenu
@AuthMenuDoc(name = "会议室信息管理")
public class MeetingRoomController {

    @GetMapping("/page")
    public Result<PageResult<MeetingRoomResult>> pageQuery(@Validated PageReq pageReq) {
        // ...
    }
}
```

**声明粒度**

- 标在方法上：只管辖该方法对应的端点。
- 标在类上：管辖该类全部端点，相当于一次批量声明。
- 方法和类都没有声明：直接放行，框架不会因为“未声明”而告警或失败。

**鉴权身份**

- 菜单行由请求路径定位，路径是 `RequestURI` 去掉 `contextPath` 后的应用内路径。
- 鉴权是精确匹配：数据库里必须是 `/meetingRoom/page` 这样的完整路径。
- 带路径变量的端点（如 `/x/{id}`）注册出来的是模式串，实际请求 `/x/1` 匹配不到，会被恒拒绝。当前示例 Controller 全部使用字面量路径。
- 同一路径的不同 HTTP 方法共用一条菜单行和同一份授权，`GET /x` 与 `POST /x` 不能分别授权。

**启动注册**

`MenuApiGeneratorProcessor` 在启动时扫描带 `@AuthMenu` 的端点，按路径生成缺失的菜单行；已经存在的行不会被覆写。类上有 `@RequestMapping` 时额外生成一条虚拟父菜单；父路径找不到时按根节点入库并输出 WARN。没有提供 `MenuApiService` 时跳过生成，只输出 WARN。

`@AuthMenuDoc` 缺失时，类级文案回落到类简单名，方法级文案回落到方法名。

**菜单更新限制**

`/menu/api/update` 只更新名称、备注、权限类型和父节点；请求路径不在可更新字段内，请求里带新路径也不会生效。要换路径，改代码里的 `@RequestMapping` 并同步数据库。

菜单行的权限类型决定校验方式：

- `OWNER`：检查该菜单行自己的权限配置。
- `PARENT`：沿父节点向上递归，直到找到有权限配置的祖先；追到根仍未找到则拒绝。

## @AuthDataRange

声明在 **MyBatis Mapper 接口的查询方法**上。注解标在哪个方法，权限条件就只注入该方法对应的那一条语句；同一调用链上的其他查询不受影响。

```java
@Mapper
public interface MeetingRoomMpMapper extends BaseMapper<MeetingRoom> {

    @AuthDataRange(businessFunction = "meeting_room", operate = "edit_info")
    @Select("select * from meeting_room order by id limit #{size} offset #{offset}")
    List<MeetingRoom> selectEditAblePage(@Param("size") Integer size, @Param("offset") Integer offset);
}
```

**参数**

| 参数 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `businessFunction` | — | 是 | 业务功能标识，与权限表的 `business_function` 对应 |
| `table` | `""` | 否 | 多表或子查询时指定权限条件挂到哪张表；为空时用语句主表 |
| `tableAlias` | `""` | 否 | 目标表有别名时指定别名，只有指定了 `table` 才生效 |
| `dataIdColumn` | `id` | 否 | 业务表的主键列名 |
| `operate` | `unified` | 否 | 操作类型，同一张表按不同操作过滤时各写一个查询方法 |
| `matchMethod` | `IN` | 否 | `IN` 或 `EXISTS`；按“小表驱动大表”选择 |

**注入机制**

- 拦截点在 MyBatis 的 `StatementHandler.prepare`，以映射语句 id（`接口全限定名.方法名`）识别声明。
- 当前登录人的权限集合在执行期写入声明模板的副本，不会把某个用户的范围留在缓存里。
- 权限集合为空时注入恒假条件 `1 = 0`，查询正常执行并返回零行。
- 只改写查询语句；`INSERT` / `UPDATE` / `DELETE` 不会被注入。
- 多表或子查询用 `table` / `tableAlias` 定位目标 SELECT；匹配不到目标表时抛异常，而不是返回未过滤结果。

**启动期校验**

以下情况会让应用启动失败：

- 方法解析不到映射语句（方法名与语句 id 不一致）。
- 语句不是 `SELECT`。
- 语句启用了二级缓存（缓存键在权限条件注入之前计算，两者不能共存）。
- 静态可得的 SQL 中找不到 `table` 声明的目标表。
- `@AuthDataRange` 出现在 Mapper 接口以外的方法上。

动态 SQL 或 Provider 生成的 SQL 无法在启动期安全渲染，目标表校验会跳过，改为执行期匹配不到表时抛异常。

**已知限制**

- MyBatis-Plus 从 `BaseMapper` 继承的 `selectList`、`page` 等方法没有声明挂载点，不受本注解管辖；需要过滤的查询要在自己的 Mapper 接口上显式写出来。
- 一条语句只绑定一个 `businessFunction` + `operate`；同一张表要按编辑、预约等不同操作过滤时，各写一个查询方法。
- 分页的列表与计数是两条语句，需要各声明一次，否则总数与行集的口径不一致。
- 没有接入 MyBatis（没有 `SqlSessionFactory`）时，启动校验器跳过扫描，`@AuthDataRange` 也不会生效。

## @AuthDataOperation

声明在业务写方法上（通常是 Controller 或 Service）。切面在方法执行前收集实参中所有 `DataIdCarrier` 交出的业务数据 ID，逐个判定，全部命中才放行；任一条无权限即整体拒绝，方法体不执行。

```java
@PostMapping("/updateById")
@Transactional(rollbackFor = Exception.class)
@AuthDataOperation(businessFunction = "meeting_room", operate = "edit_info")
public Result updateByIdMethod(@RequestBody MeetingRoomUpdateReq entity) {
    meetingRoomService.updateById(entity);
    return Result.success(entity);
}
```

请求体实现 `DataIdCarrier`：

```java
@Data
public class MeetingRoomUpdateReq implements DataIdCarrier<Long> {
    private Long id;

    @Override
    public Collection<Long> getDataIds() {
        return Collections.singletonList(id);
    }
}
```

**参数**

| 参数 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- |
| `businessFunction` | — | 是 | 业务功能标识 |
| `operate` | `unified` | 否 | 操作类型 |

**判定规则**

- 收集范围只到一层：实参本身、实参集合的元素、实参数组的元素；更深的嵌套由 `DataIdCarrier` 自己摊平。
- ID 去重后逐个判定；数值型与字符串型主键按字符串归一化，同一主键的不同类型形态视为同一条数据。
- 单次调用去重后最多 100 个 ID，超出直接失败，由调用方分批提交。
- 只按 `business_function` + `operate` 同时匹配的权限记录判定，不会回退到其他操作类型。
- 当前登录人的权限集合在内存中比对，数据 ID 作为查询参数，不拼进语句。

**失败语义**

- 实参没有承载对象、ID 集合为空、含空值、超过 100 条：用法错误，`AuthServiceException`，对外 `code=400`。
- 权限未命中：`AuthException`，对外 `code=403`。
- 两类失败由 `auth-core-spring-boot-starter` 的 `AuthExceptionHandler` 自动装配；使用方自己定义同类处理时让位。
- 方法体不执行；错误消息不输出框架内部类型信息。

**已知限制**

- 只拦截显式标注的方法；Service 内部自调用不经过 Spring AOP 代理，不会被判定。
- 需要菜单权限的接口应同时标 `@AuthMenu`；`@AuthDataOperation` 只做行级操作权限，不替代菜单鉴权。

## 不用注解：AuthDataAdaptor

`AuthDataAdaptor` 是命令式入口，适合在 Service 层直接编辑或读取某条数据的权限；它不依赖 `@AuthDataAuthEdit`。

```java
@Autowired
private AuthDataAdaptor authDataAdaptor;

// 把请求中的权限写回 meeting_room 的 appointment 槽位
authDataAdaptor.editDataAuths("meeting_room", entity.getId(), "appointment", entity.getAuthObject());

// 查询某条数据在 appointment 槽位已配置的权限，并填充到结果对象
authDataAdaptor.fillDataAuths("meeting_room", entity.getId(), "appointment", result.getAuthObject());

// 业务数据删除后清空它的权限行
authDataAdaptor.deleteBusinessFunctionAuthByDataId("meeting_room", entity.getId());
```

- `editDataAuths`：按传入的权限集合做增删，不处理事务；调用方自己保证事务。
- `fillDataAuths`：读取某条数据的权限并写入传入的 `IAuthObject`；调用方必须传入非空对象，传入 `null` 不会被静默忽略。
- `deleteBusinessFunctionAuthByDataId`：删除某条业务数据的所有权限行，业务删除数据后调用。
- 不要编辑当前接口自身的权限，否则可能出现“把自己编辑没”或把不应有的人加入；这类越权应由上一级菜单权限控制。

## 扩展点

| 接口 | 作用 | 默认实现 |
| --- | --- | --- |
| `AuthCurrentUserContextHolder` | 交出当前用户与权限集合 | 无；`auth-rbac-security` 提供基于 Spring Security 的实现 |
| `AuthMenuHandler` | 判断某路径是否有菜单权限 | `auth-rbac` 的 `RbacAnnotationAuthHandler` |
| `AuthDataHandler` | 数据操作判定、权限编辑、权限读取 | `auth-core` 的 `JdbcAuthDataHandler` |
| `MenuApiService` | 启动时保存菜单 API | 无；`auth-rbac` 的 `RbacMenuApiService` |

自定义实现通过声明同类型 Bean 覆盖默认实现；`auth-core` 的默认 `AuthDataHandler` 带 `@ConditionalOnMissingBean`，`auth-rbac` 的 `AuthMenuHandler` 也带同样条件。RBAC 模型层的扩展（自定义权限维度、替换用户装载、菜单缓存）见 [rbac-and-extension.md](rbac-and-extension.md)。

## 相关文档

- [auth 总览](../README.md)
- [RBAC 模型与扩展](rbac-and-extension.md)
- [auth-core-spring-boot-starter](../auth-core-spring-boot-starter/README.md)
- [auth-core 验证示例](../../sample/auth-core-boot-sample/README.md)
- [auth-rbac 会议室示例](../../sample/auth-rbac-boot-sample/README.md)
