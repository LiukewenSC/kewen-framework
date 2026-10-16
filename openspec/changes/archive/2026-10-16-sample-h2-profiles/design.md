# Design

## Context

Spring Boot 2.7.18（`pom.xml:102`）与 Java 8 编译目标；H2 版本由 `spring-boot-dependencies` 管理，即 2.1.214 —— 与 `docs/说明文档/已废弃实现与决策记录.md` 第四节记录实测结论时用的版本一致，无需重新试探方言边界。

现状约束：

- `docs/sql/` 是框架级建表的唯一一份，只写 MySQL 与 H2(MODE=MySQL) 公共子集，由 `scripts/check-sql-compat.sh` + `scripts/CheckCompat.java` 用 H2 实测。检查 glob 为 `docs/sql/*.sql` 与 `sample/*/sql/*.sql`。
- `basic-boot-sample`、`storage-boot-sample` 已通过 pom 的 `<resources>` 把 `docs/sql` 挂到 `classpath:sql/`，并用 `spring.sql.init` 自动建表。
- `auth-rbac-boot-sample` 的 `sql/` 目录不在 classpath 上（pom 无 `<resources>`），所以其种子脚本此前只能手动 `mysql < ...` 导入。
- `tenant-boot-sample` 的业务代码只用 `TenantContext`，不访问任何表；但它依赖 `basic-spring-boot-starter`，后者传递带入 `mybatis-plus-boot-starter` 与 `mysql-connector-java`，因此 DataSource 是被动必需 —— 去掉配置会让 `DataSourceAutoConfiguration` 因无法确定驱动而启动失败。
- `auth-rbac-boot-sample` 与 `login-boot-sample` 使用 `spring-session-jdbc`。前者未声明 `initialize-schema`（默认 `embedded`），同时其 `sql/init.sql` 尾部自带 `SPRING_SESSION` 建表，两处都会创建同名表。
- 用户已删除 `idaas-sp-boot-sample`，本次范围为 5 个 sample。

## Goals / Non-Goals

**Goals:**

- 一条可复制的 profile 模板，5 个 sample 共用同一形状，差异只体现在"需要哪些脚本"。
- H2 的一切（依赖声明除外，见 D9）在配置文件中以 profile 段为边界，扫一眼即可确认没有泄漏到公共段。
- 切到 `mysql` profile 后，示例对用户真实库只做 DML 级别的读取，不做任何 DDL。

**Non-Goals:**

- 不给 sample 增加自动化测试；验收方式是人工启动加接口调用。
- 不改框架模块的 SQL、依赖或配置。
- 不把种子数据的 INSERT 改写成 upsert 语法（见 D7 的取舍）。
- 不处理历史上已提交进公开仓库的凭据吊销，只保证本次改动的配置里不再出现真实值。

## Decisions

### D1 profile 命名统一为 `h2` / `mysql`，sample 默认激活 `h2`

沿用 `basic-boot-sample`、`storage-boot-sample` 已提交的命名，避免再造第三套。`auth-rbac-boot-sample` 现有的 `dev` 退役。

*备选*：保留 `dev` 作为 MySQL profile。否决 —— `dev` 语义是"开发环境"而非"MySQL"，与其余 4 个 sample 的按库命名不一致，读者要靠记忆才知道 dev 指什么。

*对"`正常其它环境还是用 MySQL`"的解读*：该约束指 H2 不得离开 sample 的 profile 边界，不是指 sample 内默认应激活 MySQL。依据是 `basic`/`storage` 已在 `f98ee5c` 以默认 `h2` 提交并被接受。

### D2 用 `spring.config.activate.on-profile` 的多文档块，而非 profile 目录下的 `application-<p>.yml`

单文件内 `---` 分段，公共段与 profile 段并排可见，便于人工确认 H2 没漏进公共段；`auth-rbac-boot-sample` 现有的 `application-dev.yml` 独立文件形态一并废除，减少"改一个配置要翻两个文件"。

