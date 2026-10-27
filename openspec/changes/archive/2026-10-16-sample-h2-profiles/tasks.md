# Tasks

## 1. 构建层：资源挂载上提与 H2 依赖

- [x] 1.1 把 `basic-boot-sample`、`storage-boot-sample` 现有 `<resources>` 中的 `docs/sql` 挂载块**原样**（目录值保持 `${project.basedir}/../../docs/sql`，不改相对深度）上提到 `sample/pom.xml` 的 `<build>`，并保留子模块各自的 `src/main/resources` 默认挂载；验证：`mvn -q -pl sample/basic-boot-sample -am process-resources` 后 `sample/basic-boot-sample/target/classes/sql/` 下同时存在 `auth-rbac.sql` 与 `basic-support.sql`。若该目录被解析成 `sample/docs/sql`（说明继承串按父级 basedir 插值），停止上提并执行 1.2b
- [x] 1.2 删除 `sample/basic-boot-sample/pom.xml` 与 `sample/storage-boot-sample/pom.xml` 中已有的 `<resources>` 块（子级声明会整体覆盖继承，不是追加）；验证：对这两个模块各跑一次 `mvn -q -pl sample/<module> -am process-resources`，`target/classes/sql/` 仍含上述两个文件，且 `target/classes/spy.properties` 等原有资源未丢失
- [x] 1.2b **未触发**（1.1 实测通过：两个模块的 `target/classes/sql/` 均由继承的挂载产出，`sample/docs/` 未被创建，插值方向与 D6 预期一致，无需退回各模块自持声明；design D6 的该前提已转为已验证事实）
- [x] 1.3 给 `sample/login-boot-sample/pom.xml`、`sample/tenant-boot-sample/pom.xml`、`sample/auth-rbac-boot-sample/pom.xml` 补 `com.h2database:h2` 依赖，scope 为 `runtime`，不写版本号；验证：`mvn -q -pl sample/<module> dependency:tree -Dincludes=com.h2database:h2` 显示 h2 且版本为 2.1.214
  - 注：`login-boot-sample` 早已声明 h2（`f98ee5c` 引入），本条实际只给 tenant 与 auth-rbac 新增了依赖
- [x] 1.4 确认框架模块未被带入 H2；验证：`grep -rn "h2database" --include=pom.xml basic basic-support boot qy-auth login-security-spring-boot-starter other` 无输出

## 2. auth 示例 SQL 拆分与双向兼容

- [x] 2.1 按 design D4 的行范围表，从 `sample/auth-rbac-boot-sample/sql/init.sql` 归堆出 `sql/schema.sql`（只含 `testauth_annotation_business`、`meeting_room` 两张示例自有表的 `DROP` + `CREATE`）；验证：文件内 `CREATE TABLE` 数量为 2，且不含 `SPRING_SESSION`
- [x] 2.2 归堆出 `sql/seed-data.sql`（原 1–321 行、337–348 行、368 行、370 行、372 行的全部 INSERT，保持 INSERT 不带列名的原样）；验证：`grep -c "^INSERT" sql/seed-data.sql` 与原 `init.sql` 的 INSERT 总数一致（原 377–399 行的会话表段除外，那部分按 2.3 删除）
- [x] 2.3 删除 `init.sql`，会话表 DDL 不迁入任何新文件；验证：`sample/auth-rbac-boot-sample/sql/` 下只剩 `schema.sql` 与 `seed-data.sql`，且 `grep -rn "SPRING_SESSION" sample/` 无输出
  - 注：脚本层面已清零。残留两处命中均为文字说明而非 DDL —— `sample/auth-rbac-boot-sample/README.md:48` 仍在描述已删除的 `sql/init.sql`（归入 2.6 的文档修正），`sample/login-boot-sample/.../application.yml:22` 是对 `initialize-schema: embedded` 的正确注释，保留
