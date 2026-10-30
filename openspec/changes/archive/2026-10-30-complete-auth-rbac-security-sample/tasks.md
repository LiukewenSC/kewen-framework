# Tasks

## 1. 拆除会覆盖 starter 的复制件

- [x] 1.1 删除 `SampleInMemorySecurityUserDetailsService`、`AuthBootSampleCurrentUserContextHolder`、`SampleCurrentUserController` 三个文件；用 `grep -rn "SampleInMemorySecurityUserDetailsService\|AuthBootSampleCurrentUserContextHolder\|/api/me" sample/auth-rbac-security-boot-sample/src` 确认无残留引用，并运行 `mvn -o -f sample/pom.xml -pl auth-rbac-security-boot-sample -DskipTests compile` 编译通过
- [x] 1.2 清理 `AuthRbacSecurityBootSampleConfig`：移除 `PasswordEncoder` 与 holder 两个 Bean 及其 import，保留分页插件；确认该示例内不再出现 `implements AuthCurrentUserContextHolder` 或 `implements SecurityUserDetailsService`，编译通过
- [x] 1.3 调整 `pom.xml`：补 `spring-boot-starter-validation`，配置 `spring-boot-maven-plugin` 的 `mainClass` 与 `repackage`；运行 `mvn -o -f sample/pom.xml -pl auth-rbac-security-boot-sample -DskipTests package`，确认 `target/auth-rbac-security-boot-sample-0.0.1-exec.jar` 生成

## 2. 数据、资源配置与登录链

- [x] 2.1 新增 `sql/schema.sql`，只建示例业务表 `security_sample_business`（`DROP TABLE IF EXISTS` + `CREATE TABLE`，列与 MySQL/H2 双向兼容）；确认文件中不出现任何 `CREATE TABLE sys_*`
- [x] 2.2 新增 `sql/seed-data.sql`，全部用 `INSERT ... SELECT ... WHERE NOT EXISTS` 守卫：1 个角色、部门树 `1 -> 2 -> 3` 与独立根 `4`、4 个账号（`admin`/`admin2`/`subordinate`/`outsider`，密码 `{bcrypt}` 明文 `123456`）、用户-部门与用户-角色、3 行业务数据、`testrange`/`testedit` 权限槽位、`/test`、`/test/checkMenu`（`auth_type=2`）、`/test/checkMenuNone`（`auth_type=1`、无授权）、`/deptInherit`、`/deptInherit/rooms`（`auth_type=1`、挂 `RELATIONDEPT__2`）的菜单行与授权行；运行 `bash scripts/check-sql-compat.sh`，两遍均通过且行数不翻倍
- [x] 2.3 在子 POM 重写 `<resources>`：同时挂载 `src/main/resources`、`../../relation/auth-rbac`（targetPath `sql`）与自身 `sql/`；`mvn package` 后确认 `target/classes/sql/auth-rbac.sql` 与 `target/classes/sql/seed-data.sql` 同时存在
- [x] 2.4 重写 `application.yml`：应用名、端口 8083、公共段 `spring.session.store-type=jdbc`、`cu.auth.permit-url: /`、`cu.security.current-user-url: /currentUser`；`h2` 段配置数据源、`spring.session.jdbc.initialize-schema=embedded`、`spring.sql.init.mode=always`（schema 先于 data）、UTF-8；`mysql` 段只连库、不执行任何脚本；确认 H2 相关配置全部位于 `h2` profile 段内
- [x] 2.5 启动应用并验证登录链：`java -jar` 从模块目录启动，`POST /login`（`Content-Type: application/json`）用 `admin/123456` 返回 `success:true`；`GET /currentUser` 返回 `username=admin` 且权限集合含 `ROLE__1`；用 `admin2/123456` 重复一次，权限集合不含 `ROLE__1`

## 3. RBAC 判权演示端点

- [x] 3.1 新增 `security_sample_business` 的实体、Mapper 与 Service，Mapper 查询方法标注 `@AuthDataRange(businessFunction="testrange")`（列表、分页、计数各一条）；启动日志无数据范围声明校验错误，`admin` 调 `/test/dataRange` 返回 1 行、`admin2` 返回 0 行
- [x] 3.2 新增适配后的 `TestAuthAnnotationController`：`/test/dataRange` 与 `/test/pageDataRange` 去掉原示例的 `size()==1`、非空断言并统一返回 `Result`；`/test/dataEdit` 使用 `@AuthDataOperation(businessFunction="testedit")` 且方法体不写数据；`/test/checkMenu`、`/test/checkMenuNone` 带 `@AuthMenu` 与 `@AuthMenuDoc`；按矩阵验证：`admin` 的 `checkMenu` 200、`checkMenuNone` 403，`admin2` 两者皆 403，`dataEdit` 仅 `admin` 200
- [x] 3.3 新增 `DeptInheritDemoController`，类级 `@AuthMenu`，路径 `/deptInherit/rooms`；验证 `subordinate` 200，`admin`/`admin2`/`outsider` 均为 403
- [x] 3.4 启动后核对菜单注册结果：等待“init menu auth”线程结束，日志无“初始化菜单权限脚本异常”；直接查库确认 `/deptInherit/rooms` 的 `auth_type=1` 且授权为 `RELATIONDEPT__2`，`/test/checkMenu` 的 `auth_type=2` 且父路径为 `/test`