*备选*：`application-h2.yml` / `application-mysql.yml` 外置文件。否决 —— 差异只有几行数据源参数，拆文件后反而看不出公共段里是否混入了 H2 声明。

### D3 `spring.sql.init` 的 `mode` / `schema-locations` / `data-locations` 全部只写在 `h2` profile 段

顶层公共段不再声明 `spring.sql.init` 任何键。这直接修掉 `basic-boot-sample/src/main/resources/application.yml:14` 与 `storage-boot-sample/src/main/resources/application.yml:10` 的问题：`mode: always` 写在公共段，激活 `mysql` 后每次启动对真库执行带 `DROP TABLE IF EXISTS` 的脚本。

*备选*：只在公共段声明 locations，`mode` 交给 Spring Boot 默认的 `embedded`（只对嵌入式库执行脚本）。技术上同样安全，且少一份重复。否决 —— 依赖框架默认值来达成"MySQL 不被动"，配置文件本身读不出这个保证；而本次的硬要求正是能从配置文件直接看出 H2 只在 profile 内。

采用显式方案：profile 段内 `mode: always`，MySQL profile 不声明 locations 也不声明 mode。

### D4 `init.sql` 按用途重新归堆，不是按行号切两半

`sample/auth-rbac-boot-sample/sql/init.sql` 的 399 行是"数据在前、建表夹在中间、末尾又漏了两条数据"的顺序，因此拆分是重新归堆：

| 原行范围 | 内容 | 去处 |
|---|---|---|
| 1–321 | 10 张框架表的种子 INSERT | `sql/seed-data.sql` |
| 322–336 | `testauth_annotation_business` 建表 | `sql/schema.sql` |
| 337–348 | 它的 3 条 INSERT | `sql/seed-data.sql` |
| 350–367 | `meeting_room` 建表 | `sql/schema.sql` |
| 368 | `meeting_room` 1 条 INSERT | `sql/seed-data.sql` |
| 370、372 | 夹在中间的两条 `sys_auth_data` INSERT | `sql/seed-data.sql` |
| 377–399 | `SPRING_SESSION` + `SPRING_SESSION_ATTRIBUTES` | 删除（见 D5） |

装配为：

```
schema-locations = classpath:sql/auth-rbac.sql , classpath:sql/schema.sql
data-locations   = classpath:sql/seed-data.sql
```

顺序天然成立：Spring 的 `spring.sql.init` 先跑完全部 schema-locations 再跑 data-locations，框架表已在 `docs/sql` 那一份里建好。

**种子文件名必须叫 `seed-data.sql` 而不是 `data.sql`**：`scripts/check-sql-compat.sh` 用 `ls | sort` 决定执行顺序，`data.sql` 排在 `schema.sql` 之前，实测导致 4 条 INSERT 因表未建而失败；`schema.sql` < `seed-data.sql`（第 2 个字符 `c` < `e`）才是正确顺序。改检查器的排序会违反"本次不动 `scripts/`"的边界，所以由文件名承担顺序。Spring 侧不受影响，因为两个 location 都是显式列出的。

*备选*：只切成 `schema.sql` + `data.sql` 以外的更细粒度（每张示例表一个文件）。否决 —— 示例只有两张自有表，多文件增加挂载与排序成本。

### D5 会话表交给 spring-session，从脚本中删除

`sql/schema.sql` 不含 `SPRING_SESSION`。`auth-rbac-boot-sample` 与 `login-boot-sample` 显式声明 `spring.session.jdbc.initialize-schema: embedded`（H2 走 jar 内 `schema-h2.sql`），`mysql` profile 下声明 `never`。

两处都建同名表时不会报错，因为双方都先 `DROP TABLE IF EXISTS`，但谁后跑谁清掉对方写入的数据，属于不可预测的覆盖。

*MySQL 侧的后果*：`never` 意味着使用者的会话表必须自己建。仓库不再保留这份 DDL，README 需指向 `spring-session-jdbc` jar 内的 `org/springframework/session/jdbc/schema-mysql.sql`。

