# Design

## Context

动机见 proposal.md - Why。本节只记录决定方案走向的现状与约束：

- `cu-parent` 现在同时是聚合器、构建约定载体、版本表来源。外部项目只有把它设为 `<parent>` 才能拿到后两者，而 Maven 单一继承使这与 `spring-boot-starter-parent` 互斥。
- `<scope>import</scope>` 只合并 `<dependencyManagement>`，**不携带** `<repositories>`、`<build>`、`<pluginManagement>`。
- 多个 import 之间是"先声明者胜"，因此搬动条目所在文件与顺序时，必须确认没有同 `groupId:artifactId` 在两边各管一份。实测结论：`spring-boot-dependencies:4.1.2` 里的 `guava` 属 `com.github.ben-manes.caffeine`，与框架自管的 `com.google.guava:guava:33.7.2-jre` 不是同坐标；`org.projectlombok:lombok`、`com.mysql:mysql-connector-j` 由 Spring 管且根 POM 未自定版本。**当前根 POM 的直接条目与三个上游 BOM 无 GA 重叠**，所以搬迁在版本上是中性的。
- 实测没有任何子 POM 引用 `framework-version` / `spring-boot-version` / `mybatis-plus-version` / `mybatis-version` / `jsqlparser-version` 这些属性（子模块的 `<parent><version>` 一律写字面量 `0.2.1`），属性组可整体搬走而不需保留镜像定义。
- `login-security-spring-boot-starter` 以 compile 作用域传递 `spring-security-saml2-service-provider` → OpenSAML，`~/.m2` 中已存在 `org/opensaml/*` 与 `net/shibboleth/*`，只能来自 shibboleth 仓库。
- 无私服、无 `<distributionManagement>`，外部消费依赖本地 `mvn install`。
- `other/pom.xml` 与 `sample/pom.xml` 都设了三个 skip 属性，但实测只有两个生效：`maven.install.skip` 由 maven-install-plugin 绑定、`maven.deploy.skip` 由 maven-deploy-plugin 绑定；**`maven.package.skip` 没有任何插件绑定**（本地 maven-jar-plugin 3.2.2 / 3.5.0 / 3.5.1 的 descriptor 里唯一的 `skip` 参数绑的是 `${maven.test.skip}`，且只作用于 test-jar 目标）。所以"示例与代码生成器不对外供给"实际是靠不 install 实现的，jar 照旧生成——这正是 README 里 `java -jar target/*-exec.jar` 能跑起来的前提。
- 命令行 `mvn` 默认走 `JAVA_HOME=C:\Java\jdk-17`，而根 POM 要求 `release=21`，实测 `mvn -o -pl basic compile` 直接失败（`错误: 不支持发行版本 21`）；带 `JAVA_HOME=/c/Java/jdk-21` 前缀后同一命令成功。本 change 里所有 maven 命令都必须显式指定 JDK 21。

## Goals / Non-Goals

**Goals:**
- 版本表成为可独立 import 的制品，外部项目保住自己 `<parent>` 的选择权。
- 搬迁过程可证明"只改交付形态、不改任何实际解析版本"。
- 框架仓库自身的构建方式不因引入 BOM 而变化（子 POM 一行不改）。

**Non-Goals:**
- 不做版本单源（`${revision}` + flatten-maven-plugin），不做 `maven-release-plugin`。
- 不把 SAML 依赖降为 `optional`，不改 starter 的依赖可见性。
- 不引入 `<pluginManagement>`：`spring-boot-maven-plugin` 在 5 处 POM 硬编码 `4.1.2` 属既有问题，import 机制本来也管不到插件版本，留给单独的 change。
- 不发 Maven Central / 私服，不改 `groupId`。

## Decisions

### D1：BOM 无 parent，`cu-parent` 以 `scope=import` 取版本

新建 `dependencies/pom.xml`，`groupId=com.cu.framework`、`artifactId=cu-dependencies`、`packaging=pom`，**不声明 `<parent>`**；它作为 `<module>` 挂进根 POM，根 POM 的 `<dependencyManagement>` 收缩为一条 `cu-dependencies` 的 import（置于首位）。

替代方案（Spring 式）：让 `cu-parent` 直接把 `cu-dependencies` 当 `<parent>`，框架内部靠继承取版本、外部靠 import，一张表零重复，形态与 `spring-boot-starter-parent` → `spring-boot-dependencies` 完全对齐。没选它的原因：这种"BOM 既是 `cu-parent` 的父 POM 又是它的 module"的自指聚合需要显式 `<relativePath>`，我对 Maven 在 reactor 里如何处理没有实测把握。这是纯粹的形式优化，不影响 spec 与任务拆分，若后续实测通过可作为跟进改动。