## 4. 登录页与验证台

- [x] 4.1 改造 `login.html` / `login.js`：账号提示改为四个演示账号与统一密码，登录态查询从 `/api/me` 改为 `/currentUser`，权限展示从对象数组映射为权限串；手工验证登录成功、刷新后仍为已登录、退出后回到表单
- [x] 4.2 重写 `index.html` / `index.js` 为只读验证台：身份切换（真实 `POST /logout` + `POST /login`）、按用户名内置真值表、逐行展示期望/实际/结果、“测试当前身份”一键重跑并汇总；四个身份逐一验证所有行均为通过
- [x] 4.3 补齐验证台样式（复用 `login.css` 或页面内样式），确认 1280 宽与窄屏下表格可读；确认页面只发起 GET 与 `dataEdit` 这一条无副作用 POST，不写任何业务数据
- [x] 4.4 `grep -rn "/api/me" sample/auth-rbac-security-boot-sample/src/main/resources/static` 无结果；确认验证台与登录页都只以响应体的 `success`/`code` 判断结果

## 5. 可选单点登录配置

- [x] 5.1 新增 `application-sso.yml`：OAuth2 手动端点、OIDC、SAML2 三组本地 IDP 配置，`client-secret` 用占位值并在注释指向 `login-boot-sample` 的同名客户端；默认启动（不带 sso profile）登录页不出现任何单点登录按钮
- [x] 5.2 本地 IDP（9010）在线时以 `--spring.profiles.active=h2,sso` 启动，确认登录页出现三个入口；完成一次 OAuth2 登录后 `/currentUser` 的用户名与权限来自本地 RBAC 账号；IDP 不在线时记录为未验证并写入 README 的覆盖边界

## 6. 验证脚本

- [x] 6.1 新增 `verify.sh`：按设计文档的矩阵对四个账号逐一登录（独立 cookie jar）、断言 `code`/`total`/用户名，输出 `PASS/FAIL` 与汇总，任一失败以非零退出；应用启动后运行 `bash verify.sh`，全部通过
- [x] 6.2 验证脚本不改数据、不依赖 `jq`、BASE 地址可被环境变量覆盖；临时把 `BASE` 指向未启动端口时脚本以非零退出并给出可读提示

## 7. 示例 README

- [x] 7.1 新增示例 `README.md`：目的与验证范围、四个账号与角色/部门归属、身份 × 端点真值表、启动命令（`package` + `java -jar`，端口 8083）、接口清单、SSO 可选开启方式、MySQL 手工导入顺序、覆盖边界（会议室业务 / SSO 联调 / H2 console 的归属）
- [x] 7.2 按 README 的启动与 curl 命令逐条实跑，确认命令与输出一致；真值表与页面、`verify.sh` 三处逐行核对一致

## 8. 文档索引

- [x] 8.1 更新 `auth/auth-rbac-security-spring-boot-starter/README.md` 的「当前没有单独使用本 starter 的示例」，改为指向本示例；更新 `auth/README.md` 路径 A 的“想先跑起来”指向
- [x] 8.2 根 `README.md` 与 `README.en.md` 的示例清单各增加一行，目录树中的示例数量从 6 改为 7；检查新增相对链接均可打开

## 9. 集成验证

- [x] 9.1 从模块目录执行 `bash scripts/check-sql-compat.sh`（两遍全过）与 `mvn -o -DskipTests package`，确认 exec jar 生成且示例 SQL 在 jar 内
- [x] 9.2 从模块目录 `java -jar` 启动，跑 `bash verify.sh` 全通过；再执行一次登录/退出流程，确认重启后种子与判权结果不变
- [x] 9.3 浏览器验收：`http://localhost:8083/login.html` 登录、首页验证台四个身份全部通过、切换身份与退出正常、控制台无报错；`/api/me` 返回 404
- [x] 9.4 `git status` 复核只包含本变更预期文件（无 `target/`、`logs/`、`data/`）；运行 `openspec validate complete-auth-rbac-security-sample --strict` 通过
