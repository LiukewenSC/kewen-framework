# Tasks

## 1. 修正用户权限体的装载通路

- [x] 1.1 把 `../../../../auth` 里 `<collection property="users">` 改为映射单个 `user`（association），使 `UserAuthInfo.user` 真正被灌上值；验证：以 h2 profile 启动 `auth-rbac-boot-sample`，装载 `admin` 后 `getAuthInfo().getUser()` 非空且其 id 等于 `sys_user` 里该用户的 id
- [x] 1.2 让 `UserAuthInfo.listBaseAuth()` 在 `user == null` 时短路返回空集合（不再汇总 roles/depts）；验证：新增 JUnit 4 单测构造 `user` 缺失的 `UserAuthInfo`，断言返回空集合且不抛异常
- [x] 1.3 在 `RbacSecurityUserDetailsService` 与 `sample/auth-rbac-boot-sample` 的 `AuthBootSampleCurrentUserContextHolder` 两处各自对 `authInfo` 判空，缺失时给出空权限集合；验证：JUnit 4 单测覆盖"无凭证用户被装载为认证主体"，断言装载成功、权限集合为空、不抛 NPE
- [x] 1.4 确认一个隶属多个部门的用户经同一次 join 只聚合成一个权限体对象（design.md 风险首条标注的 MyBatis 行为，属推断未实测）；验证：为种子数据中某用户临时挂两个部门后装载，断言不抛 TooManyResults 且 `getDepts()` 含两个部门——此断言与 4.1 的部门树种子合并执行亦可。**实测结果：不成立**——根 resultMap 没有鉴别列时 MyBatis 会把嵌套集合的鉴别列并进父行键，多部门用户被拆成多个权限体并抛 TooManyResults；已在 resultMap 根节点补 `<id property="userId" column="user_id"/>` 并在 `UserAuthInfo` 上增加对应的所属用户 ID 字段，由 `DeptInheritanceSampleTest#multiDeptUserLoadsOneAuthInfoCarryingBothDepts` 钉住
- [x] 1.5 在 `../../../../auth` 把"用户权限体不反解、权限体缺失时得到空权限"这条对外结果补一句说明；验证：文档所述行为与 1.2/1.3 的断言一致，无多余承诺

## 2. 部门继承语义：父链不含自身

- [x] 2.1 把 `SysUserCompositeImpl.getDeptListByDeptId` 的展开起点从本部门改为直接上级，使返回的链路只含严格上级部门；验证：JUnit 4 单测对"根→父→子→孙"链路断言孙部门的链路为 [根, 父, 子]（不含孙自己），条数与顺序都钉住
- [x] 2.2 将 `SysUserComposite` 的该方法改名为 `listAncestorDepts`，同步三处：接口声明 `composite/SysUserComposite.java`、实现 `composite/impl/SysUserCompositeImpl.java`、唯一调用点 `composite/model/UserAuthInfo.java` 的 `fillUserAuth`（已核对全仓库仅此三处）；验证：`grep` 全仓库无 `getDeptListByDeptId` 残留、`mvn -q compile` 通过。**这条是可单独砍掉的一次性改名，不影响 2.1 与后续任务**
- [x] 2.3 给上级链的遍历加终止防护：自引用（父级等于自身）与互为父级都必须在有限步内结束；验证：JUnit 4 单测用构造的畸形层级数据断言装载正常完成、不出现超出层数上限的重复查询，且给出的权限集合确定
- [x] 2.4 把 `ParentDept.level` 的口径改为"距自身的层级，直接上级=1、隔级=2"，并在字段上只写能确证的一句注释（该值不参与序列化，也不参与任何权限判定）；验证：单测断言直接上级与隔级上级各自的 level 值；`grep` 确认除构造函数赋值外无读取点，注释没有编造用途
- [x] 2.5 让 `UserAuthInfo.fillUserAuth` 幂等：写入 `parents` 与 `deptRoles` 前先清空，重复调用不累积；验证：单测对同一 `UserAuthInfo` 连续调用两次，断言两次结果的两个集合大小一致
- [x] 2.6 补齐用户侧展开的串级断言：直接子部门命中、隔级子孙命中、本部门成员不命中、只隶属根部门者无可继承串、多部门任职取并集；验证：JUnit 4 单测按权限串集合逐条断言这五种身份，其中"本部门成员不命中"这一条必须能在去掉 2.1 的改动后失败（防止断言空转）

## 3. 权限串的标记规则与编解码往返

- [x] 3.1 为 `RELATIONDEPT` 补配置侧往返单测：串 → 配置权限对象 → 重新序列化的串与描述都与入库值一致；验证：新增 JUnit 4 单测断言往返一致，且 `relationDepts` 被正确归位（而不是落到 `depts`）
- [x] 3.2 把"标记必须是单段令牌"写成可执行断言：一条权限的位置数等于其标记按次级分隔符拆出的段数；验证：单测对 `ParentDept` 断言其权限串的 ID 段与描述段各只有一个值；同时确认 `flag()` 返回值不含次级分隔符
- [x] 3.3 让本轮新增的单测对 `DeptWithParents` 与 `Dept` 竞争同一标记这一既有缺陷免疫；验证：检查所有新增断言，其中不出现对 `getDepts()` 元素具体运行时类型的判定，只断言 id 与权限串
- [x] 3.4 清掉 `ParentDept` 里已不使用的 `AuthConstant` 导入，并按仓库约定补类注释（`@author kewen` + `@since` 取该文件的 git 创建日期）；验证：`mvn -q compile` 通过，且 `ParentDept.flag()` 的取值理由（旧值 `DEPT_RELATION` 会被当成两个位置）以一行注释落在 `flag()` 上，不写推测