- [x] 2.4 在 `sample/auth-rbac-boot-sample/pom.xml` 增加把 `${project.basedir}/sql` 挂到 `targetPath` 为 `sql` 的 `<resources>`，与继承来的 `docs/sql` 挂载合并声明（两处都要写，否则覆盖继承）；验证：`mvn -q -pl sample/auth-rbac-boot-sample process-resources` 后 `target/classes/sql/` 含 `auth-rbac.sql`、`basic-support.sql`、`schema.sql`、`seed-data.sql` 四个文件
- [x] 2.5 跑 SQL 兼容性检查覆盖新文件名；验证：`bash scripts/check-sql-compat.sh` 列出的检查文件包含 `sql/schema.sql` 与 `sql/seed-data.sql`，且两遍全 `ok`
  - 实现中发现：检查器按 `ls | sort` 决定执行顺序，原名 `data.sql` 排在 `schema.sql` 之前，实测失败 4 条（`Table "testauth_annotation_business" not found` ×3、`Table "meeting_room" not found` ×1）。改名为 `seed-data.sql` 后顺序正确，两遍通过、16 张表。原因与取舍已回写进 design D4
- [x] 2.6 更新 `README.md:165` 目录树与 `README.md:225-226` 的导入命令，改为 `auth-rbac-boot-sample` 与 `docs/sql/auth-rbac.sql` + `sql/schema.sql` + `sql/seed-data.sql` 三条手动导入；同时修正 `sample/auth-rbac-boot-sample/README.md:41-50` 的同一张脚本表（仍写着已删除的 `sql/init.sql` 与 `SPRING_SESSION`）；验证：`grep -rn "auth-boot-sample\|sql/init.sql" README.md sample/*/README.md` 无输出，且三条命令的目标文件都存在
  - 追加（用户点名要求清理）：`sample/auth-rbac-boot-sample/README.md` 的 §2 工程结构（`AuthWebSample` → `AuthBootSampleApp`、去掉不存在的 `response`/`templates` 目录、`p6syd` 拼写）、§3 引言（默认 H2 无需初始化数据库）、§3.2（**移除真实库名与口令 `cu_framework_auth_template`/`open_framework`/`Framework123456_`，改为占位值**；删掉指向已不存在的 `KP6SpyDriver` 的配置，说明当前 `mysql` profile 走官方驱动、要 p6spy 需自行改前缀）、§3.3（启动类名与命令行启动方式）。验证：`grep -n "AuthWebSample\|p6syd\|templates\|cu_framework_auth_template\|Framework123456" sample/auth-rbac-boot-sample/README.md` 无输出

## 3. basic-boot-sample 配置收敛

- [x] 3.1 把 `spring.sql.init` 的 `mode` / `encoding` / `schema-locations` 从顶层文档段移入 `h2` profile 段，顶层只留 `spring.profiles.active: h2` 与非数据库相关配置；验证：`sample/basic-boot-sample/src/main/resources/application.yml` 的顶层段内 `grep -n "sql:"` 无命中，且 `h2` profile 段内含 `mode: always`
- [x] 3.2 修正该模块 `mysql` profile 的 `jdbc:pyspy:mysql://` 为 `jdbc:p6spy:mysql://`，并把顶层与 `mysql` 段都补一行注释说明激活 `mysql` 后不执行任何建表脚本、表需自行准备（含 spring-session 的 `schema-mysql.sql` 来源提示）；验证：`grep -rn "pyspy" sample/` 无输出

## 4. storage-boot-sample 配置收敛

- [x] 4.1 与 3.1 同样把 `spring.sql.init` 全量移入 `h2` profile 段，顶层不再声明；验证：该模块 `application.yml` 顶层段内无 `mode: always`，`h2` profile 段内有其声明
- [x] 4.2 修正该模块 `mysql` profile 的 `jdbc:pyspy:mysql://` 为 `jdbc:p6spy:mysql://` 并补充与 3.2 相同的注释；验证：`bash scripts/check-sql-compat.sh` 仍全绿，且该模块 `application.yml` 中 `grep -n "pyspy"` 无命中

