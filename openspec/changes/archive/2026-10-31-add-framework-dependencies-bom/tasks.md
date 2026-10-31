# Tasks

> 构建命令约定：本仓库所有 maven 命令都必须带 `JAVA_HOME=/c/Java/jdk-21` 前缀。实测命令行 `mvn` 默认走 `C:\Java\jdk-17`，`release=21` 会在 compile 阶段直接失败（`错误: 不支持发行版本 21`）；带前缀后同一命令成功。依赖树快照统一落在 `logs/`（`.gitignore` 里有 `**/logs/`，且不受 `mvn clean` 影响）。

## 1. 迁移前基线取证

- [x] 1.1 全 reactor 输出「迁移前」依赖树快照。**实测 `-DoutputFile=<绝对路径> -DappendOutput=true` 不可用**：dependency-plugin 的 `outputFile` 按各模块 basedir 解析，`appendOutput` 在这版上不生效，跑完文件里只剩最后一个模块。改用 stdout 归一化（下面这条即为快照命令，1.1 与 2.3 必须逐字同一条）：
      `JAVA_HOME=/c/Java/jdk-21 mvn -o dependency:tree 2>&1 | sed -n 's/^\[INFO\] //p' | awk -v OFS='\t' '/^--- .*:tree .* @ /{m=$0;sub(/.* @ /,"",m);sub(/ ---.*/,"",m);mod=m;next} /:(jar|pom|war):/{l=$0;sub(/^[^A-Za-z0-9]+/,"",l);print mod,l}' > logs/dependency-tree.before.txt`
      实测结果：1682 行、24 个 reactor 条目（含 5 个聚合 POM），同一条命令连跑两次 diff 为空（`logs/dependency-tree.dupcheck.txt`）
- [x] 1.2 抽查快照确认可作为 diff 基线，实测全部命中：`com.google.guava:guava:33.7.2-jre`（自管）、`org.projectlombok:lombok:1.18.38` 与 `com.mysql:mysql-connector-j:9.7.0`（均由 Spring BOM 管）、`p6spy:p6spy:3.9.1`、`org.mybatis:mybatis:3.5.19`、`com.github.jsqlparser:jsqlparser:4.9`、`cn.hutool:hutool-all:5.8.48`、`com.alibaba:fastjson:2.0.66`；`org.opensaml:*` 共 52 行，分布在 `login-security-spring-boot-starter` 与 `login-boot-sample`、`auth-rbac-boot-sample`、`auth-rbac-security-boot-sample`、`auth-rbac-security-spring-boot-starter` 的树中

## 2. BOM 落地与根 POM 收缩

- [x] 2.1 新建 `dependencies/pom.xml`：`com.cu.framework:cu-dependencies:0.2.1`、`packaging=pom`、不声明 `<parent>`，把根 POM 现有整张 `<dependencyManagement>`（含 spring-boot / spring-cloud / spring-cloud-alibaba 三个 `import` 与全部第三方 pin）与版本属性组（`framework-version`、`mybatis-plus-version`、`mybatis-version`、`jsqlparser-version`、`spring-boot-version`、`spring-cloud-version`、`spring-cloud-alibaba-version`）原样搬入，验证为 `JAVA_HOME=/c/Java/jdk-21 mvn -o -N -f dependencies/pom.xml validate` 通过
- [x] 2.2 根 `pom.xml`：`<modules>` 增加 `dependencies`，`<dependencyManagement>` 收缩为单条 `cu-dependencies` 的 `import`（不留残余条目），删除已搬走的版本属性；保留 `maven.compiler.release`、`maven.compiler.parameters`、`project.build.sourceEncoding`、`<repositories>`、`<build>` 与两个 annotation processor。验证：`JAVA_HOME=/c/Java/jdk-21 mvn -o -q validate` 通过，且 `grep -c '<scope>import</scope>' pom.xml` 输出 1
- [x] 2.3 全量重装并做 D6 的 tree diff 验收门：`JAVA_HOME=/c/Java/jdk-21 mvn -o clean install -DskipTests` 成功后，用 1.1 那条快照命令（只把结尾的 `logs/dependency-tree.before.txt` 换成 `logs/dependency-tree.after.txt`，其余一字不改）产出迁移后快照，与基线逐行 diff 必须为空——唯一允许的差异是新增一行 `cu-dependencies	com.cu.framework:cu-dependencies:pom:0.2.1`（新模块自身进入 reactor），除此之外任何一行差异都不接受。**若出现任何版本行差异，停下来判断是不是 GA 重叠触发了「先声明者胜」，不要靠调整别的声明把它盖掉**
- [x] 2.4 确认单模块构建路径不受影响：`JAVA_HOME=/c/Java/jdk-21 mvn -o -q -pl auth/auth-core -am dependency:tree` 与去掉 `-am` 的同一条命令都成功（后者依赖已安装的 `cu-dependencies`）；若去掉 `-am` 失败，把「必须带 `-am` 或先全量 install」补进 4.4 的文档要求再继续

