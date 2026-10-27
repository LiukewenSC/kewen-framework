# Tasks

## 1. 只读预检端点

- [x] 1.1 在 `sample/auth-core-boot-sample` 新增请求/响应 DTO（`menuPaths`、`dataChecks`、`auths`、`menu`、`data`、每个 ID 的 `required/matched/missing/ok`）；验证：`mvn -q -pl sample/auth-core-boot-sample -am compile` 通过。
- [x] 1.2 在 `DemoMetaController` 新增 `POST /demo/permission-view`（或独立 controller，路径仍在 `/demo` 下、无 `@AuthMenu`），注入 `DemoAuthContextHolder`、`DemoMenuAuthHandler`、`DemoMenuAuthMapper`、`AuthDataHandler`；验证：启动后用 curl 分别以 alice、bob、无身份调用，返回的 `menu`/`data.ok` 与 `verify.sh` 已知结果一致。
- [x] 1.3 预检端点对每个 data ID 单独调用 `hasDataOperateAuths`，用 `getDataAuths` 返回 `required`，用当前权限集合求 `matched/missing`；验证：bob 对 `/workOrder/update` + `work_order/edit/1` 返回菜单 `ok=true`、行级 `ok=false`，alice 两者均 `true`。
- [x] 1.4 端点增加空列表与上限保护（菜单路径 ≤ 50、dataCheck ≤ 50、每个 IDs ≤ 100），超限返回 400；验证：空列表返回空结果，101 个 ID 返回 `code=400`，预检不带 `X-Trace-Id` 时 `/demo/trace` 不新增 judge 记录。

## 2. C22 判据与种子数据

- [x] 2.1 在 `sql/seed-data.sql` 为 `/authData/grant`、`/authData/remove`、`/authData/fill` 各补一条 `ROLE__20` 菜单授权，保持 `WHERE NOT EXISTS` 幂等；验证：重启两次后 `demo_menu_auth` 行数只增加 3 行且不重复。
- [x] 2.2 保持 `demo_auth_data` 的 `demo_admin/900/unified` 只授予 `ROLE__10`；验证：bob 调 `/authData/grant` 返回 403 且消息为 `权限校验不通过`，说明拒绝发生在行级而非菜单。
- [x] 2.3 收紧 `verify.sh` 的 C22 断言，在 `code=403` 之外加 `grep -q '权限校验不通过'`；验证：`bash verify.sh` 的 C22 通过，且改动前用旧种子的对照会显示菜单拒绝消息。
- [x] 2.4 确认 B6 的 `demo_menu_api` 行数仍为 25、E3 的重启前后行数比较不受 `demo_menu_auth` 新增行影响；验证：`bash verify.sh --stage=restart` 的 B6/E3 相关断言通过。

## 3. 页面单视图改造

- [x] 3.1 把 `index.html` 的 `S` 数组改为带 `type: 'perm'|'cap'|'script'|'test'` 的目录，加入 design D6 的 P1-P25、`covers` 原编号与 `EXPECTED_IDS` 常量；验证：页面加载时覆盖校验显示缺失 0 条。
- [x] 3.2 渲染主表：每行显示场景、判定层、菜单列、行级列、数据/结论列；未管辖显示"未管辖"、未声明显示"未声明"、查询失败显示"查询失败"，不得显示成拒绝；验证：alice/bob 对 P1、P7、P8、P13、P17 的分层结果与种子数据相符。
- [x] 3.3 实现刷新：切换身份和点击"刷新判定"时发一个批量 `/demo/permission-view` + 顺序只读 GET（P17-P25），不发送任何写请求；验证：浏览器 Network 面板只有预检 POST 与只读 GET，`demo_execution_log` 行数不变，刷新不会被 Hikari 连接池卡住。
- [x] 3.4 实现行展开依据：菜单显示"要求/持有/缺失"权限串，行级按 ID 显示"要求/持有/缺失"，数据范围显示可见行集与行数；验证：bob 的 P7 显示菜单通过、工单1行级缺失 `ROLE__10`。
- [x] 3.5 实现数据范围行的按需证据：每个 GET 带独立 `X-Trace-Id`，行内"证据"按钮再拉 `/demo/trace` 展示注入后的 SQL；验证：P17 在 alice 下能看到 `demo_auth_data` 子查询，bob 下可见行集为 1。
- [x] 3.6 实现"真实调用"预填：只把方法/路径/请求体填入自由调用面板并设为当前身份，不自动发送；验证：点击 P7 后自由调用面板被填充，未发出请求；手动发送后才产生副作用。
- [x] 3.7 实现覆盖脚注：按 `cap`/`script`/`test` 分组列出 K/S/T 行与 `where`（`verify.sh C6-C8`、`StarterAssemblyTest`、`verify.sh --stage=restart` 等），并在 `union(covers)` 缺少 `EXPECTED_IDS` 时红色告警；验证：脚注覆盖 B1b、B3、B5-B8、C6-C10、C15、C17-C21、D2、D9、D10、D11b、D12、D13/D14、E1-E3、A1-A5。
- [x] 3.8 删除旧的逐用例 `demoUser`、固定身份 `runAll` 与旧矩阵渲染代码；验证：`grep -n "demoUser\|runAll" index.html` 无逐用例身份残留（全局身份变量改名为 `identity`）。
- [x] 3.9 调整身份下拉与顶部面板：选项标注各自权限串，切换时刷新当前权限与主表；验证：alice/bob/carol/dave/erin/frank/无身份 7 种身份下，当前权限回显与主表同步变化。
- [x] 3.10 浏览器人工回归：按 design D6 的 P 表逐行核对 7 种身份，重点确认 bob 的 P7 为"菜单通过、行级拒绝"、frank 的 P6 允许、carol/无身份的 P17 为空、dave 的 P24 为 101,102、erin 的 P17-P25 为不适用；验证：结果与种子数据一致，页面无报错。
- [x] 3.11 在刷新流程中识别含单引号的权限串（预检 `auths`，失败时退回 `current-user.rawFromStore`），跳过数据范围 GET 并在行内显示"不适用"、summary 单独计数；验证：erin 下 P17-P25 显示不适用，Network 面板没有对应 GET，服务端没有 500。