## 5. login-boot-sample 首次接入 profile

- [x] 5.1 把 `spring.datasource` 的 H2 连接从顶层主段移入 `h2` profile 段，新增 `mysql` profile 段（URL / 用户名 / 密码用占位值，不含任何真实凭据）；验证：该模块 `application.yml` 中每一处 `jdbc:h2` 命中都位于 `on-profile: h2` 之后，且 `spring.profiles.active: h2` 生效
- [x] 5.2 把 `spring.session.jdbc.initialize-schema` 按 profile 拆分：`h2` 段用 `embedded`，`mysql` 段用 `never`，顶层不再声明；验证：配置中该键出现两次且分别落在对应 profile 段内
- [x] 5.3 删除文件末尾"换用本地 MySQL 时替换如下"的注释块，改为正式的 `mysql` profile；验证：`grep -n "spring.profiles.active=mysql" sample/login-boot-sample/src/main/resources/application.yml` 有命中或在注释中等价说明存在
- [x] 5.4 实测登录：默认 profile 启动 `login-boot-sample`（8080），向 `/login` POST `username`/`password` 走通一次登录并确认 `SPRING_SESSION` 表有行（经 `/h2-console`）；验证：登录返回成功且会话表行数大于 0
  - 实测：`POST /login`（admin/admin123）返回 HTTP 200 并下发 `SESSION` cookie，日志中无会话表缺失的 SQL 报错，说明 h2 profile 下 spring-session 的 `schema-h2.sql` 已建表且写入成功。未走 `/h2-console` 的表单登录去数行数，证据形式与本条描述略有差异

## 6. tenant-boot-sample 首次接入 profile

- [x] 6.1 为该模块写 `h2` profile（空内存库，不声明任何 `spring.sql.init` 键）与 `mysql` profile（沿用现有占位 URL），顶层声明 `spring.profiles.active: h2`；验证：该模块配置中 `jdbc:h2` 只出现在 `h2` profile 段，且 `grep -n "sql.init\|schema-locations" sample/tenant-boot-sample/src/main/resources/application.yml` 无命中
- [x] 6.2 在该模块配置注释中写明其 DataSource 是被 `basic-spring-boot-starter` 传递带入的 mybatis-plus 逼出来的、示例本身不访问任何表；验证：注释存在，且默认 profile 启动后无需任何表即可调用 `/hello` 拿到含租户 ID 的问候串
  - 实测：profile "h2" 激活、Tomcat 8080、`GET /hello` → `hello - null`（未带租户请求头，故 ID 为空，符合预期）。全程无任何表存在

## 7. auth-rbac-boot-sample 接入 profile