*备选*：`mysql` profile 用 `always`。否决 —— 每次启动 `DROP` 会话表，使用者登录后才发现会话丢失，比要求手动建一次表更糟。

### D6 `docs/sql` 挂载上提到 `sample/pom.xml`，各 sample 只声明自己额外的挂载

上提时必须**原样**搬动 `<resource>` 块，目录值继续写 `${project.basedir}/../../docs/sql`，不按父 pom 所在层级改成 `/../docs/sql`。原因是 `maven-resources-plugin` 在执行期读取的是当前项目的 effective model，继承下来的 `${project.basedir}` 按子模块上下文插值，因此同一个字符串对 5 个子模块都解析为仓库根的 `docs/sql/`。

这一插值方向是本决定唯一的外部前提，所以在任务里写成有实测分支的：`process-resources` 后若 `target/classes/sql/` 没出现 `docs/sql` 的文件、或该目录被解析成 `sample/docs/sql`，判定上提不成立，退回"各子模块自持一份声明"（即今天的状态），并追加一项 5 份声明字符串一致性的比对。

**必须同步删除 `basic-boot-sample` 与 `storage-boot-sample` 自己 pom 里的 `<resources>` 块** —— Maven 的子级 `<resources>` 是整体覆盖而非追加，留着会让这两个模块丢掉继承来的挂载，同时其余模块白拿。同理，`auth-rbac-boot-sample` 若要挂自身 `sql/`，其 `<resources>` 必须同时列出 `docs/sql` 与 `${project.basedir}/sql` 两项，只写自己那一项会把继承来的挤掉。

`docs/sql` 现有文件名为 `auth-rbac.sql`、`basic-support.sql`，与 `schema.sql` / `data.sql` 不冲突。

*备选*：做一个专门放 SQL 的 artifact 让 sample 依赖。否决 —— 为 2 个文件引入一个模块和一条依赖链，成本大于收益。

### D7 种子数据只在 `h2` profile 自动灌，MySQL 侧维持手动导入

`data-locations` 只出现在 `h2` profile 段。H2 内存库每次启动都是空库，带显式主键的 INSERT 不会撞键。

*备选*：把 INSERT 改写成 MySQL 与 H2 都接受的 upsert 形式，使种子数据幂等，从而两个 profile 都能自动灌。否决 —— 两边幂等写法的公共子集基本为空（MySQL 是 `ON DUPLICATE KEY UPDATE` / `REPLACE INTO`，H2 的 `MERGE INTO` 语法又不被 MySQL 接受），要改 100 多行且牺牲可读性。

**依据这一条的前提**：现有 `scripts/CheckCompat.java:26` 的连接串 `jdbc:h2:mem:check` 未带 `DB_CLOSE_DELAY=-1`，"跑两遍"实际是在两个全新库上各跑一遍，因此该检查器**证明不了**脚本幂等。不能拿它的绿色结果当自动灌 MySQL 的理由。

### D8 `tenant-boot-sample` 只给空 H2，不挂任何脚本

它的代码不访问表，`cu.request.persistent.database` 也未开启，因此 `h2` profile 只需要一个能连上的空内存库来安抚传递依赖带来的 `DataSourceAutoConfiguration` 与 mybatis-plus。`schema-locations` 一概不声明。

*备选*：顺手挂 `basic-support.sql`，让 tenant 也"看起来完整"。否决 —— 为一个不需要表的示例引入表，等于多一处需要同步的 DDL 消费者。

### D9 H2 依赖用 `runtime` scope，p6spy 只留在 `mysql` profile

5 个 sample 的 pom 声明 `com.h2database:h2`（版本由 BOM 管理，不写死）。`h2` profile 用 `org.h2.Driver` 直连；`mysql` profile 保留 p6spy，URL 修正为 `jdbc:p6spy:mysql://`。`spy.properties` 因此只对 `mysql` profile 有意义。

*备选*：H2 用 `test` scope。否决 —— 这里是运行期 `spring-boot:run` 要用的驱动，不是单元测试依赖；`test` scope 进不了运行时 classpath。