反向的顾虑已被实测排除：担心 `cu-parent` import 一个同 reactor 里、尚未安装到本地仓库的兄弟 BOM 会先撞上"建模阶段解析不到"的鸡生蛋问题。实测在 `~/.m2` 里完全没有 `cu-dependencies` 时，`mvn -o -q validate` 对整个 reactor 通过，随后全量 `clean install` 也成功，且 reactor 把 `cu-dependencies` 排在第一位构建——Maven 的 workspace reader 在建模阶段就能找到它。

### D2：BOM 收录全量版本表（含三个上游 BOM 的 import）

根 POM 现有的整张 `<dependencyManagement>` 原样搬入 `cu-dependencies`，包括 spring-boot / spring-cloud / spring-cloud-alibaba 三个 import 与所有第三方 pin。

理由：目标是"外部一个 import 换零版本号"，`spring-boot-dependencies` 本身就是最大化版本矩阵，有先例。

代价与逃生口：框架开始替别人的三方版本负责。消费方若要覆盖，把自定义条目排在 `cu-dependencies` 的 import **之前**（先声明者胜），不要依赖排在后面。这一条必须写进接入文档。

### D3：版本属性随版本表一起搬入 BOM

`framework-version`、`mybatis-plus-version`、`mybatis-version`、`jsqlparser-version`、`spring-boot-version`、`spring-cloud-version`、`spring-cloud-alibaba-version` 全部落在 `cu-dependencies` 自己的 `<properties>` 里（import 时按 BOM 自身模型插值，自有属性可正常解析）。根 POM 保留 `maven.compiler.release`、`maven.compiler.parameters`、`project.build.sourceEncoding`——它们是构建约定，不属于版本表。

不保留镜像定义的前提是上面那条"无子 POM 引用属性"的实测；若搬迁后 reactor 里出现无法解析的属性，说明实测漏了，按 D6 的 tree diff 会立刻暴露。

根 POM 里那条 import 的 BOM 版本写**字面量**而不是属性：`cu-parent` 已不再持有 `framework-version`，为一条 import 单独留一个属性反而多一处要同步的重复定义。代价是版本字面量因此多一处落点，由 `scripts/check-pom-version-sync.sh` 纳入第 2 项检查兜住。

### D4：`cu-parent` 继续持有 `<repositories>`，文档负责转告

shibboleth 仓库无法通过 BOM 传递，因此：框架仓库自己保留声明（内部构建照常）；外部接入文档必须显式给出同一 `<repositories>` 片段。不把仓库声明塞进 BOM 假装能生效。

### D5：不进 BOM 的坐标，也不为它们收口版本

`sample/*` 全部不入：`sample/pom.xml` 已设 `maven.install.skip`，示例不再安装到本地仓库，与 `code-generator` 同属"仓库外解析不到"的一类。`code-generator` 不入，且 `other/code-generator/pom.xml` 里 velocity 的 `2.3` 字面量保持原样：`other/pom.xml` 设了 `install.skip`（外加一个实际不生效的 `package.skip`，见 Context），该模块不安装，仓库外没有任何项目能解析到它，那个硬编码不可能与谁漂移。把它提进 BOM 换来的只是仓库内文本一致，代价是版本表里出现一条永远无法被外部验证的条目——与"版本表只覆盖可解析供给物"相悖。

### D6：搬迁正确性用 `dependency:tree` 全量快照 diff 证明

先跑一次并存档基线（`JAVA_HOME` 必须显式指向 JDK 21，见 Context）：

```
JAVA_HOME=/c/Java/jdk-21 mvn -o dependency:tree -DoutputFile=<file> -DappendOutput=true
```

对全 reactor 输出一份「迁移前」清单，改造后输出「迁移后」清单，逐行 diff 必须为空（唯一允许的差异是新模块 `cu-dependencies` 自身进入 reactor 的那一行）。这一步是本 change 的验收门，用来兜住 D2 里"GA 重叠"这类静默改版本的风险，也兜住 import 顺序与属性插值的意外。