## 4. 文档与脚本同步

- [x] 4.1 改写 `README.md` 4.1：身份下拉驱动整张主表、切换只跑只读判定、真实调用只预填自由调用、覆盖脚注指向脚本/测试；验证：按文档描述在浏览器操作，行为一致。
- [x] 4.2 在 `README.md` 第 6 节新增 6.1 "页面判权行与原编号的映射"（P1-P25 表），删除"页面里的用例编号与 verify.sh 完全一致"的旧说法；验证：每个原编号都能在 P 表或覆盖脚注中找到落点。
- [x] 4.3 更新 `README.md` 中 C22 的描述与"52 条用例逐条标注身份"的旧表述；验证：`grep -n "身份固定\|手工身份\|完全一致" README.md` 不再出现旧结论。
- [x] 4.4 在 `verify.sh` 增加 2-3 条只读一致性断言：`/demo/permission-view` 对 alice/bob/无身份在 P1、P7、P13 上的结论与真实调用 code 一致；验证：`bash verify.sh` 全量通过。
- [x] 4.5 更新项目记忆中的身份语义（`auth-core-demo-identity-scope-decision`）：主矩阵身份=镜头、规格断言=固定夹具、页面不写数据；验证：memory 文件正文、description 与 `MEMORY.md` 索引一致。

## 5. 集成验证

- [x] 5.1 从模块目录构建并启动：`cd sample/auth-core-boot-sample && mvn -q package -DskipTests && java -jar target/auth-core-boot-sample-0.0.1-SNAPSHOT-exec.jar`；验证：应用正常启动，`http://localhost:8082/coredemo/` 可打开。
- [x] 5.2 运行 `bash verify.sh`；验证：全部 PASS，编号与改动前一致，C22 为行级拒绝消息。
- [x] 5.3 运行 `bash verify.sh --stage=restart`，按脚本提示重启；验证：B7、E3 通过，`demo_menu_api` 仍 25 行、各表行数不因重启增长。
- [x] 5.4 破坏性检查：切换身份多次后核对 `/demo/counts` 与 `demo_auth_data` 未因切人改变；验证：权限槽位、工单数据、副作用表在仅刷新判定时保持不变。

## 6. 身份真值表与一键测试

- [x] 6.1 在页面内建立 `EXPECT` 真值表（25 个 P 行 × 7 个身份，`allow`/`menu`/`row`/`空`/`n/a`/数据ID），并把空身份归一化为 `none`；验证：7 种身份无 `undefined`、无缺失行。
- [x] 6.2 主表增加“期望（当前身份）/ 实际 / 结果”三列，结果逐条显示通过/不通过/不适用；菜单已拒绝时实际列只显示菜单拒绝，不误报行级；验证：alice/bob/carol/dave/frank/无身份 25/25 通过，erin 16/16 通过 + 9 条不适用。
- [x] 6.3 增加“测试当前身份”按钮与切换身份自动测试，汇总通过/不通过/不适用；验证：alice 按钮重测仍 25/0/0，切换身份自动更新。
- [x] 6.4 增加“当前身份能做什么（既定）”总览，按可执行 / 接口可进但数据不可操作 / 不可进入 / 可查看的数据 / 看不到的数据 / 不适用分组；验证：alice/bob/erin/无身份的 chips 与真值表一致。
- [x] 6.5 更新 README 4.1/6.1：期望来自页面真值表，结果逐条校验，一键测试；副作用/启动/规格断言仍以 `verify.sh`/测试为准，删除“页面不承担通过/不通过”的旧说法；验证：文档描述与页面行为一致。
- [x] 6.6 浏览器回归 7 种身份 + 一键按钮；验证：无写请求、`workOrder.update`/`authData.grant` 计数不变、覆盖 70/70、无 console 错误。
- [x] 6.7 更新项目记忆：主表判权期望=页面真值表，副作用/规格/启动=`verify.sh`/测试，页面不写数据；验证：memory 文件、description 与 `MEMORY.md` 索引一致。