### D10 顺带修 YAML 与文档漂移

`auto-commit:true`、`connection-test-query:select 1` 这类冒号后缺空格的写法会被 YAML 解析成一个字符串值，整条配置静默失效（`auth-rbac-boot-sample/src/main/resources/application.yml:11-12`）。既然本次要重写同一批数据源段，就一并修正，不另开一次改动。

README 三处 `auth-boot-sample` 旧模块名（`:165`、`:225`、`:226`）随本次一起更新，因为 `:226` 的导入命令引用的文件在本次拆分后已不存在。

## Risks / Trade-offs

- **[子模块 `<resources>` 覆盖继承，导致挂载丢失]** → D6 已把"删除两处旧声明"与"auth 必须两项并写"列为必做任务；验证手段为 `process-resources` 后检查 `target/classes/sql/` 文件清单，同时确认 `spy.properties` 等原有资源仍在。
- **[继承串的 `${project.basedir}` 插值方向与预期相反]** → 上提本身带一个实测分支（任务 1.1 的判定 + 1.2b 的退回路径）。退回后功能完全等价，只多出 5 份需要人工保持一致的声明，由 8.3 的字符串比对兜住；不会因此卡住后续 profile 改造。
- **[profile 段写错层级导致静默不生效]** → 每个 sample 启动后确认实际激活 profile 与其 datasource URL（日志或 actuator 均可），不假设 YAML 分段正确。
- **[seed-data.sql 的 INSERT 不带列名，依赖 docs/sql 的列顺序]** → 列数不符会被 `scripts/check-sql-compat.sh` 执行时报错拦住；同类型列换序导致的静默数据错位是该检查器的能力盲区，作为已知残留记录，不在本次解决。本次不补列名，避免把 100 多行 INSERT 的重写风险混进配置类改动。
- **[MySQL 侧不再有会话表 DDL 来源]** → README 指向 spring-session jar 内脚本路径，并在 `mysql` profile 注释注明 `initialize-schema: never` 的后果。
- **[`dev` profile 退役]** → README 与 sample 内注释改为 `mysql`，是一次有意的 BREAKING；使用者需自行改启动参数。
- **[auth 与 login 的 H2 启动未经实测]** → 已知可本地跑通的只有 `basic-boot-sample`（`fb8dd51`）与 `login-boot-sample`。`auth-rbac-boot-sample` 涉及 mybatis-plus + 视图 + 12 张表，实测失败时按风险处理，不当作既定事实。
- **[视图 `view_menu_api_auth`]** → `docs/sql/auth-rbac.sql:143` 建的是视图；H2 对 `DROP VIEW IF EXISTS` + `CREATE VIEW` 的重复执行已被检查器覆盖，但示例代码是否真正走该视图尚未确认，列为实测项。

## Migration Plan

1. 先动构建：`sample/pom.xml` 加上提的 `<resources>`，同时删掉 `basic` / `storage` 两处旧声明；给缺 H2 依赖的 3 个 sample 补 `runtime` 依赖，给 `auth-rbac-boot-sample` 补自身 `sql/` 挂载。
2. 再动 SQL：从 `init.sql` 归堆出 `schema.sql` 与 `seed-data.sql`，删除会话表段，删掉 `init.sql`；立刻跑 `bash scripts/check-sql-compat.sh`，让检查器覆盖新文件名，把 SQL 层问题在配置改动之前清掉。
3. 然后动配置：按 D1–D5 逐模块重写 5 个 `application.yml`，一次一个模块，改完即启动验证。
4. 最后动文档：README 的目录树、导入命令、profile 说明；`auth-rbac-boot-sample` 的 `application-dev.yml` 删除。

**Rollback**：改动全部落在 `sample/` 与 `README.md`，不触碰框架模块与 `docs/sql/`，回滚即整批还原 `sample/`。SQL 拆分保留一次独立提交（`init.sql` 删除与新文件添加在同一提交内），便于只回滚配置而留下拆分结果。