## 3. 既有漂移收口与维护脚本

- [x] 3.1 `sample/auth-rbac-boot-sample/pom.xml`：删除自建的 `p6spy` `<dependencyManagement>` 块（版本由 BOM 供给），验证为 `JAVA_HOME=/c/Java/jdk-21 mvn -o -q -pl sample/auth-rbac-boot-sample dependency:tree -Dincludes=p6spy` 仍解析出 `p6spy:3.9.1`
- [x] 3.2 核对 BOM 的收录边界（spec「BOM 只收录可解析的对外坐标」）：`dependencies/pom.xml` 不含任何 `com.cu.framework.sample:*` 与 `code-generator` 坐标；确认 `other/code-generator/pom.xml` 的 velocity `2.3` 保持原样，且本次不动 `other/pom.xml` 与 `sample/pom.xml` 的 skip 属性（`sample` 已设 `maven.install.skip`，与 code-generator 同属仓库外解析不到的一类，见 design D5）。验证：`grep -nE 'com\.cu\.framework\.sample|code-generator|velocity' dependencies/pom.xml` 无输出，`git diff -- other/` 为空
- [x] 3.3 **由用户在提交 `a2932d1` 中自行完成**（2026-10-31 22:21），不由 agent 动手：`sample/pom.xml` 与 `other/pom.xml` 里的 `maven.package.skip` 两行都已删除，实测 `grep -n skip other/pom.xml sample/pom.xml` 只剩 `install.skip` / `deploy.skip`（`other/pom.xml` 另有 `maven.test.skip`，那个是真生效的）。背景：该属性实测无任何插件绑定（本地 maven-jar-plugin 3.2.2 / 3.5.0 / 3.5.1 的 descriptor 里 `skip` 只绑 `${maven.test.skip}` 且只作用于 test-jar），属空转，删除不改变构建产物——归档前最后一次全量 `clean install` 已证明 exec jar 照常产出
- [x] 3.4 新增 `scripts/check-pom-version-sync.sh`：从根 `pom.xml` 读 `<version>` 作基准，断言根 BOM import 版本、BOM 的 `<version>`、BOM 内 `framework-version` 属性、全部子 POM 的 `<parent><version>` 与之相等；不一致或读不到都非零退出并列出行号。实测覆盖：真仓库 exit 0；`VERSION_SYNC_ROOT` 指向 `/tmp` 夹具后 4 类不符 + 2 类"读不到"共 6 个分支各自 exit 1，且每次破坏后还原回 exit 0。用法与盲区登记在 `scripts/README.md`，并把该脚本加进 `README.md`「参与贡献」的自检命令串。**验证方式修正**：原任务写的"临时把某个子 POM 的版本改成别的值后脚本必须失败（改完还原）"没有被采用——对被跟踪文件做破坏性验证曾误删三处 `<artifactId>` 行（已逐行补回并复验），改为夹具上做

## 4. 接入文档改写法

