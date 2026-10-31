# Proposal

## Why

`cu-parent` 一个 POM 同时承担聚合器、构建约定、依赖版本表三种角色，外部项目想免版本号引入框架，就只能把它设为自己的 `<parent>`。而 Maven 只允许单一继承，需要 `spring-boot-starter-parent` 的项目（如 `kw-idp-simple`）物理上无法继承，只能自己抄一遍版本号——`sample/auth-rbac-boot-sample` 里重复声明 p6spy 3.9.1 就是这个约束下的临时手法。

结果是目前三处接入文档（README、README.en、auth/README）都只能教外部项目"继承 cu-parent"这一条路，框架不像标准框架：Spring 自己也是把版本表单独发成 `spring-boot-dependencies` 供 import，而不是要求所有人都继承 `spring-boot-starter-parent`。

## What Changes

- 新增 `com.cu.framework:cu-dependencies`（`packaging=pom`，无 parent，位于 `dependencies/pom.xml`），承载根 POM 现有的整张 `<dependencyManagement>`：框架自身 11 个模块坐标、spring-boot / spring-cloud / spring-cloud-alibaba 三个上游 BOM 的 `import`，以及全部第三方库版本与对应版本属性。
- 根 POM 的版本表改为单一入口：`<dependencyManagement>` 只留 `cu-dependencies` 的 `import`（置于 import 列表首位），`<modules>` 增加 `dependencies`；构建约定（`release=21`、`-parameters`、encoding、`maven-source-plugin`）、`<repositories>` 与两个 annotation processor 仍留在 `cu-parent`。
- 外部项目接入方式改为：parent 归项目自己 + `dependencyManagement` import `cu-dependencies`，并**自行声明 shibboleth 仓库**——`import` 只合并版本表，不携带 `<repositories>`，而 `login-security-spring-boot-starter` 以 compile 作用域传递 `spring-security-saml2-service-provider` → OpenSAML，后者不在 Maven Central。
- **BREAKING**（对外部继承方，非 API 层）：文档不再推荐用 `cu-parent` 作 parent 引入框架；已继承的外部项目仍可继续工作，但升级框架版本时须改为 import `cu-dependencies`。
- 顺带收口既有的版本漂移：`sample/auth-rbac-boot-sample` 删除重复的 p6spy `dependencyManagement`。`other/code-generator` 的 velocity `2.3` 硬编码**不动**——`other` 聚合不打包、不安装、不发布，仓库外无人能消费该模块，BOM 替它管版本等于给不可消费的坐标定版。
- 修正文档中的失效坐标：接入示例里的 `mysql:mysql-connector-java` 在 Spring Boot 4 已不受管理（现为 `com.mysql:mysql-connector-j`），按新写法前者拿不到版本会直接构建失败。
- 同步 README / README.en / auth/README 三处接入片段。

明确不做（各自单开 change）：版本单源改造（`${revision}` + flatten-maven-plugin）、把 SAML 依赖降为 optional、发布到 Maven Central 或私服。

## Capabilities

### New Capabilities

- `build/framework-bom`: 框架依赖版本表作为可 import 的独立 BOM 对外提供；规定 BOM 的内容边界、`cu-parent` 与 BOM 的单一事实源关系，以及外部项目在不继承 `cu-parent` 时仍需自备的构建前提（shibboleth 仓库、`-parameters`）。

### Modified Capabilities

无。本次只改变依赖版本的交付形态，不改动任何既有能力的行为契约（鉴权、数据权限、登录交付、异常契约、存储、示例验证台均不变）。

## Impact

- **构建文件**：新增 `dependencies/pom.xml`；根 `pom.xml` 的 `<modules>` 与 `<dependencyManagement>`；`sample/auth-rbac-boot-sample/pom.xml` 去掉一处硬编码版本。`other/code-generator/pom.xml` 不在改动范围。
- ** reactor 顺序**：`cu-dependencies` 无 parent 且被 `cu-parent` 聚合，需确认 `mvn clean install` 时它先于依赖它的模块构建。
- **文档**：实测只有 3 处含 `<parent>cu-parent</parent>` 接入片段——`README.md`（138 行）、`README.en.md`（127 行）、`auth/README.md`（41 行）；`docs/` 与 `boot/*/README.md` 无命中。
- **不进 BOM**：`code-generator` 与 `sample/*`。前者由 `other/pom.xml` 的 `maven.install.skip` 保证从未安装；后者也已加上 `maven.install.skip`（同文件里那条 `maven.package.skip` 实测无插件绑定，是空转，jar 仍照常产出，所以 README 的 exec jar 路径不受影响）。
- **验证前提**：实测命令行 `mvn` 默认走 `C:\Java\jdk-17`，而根 POM 要求 `release=21`，`mvn -o -pl basic compile` 直接失败；所有 maven 命令须显式 `JAVA_HOME=/c/Java/jdk-21`。
- **验证**：全仓 `mvn clean install -DskipTests` → 仓库外最小夹具项目 import BOM 后 `mvn dependency:tree`，须解析出框架 starter 与 OpenSAML 传递依赖且版本号齐全。