快照取法实测修正：`-DoutputFile` 按各模块 basedir 解析，`-DappendOutput=true` 在所用 dependency-plugin 上不生效——绝对路径跑完全量后文件里只剩最后一个模块的 47 行。因此改为归一化 stdout：剥掉 `[INFO] ` 前缀，用 `:tree ... @ <module> ---` 行跟踪当前模块，把树行的图形前缀剥掉，输出 `模块<TAB>groupId:artifactId:type:version:scope` 的扁平表（扁平化后与树内顺序无关，diff 更稳）。实测该命令连跑两次 diff 为空，基线为 1682 行 / 24 个 reactor 条目。

## Risks / Trade-offs

- **[import 不带 build 配置，消费方可能丢 `-parameters`]** → 首选让消费方用 `spring-boot-starter-parent`（实测其 4.1.2 的 `maven-compiler-plugin` 已配 `<parameters>true</parameters>`）；用自定义 parent 时文档给出显式配置。丢失的表现是不写名称的 `@RequestParam` 在运行期报参数名解析失败，不会编译报错，属高危静默问题，必须写进文档正文而非脚注。
- **[import 不带 `<repositories>`，OpenSAML 解析失败]** → 文档给出 shibboleth 仓库片段（D4）。备选方案是把 SAML 依赖降为 optional，已列入 Non-Goals，另开 change。**实测取证方式换了**：这台机器删掉仓库声明后离线在线都能解析，因为 `.m2` 已缓存 OpenSAML，Maven 3.9 直接用本地副本——这项失败只在干净仓库上出现。必要性改由两条证据证明：`_remote.repositories` 记录构件来源 id 为 `shibboleth`；Central 上 `org/opensaml/opensaml-core-api/5.2.3` 是 404，同一文件在 Shibboleth 仓库是 200。
- **[`mvn -pl <module>` 单模块构建时 BOM 不在 reactor 内]** → 若 `cu-dependencies` 未安装到本地仓库，只构建单个子模块会去仓库找它并失败。缓解：文档与脚本统一用全量 `mvn install` 或带 `-am`；D6 的验证命令本身也按全 reactor 跑。
- **[版本字面量出现处从 2 类变 3 类]** → BOM 自身 `<version>`、`framework-version` 属性、每个子 POM 的 `<parent><version>`。本次不根治，但 tasks 里加一条 bump 一致性自查脚本，避免引入 BOM 后漏改。
- **[外部项目仍靠本地 `mvn install` 消费]** → BOM 也必须被安装才可用；全量 `mvn clean install` 已覆盖。仓库 `other/` 的 install.skip 恰好说明"是否进仓库"与"是否进 BOM"要一致，D5 据此排除 code-generator。
- **[旧写法与新写法并存]** → 已继承 `cu-parent` 的项目不会坏（根 POM 的 `<dependencyManagement>` 变成 import，对它透明），因此升级不强制断代；文档只把 import 写法列为默认推荐。
- **[`.m2` 里的 sample 残留]** → 7 个示例构件在加 `install.skip` 之前那次全量 install 中已装入 `~/.m2/repository/com/cu/framework/sample/`，新配置只阻止再装，不会删除已有内容；无人依赖它们，故不影响构建，但会让人误判示例仍可对外解析。清理是一次性手工 `rm`，且属仓库外写操作，交给用户执行。

## Migration Plan

1. 存档 D6 的"迁移前" `dependency:tree` 基线。
2. 新增 `dependencies/pom.xml`，根 POM 的 `<dependencyManagement>` 与版本属性组整体搬入，根 POM 换成单条 import 并加 `<module>dependencies</module>`。
3. 全量 `mvn clean install -DskipTests`，再出"迁移后" tree 并 diff，要求为空。
4. 收口漂移：`sample/auth-rbac-boot-sample` 删重复 p6spy 声明；`other/` 与 code-generator 的 velocity 按 D5 一律不动。`sample/pom.xml` 与 `other/pom.xml` 里那条不生效的 `maven.package.skip` 可摘除（行为完全不变，摘它是为了避免后来人误以为示例不产 jar），但不在必需项内。
5. 改三处接入文档（README / README.en / auth/README）：import 写法 + shibboleth 仓库 + 覆盖版本需排在 import 之前 + `-parameters` 说明 + 把 `mysql:mysql-connector-java` 修为 `com.mysql:mysql-connector-j`。
6. 仓库外最小夹具项目验证：只 import BOM、parent 用 `spring-boot-starter-parent`、声明 `login-security-spring-boot-starter`，跑 `mvn dependency:tree` 确认框架 starter 与 OpenSAML 全部解析成功。
7. 回滚策略：`cu-parent` 仍是完整聚合器与构建 parent，任何一步出问题都可整体 revert；已按旧写法继承的外部项目无需配合回滚。