## 4. auth-rbac-boot-sample 的端到端用例

- [x] 4.1 把 `sample/auth-rbac-boot-sample/sql/seed-data.sql` 的 `sys_dept` 从两个平铺节点改成一条父子链（沿用该文件"带显式主键、非幂等、INSERT 不带列名"的既有形态，列顺序须与 `docs/sql/auth-rbac.sql` 一致），并补 `sys_user_dept` 让三个用户在"父部门 / 其子部门 / 与链无关的部门"各占一个位置；验证：以 h2 profile 启动该示例，查询能读到这条层级明确的部门链与三个归属
- [x] 4.2 给 `AuthBootSampleCurrentUserContextHolder` 加按请求头指定用户名的口子，头部名沿用 auth-core 示例已有的 `X-Demo-User`，缺省仍为 `admin`；验证：带与不带该头分别请求，装载出的用户名不同；并确认改动只在 sample 模块内，`grep` 框架模块无此头部名
- [x] 4.3 在示例里放一个受菜单管辖的演示端点，并在 `sys_auth_menu` 给它配一条指向父部门的 `RELATIONDEPT__` 权限行；验证：`sys_menu_api` 与 `sys_auth_menu` 都有对应行，且该端点的权限类型为 OWNER
- [x] 4.4 以 MockMvc 落地三身份对照用例（父部门成员拒绝 / 子部门成员放行 / 无关部门成员拒绝），装置照 `sample/basic-boot-sample` 的 `AbstractBasicSampleWebTest` 形态；验证：`mvn test` 该模块通过，且把 4.3 的权限行改配到请求者自己的部门时"父部门成员"那一路会翻成放行（证明判据真的走的是层级关系而不是常量）
- [x] 4.5 在示例的说明处（该模块 README 或 `AuthRbacBootSampleApp` 附近的既有的说明位）写清这条用例的四要素：声明位置、触发方式、观察点、期望结果；验证：按文档所述请求头与端点能亲手复现 403/200/403

## 5. auth-core-boot-sample 的继承型标记用例

- [x] 5.1 在 `sample/auth-core-boot-sample` 自有的权限实体包内新增一个继承型标记实体，主键用整型，标记为单段令牌；验证：`grep` 确认该示例未引用 `auth-rbac` 的任何类或 `sys_*` 表，且 `pom.xml` 依赖未增加权限模型层
- [x] 5.2 让该串在示例的配置侧权限体上完成无损往返，并在充当用户权限体的对象上反解时得到**可识别的失败**而不是空集合；验证：新增自动化测试断言这两件事——往返串一致、反解当场抛错，后者 MUST NOT 被写成"返回空集合"
- [x] 5.3 用示例自己的表造出层级数据（不改用权限模型层的表），使该标记的判定只取决于权限串是否一致；验证：授予同一串的演示端点对持有该串的用户放行、对不持有的用户拒绝。**落地口径（已在 design.md D8 范围内）：示例不建部门树**——按树展开下级本来就是具体权限模型的职责，在一个专门证明"核心层不依赖任何权限模型"的示例里再种一棵树会把它的立论拆掉；示例只把自己 `demo_user_auth` 里的一条 `DEPTINHERIT__100` 当作既成权限，`/deptInherit/ledger` 只授予这一条串。已实测 `grep` 该示例的建表/种子/代码中不出现任何 `sys_*` 表名或 `auth.rbac` 类，业务主键仍全为整型，符合规格那条场景
- [x] 5.4 把该用例并入该示例既有的用例装置三处：`static/index.html` 的用例条目、`README.md` 的场景矩阵行、`verify.sh` 里对应的断言；验证：`bash sample/auth-core-boot-sample/verify.sh` 跑通且新增用例被逐条执行——三处缺一即视为未完成，因为该能力规格要求文档与脚本描述同一批用例

## 6. 跨组集成校验

- [x] 6.1 对改动过的 SQL 跑仓库既有的兼容性检查；验证：`bash scripts/check-sql-compat.sh` 通过（注意该检查按文件名排序执行，schema 必须排在 data 之前）
- [x] 6.2 全仓库构建与测试；验证：`mvn -q test` 通过，auth-rbac（JUnit 4）与 auth-core（JUnit 5）两侧新增用例都被实际执行而不是被跳过
- [x] 6.3 两个示例都以默认 profile 启动并跑通各自的验证入口；验证：`auth-rbac-boot-sample` 的三身份对照与 `auth-core-boot-sample` 的 `verify.sh` 在同一份代码上同时通过，且未连接任何本地 MySQL
- [x] 6.4 把三条对外后果落到归档说明材料里（实际落了四条，第四条是实现期新发现的装载缺陷与随之新增的 `UserAuthInfo.userId` 对外字段）；四条表述已写进本 change 的 `design.md` 末尾「归档说明必须写明的对外后果」一节，归档时直接抄，不留在对话里