- [x] 4.1 `README.md`「路径二」片段（约 138 行）改为：parent 用 `spring-boot-starter-parent` + `dependencyManagement` import `cu-dependencies`，并补一段 `<repositories>` 的 shibboleth 声明，说明不继承 `cu-parent` 时 import 不会带仓库；同时把示例里的 `mysql:mysql-connector-java` 改为 `com.mysql:mysql-connector-j`。验证：按片段新建临时项目（不放仓库内）跑 `JAVA_HOME=/c/Java/jdk-21 mvn dependency:tree` 解析成功
- [x] 4.2 `README.en.md`（约 127 行）同步 4.1 的同一改法，验证为两份 README 的依赖片段逐字段一致（groupId / artifactId / 版本号 / repositories）
- [x] 4.3 `auth/README.md`「5 分钟接入」片段（约 39 行）同步改法，验证为按该片段能列出 `auth-rbac-security-spring-boot-starter` 与其传递的 starter，且 `grep -rn 'mysql-connector-java' README.md README.en.md auth/README.md` 无结果
- [x] 4.4 在同一批文档里补三条使用方必读：覆盖版本须把自定义条目排在 `cu-dependencies` 的 `import` **之前**（先声明者胜，排后面不生效）；parent 不是 `spring-boot-starter-parent` 时必须自行开启 `-parameters`，否则不写名称的 `@RequestParam` 等在运行期报参数名解析失败（不会编译报错）；消费前需先 `mvn clean install` 框架仓库（BOM 同样要安装，而 sample 与 code-generator 装了也不装都不影响外部使用）。验证：三条各自能在文档正文（非脚注）找到，且与 design.md 的 D2 / D4 / Risks 不矛盾
- [x] 4.5 检查其余文档是否还有「继承 `cu-parent` 才能免版本」的表述并一并改为 BOM 写法（`grep -rn 'cu-parent' README.md README.en.md docs/ auth/ boot/ sample/`），验证为命中项要么已改写、要么明确说明 `cu-parent` 是框架内部构建用的 parent
- [x] 4.6 `README.md`「5 分钟跑起来」的 exec jar 路径复核：`sample` 加了 `maven.install.skip` 之后 jar 仍照常产出（`package.skip` 是空转），确认 `mvn clean install -DskipTests` → `java -jar sample/auth-rbac-boot-sample/target/auth-rbac-boot-sample-0.2.1-exec.jar` 这条文档路径没有被示例的 skip 改动打断；若发现 jar 未生成，停下来按 design Context 的属性实测重查，不要直接改文档命令

## 5. 外部消费端到端确认

- [x] 5.1 在仓库外建最小夹具（`%TEMP%/bom-fixture`，**pom 正文由 `README.md` 路径二的片段直接抽取拼装**，测的是文档本身）：parent 为 `spring-boot-starter-parent:4.1.2`、`dependencyManagement` 只 import `cu-dependencies`、`<repositories>` 含 shibboleth。实测：离线与在线 `mvn dependency:tree` 均 BUILD SUCCESS，树 163 行；`org.opensaml:opensaml-*:5.2.3` 共 17 行经 `auth-rbac-security-spring-boot-starter` 传递进来；`spring-security-saml2-service-provider:7.1.2`、`com.mysql:mysql-connector-j:9.7.0` 均免版本；夹具 pom 里 `<version>` 只出现 3 次——自身的 1.0.0、Spring Boot parent 的 4.1.2、BOM 的 0.2.1，框架与三方依赖零版本号
- [x] 5.2 仓库声明的必要性取证（**原设想被实测否掉并换路**）：把夹具的 `<repositories>` 删掉后，离线与在线解析都仍成功——本机 `.m2` 已缓存 OpenSAML，Maven 3.9 照样用本地副本，缺失的仓库声明被缓存掩盖，所以“去掉声明就失败”在这台机器上无法复现。改用两条硬证据：`~/.m2/repository/org/opensaml/opensaml-core-api/5.2.3/_remote.repositories` 记录 `opensaml-core-api-5.2.3.jar>shibboleth=`（构件只来自该 id）；`curl` 探测 `repo1.maven.org/maven2/org/opensaml/opensaml-core-api/5.2.3/*.pom` 返回 **404**，同一文件在 `build.shibboleth.net/maven/releases` 返回 **200**（对照组：Central 上 `spring-boot-dependencies-4.1.2.pom` 为 200）。结论：干净仓库上少了这段声明必然解析失败，spec 已把“缓存会掩盖失败”单列为一个 scenario
- [x] 5.3 既有回归：`bash scripts/check-sql-compat.sh` → 两遍各 26 张表 ok、exit 0；`bash scripts/check-config-docs.sh` → 67 个 YAML 路径节点 / 48 个 metadata 键全部覆盖、exit 0。运行时复测需先让开端口：8081 被用户的 HBuilderX uni-app dev server（pid 18436，node）占着，第一次启动的 jar 因端口冲突自行退出，改用 `--server.port=8082` 起 exec jar（收尾已停止，无残留 java 进程）。实测 `curl -H 'X-Demo-User: admin' 'http://localhost:8082/meetingRoom/page?page=1&size=10'` 返回 code 200 且 `data.data` 有会议室行；`X-Demo-User: outsider` 返回 `{"code":403,"errorType":"AUTHORIZATION","message":"没有API菜单/meetingRoom/page访问权限"}` —— 菜单鉴权与数据权限行为未受影响