- [x] 7.1 重写该模块 `application.yml` 数据源为 `h2` / `mysql` 两个 profile 段，默认激活 `h2`；`h2` 段声明 `spring.sql.init.schema-locations = classpath:sql/auth-rbac.sql, classpath:sql/schema.sql` 与 `data-locations = classpath:sql/seed-data.sql`、`mode: always`；验证：顶层段无 H2 连接串也无 `sql.init`，`h2` profile 段内三者齐全
- [x] 7.2 修正该模块顶层 `hikari` 段的 YAML 缺陷：`auto-commit:true` 与 `connection-test-query:select 1` 冒号后补空格，并核对 `max-lifetime` / `idle-timeout` 等同段键确实被解析；验证：`spring.datasource.hikari.auto-commit` 在配置中为布尔映射，不再是整行字符串
- [x] 7.3 删除 `sample/auth-rbac-boot-sample/src/main/resources/application-dev.yml`，其 MySQL 连接内容并入 `mysql` profile 段（凭据用占位值）；验证：文件不存在，且 `grep -rn "profiles.active: dev\|on-profile: dev" sample/` 无命中
- [x] 7.4 按 D5 为该模块声明 `spring.session.jdbc.initialize-schema`：`h2` 段 `embedded`、`mysql` 段 `never`；验证：两处声明位于对应 profile 段内
- [x] 7.5 实测默认 profile 启动（8081）：确认 12 张框架表 + 2 张示例表 + 视图建成、种子数据已灌入、`/meetingRoom/page` 与 `/checkMenu` 等接口可正常返回；验证：`/h2-console` 中 `sys_user`、`meeting_room` 行数均大于 0，接口无 5xx
  - 原受阻原因已解除：用户在启动类上加 `@SpringBootApplication(exclude = {MybatisPlusConfig.class})`，该配置类确实登记在 `basic-spring-boot-starter` 的 `spring.factories` 的 `EnableAutoConfiguration` 下，exclude 生效
  - 实测通过：profile "h2"、HikariCP-H2 连接、Tomcat 8081、`Started AuthRbacBootSampleApp in 10.243 s`；`GET /test/dataRange` 返回 `seed-data.sql` 中的真实行（`id=1 可通过超级管理员1`），该接口经 `@AuthDataRange` 查 `sys_auth_data`，即建表 + 灌种子 + MyBatis-Plus 在 H2 上查询全链路通；`GET /test/checkMenu` 返回 200
  - 两点偏差，均与本次改动无关：`/meetingRoom/page?page=1&size=10` 返回 500 `JdbcAuthDataPersistent.listAuthData ... "this.jdbcAuthDataPersistent" is null`（框架侧字段未注入，切 MySQL 同样会 NPE）；`/test/checkMenuNone` 返回 500 `AuthException: 没有API菜单/test/checkMenuNone访问权限`（权限框架按设计拒绝）。故本条"接口无 5xx"未逐条达成，达成的是数据库链路那部分
  - 视图 `view_menu_api_auth` 已随 `auth-rbac.sql` 建成（`check-sql-compat.sh` 实测 16 张表 + 视图脚本），但示例代码是否读取该视图仍未验证，保留为已知残留

## 8. 集成校验

- [x] 8.1 逐个示例（8080 端口互斥，一次只起一个）以默认 profile 启动并调用其代表接口：`basic-boot-sample` `/validated/hello`、`storage-boot-sample` `/h2-console`、`login-boot-sample` `/login`、`tenant-boot-sample` `/hello`、`auth-rbac-boot-sample` `/meetingRoom/page`；验证：5 个模块均启动成功且接口返回符合预期，全程不需要本机存在 MySQL
  - 实测 5/5 启动成功，全程未使用 MySQL：basic 以 h2 profile 通过 15 个既有契约测试（含 `/validated/hello` 等真实请求）；storage 启动干净、`/h2-console` 返回 302（控制台已注册）；login `POST /login` 返回 200 且下发 `SESSION`；tenant `GET /hello` 返回 `hello - null`；auth 启动后 `GET /test/dataRange` 返回真实种子行
  - 代表接口路径按实测修正：auth 示例未引 `login-security-spring-boot-starter`，其 `/login` 不存在（404），改用 `/test/dataRange` 与 `/test/checkMenu`；`/meetingRoom/page` 仍受框架侧 NPE 阻塞，见 7.5
- [x] 8.2 跑 `mvn -q -pl sample/basic-boot-sample test`；验证：`fb8dd51` 引入的既有 web 层契约测试在本次配置改动后仍全部通过
- [x] 8.3 静态确认 H2 未泄漏到 profile 之外；验证：`grep -rn "jdbc:h2\|org.h2.Driver" sample/ --include="*.yml"` 的每一处命中，人工核对都位于其所属 `on-profile: h2` 文档段内，且 `docs/sql/` 与 `scripts/` 两个目录在本次改动中无任何文件变更（`git diff --name-only` 中不出现）
