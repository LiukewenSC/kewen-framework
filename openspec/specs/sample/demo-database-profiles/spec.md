# sample/demo-database-profiles Specification

## Purpose

约束示例工程（sample）按 profile 选择数据库的行为：H2 内存库的可达范围、MySQL 兜底的正确性，以及建表脚本与种子数据的归属和加载顺序，使示例在不安装数据库时也能启动验证，同时保证切回 MySQL 不会破坏使用者已有的数据。

## Requirements

### Requirement: H2 配置的作用域限定在示例工程的 profile 内

示例工程选择数据库 MUST 由 Spring profile 决定。H2 的数据源、建表脚本与种子数据声明 MUST 只出现在该示例的 `h2` profile 段内，MUST NOT 出现在对任何 profile 都生效的公共配置段中。框架模块与非示例部署 MUST NOT 因本约定引入 H2。

#### Scenario: H2 声明被包在 profile 段内

- **WHEN** 检查任一示例工程的配置文件
- **THEN** 指向 H2 的数据源参数只出现在 `h2` profile 段中
- **AND** 未被 profile 包裹的公共段中不存在 H2 连接串或 H2 专属脚本声明

#### Scenario: 框架模块不感知 H2

- **WHEN** 构建框架模块自身
- **THEN** 框架模块不声明 H2 依赖
- **AND** 框架模块的配置不含 H2 连接串

### Requirement: 示例工程默认启动不依赖本地 MySQL

每个示例工程 MUST 能在未安装、未连接任何本地 MySQL 的环境下以默认 profile 启动成功；需要数据表的示例 MUST 在启动过程中自动获得所需的表。

#### Scenario: 无 MySQL 环境启动

- **WHEN** 在一台没有可用 MySQL 的机器上以默认 profile 启动任一示例工程
- **THEN** 应用启动完成并可响应其示例接口
- **AND** 不因数据源缺失或驱动缺失而启动失败

#### Scenario: 需要建表的示例自动获得表

- **WHEN** 一个使用框架数据表的示例以默认 profile 启动完成
- **THEN** 该示例所依赖的表已存在于其数据库中
- **AND** 示例接口读写这些表时不报"表不存在"

#### Scenario: 不需要数据表的示例不建表

- **WHEN** 一个业务代码不访问任何数据表的示例以默认 profile 启动
- **THEN** 启动过程中不执行任何建表脚本

### Requirement: 切回 MySQL 不破坏使用者已有数据

示例工程激活 `mysql` profile 时 MUST 只连接使用者指定的数据库，MUST NOT 在其中创建、重建或清空任何表；其连接配置 MUST 使用实际可被驱动加载的 URL 格式。

#### Scenario: 激活 mysql 后重启不丢数据

- **WHEN** 使用者在同一个 MySQL 库上多次启动并关闭 `mysql` profile 的示例
- **THEN** 库中已有的表与数据在重启后仍然存在
- **AND** 启动过程不执行带删除语义的建表脚本

#### Scenario: mysql profile 的 URL 可被驱动接受

- **WHEN** 示例以 `mysql` profile 启动并连接一个可用的 MySQL
- **THEN** 数据源 URL 使用其指定驱动能识别的前缀
- **AND** 连接建立成功

### Requirement: 框架建表脚本保持单一来源

示例工程所需的框架数据表 MUST 全部来自仓库中框架级建表脚本的同一份内容，MUST NOT 在示例工程内复制一份表结构。

#### Scenario: 框架表结构变更只需改一处

- **WHEN** 框架级建表脚本中某张表的定义被修改
- **THEN** 所有依赖该表的示例在下一次启动时获得新定义
- **AND** 无需同步修改任何示例工程内的脚本

#### Scenario: 示例内不存在框架表的重复定义

- **WHEN** 检查各示例工程自带的 SQL 内容
- **THEN** 其中不包含框架级建表脚本已覆盖的表定义

### Requirement: 示例专属结构与种子数据分文件且按序加载

示例工程专有的建表语句 MUST 与其种子数据分开存放，且建表内容 MUST 在种子数据之前完成加载；使用者 MUST 能够只导入种子数据而不重建表。

#### Scenario: 自动加载顺序正确

- **WHEN** 一个带专有表与种子数据的示例以默认 profile 启动
- **THEN** 其专有表在种子数据写入前已存在
- **AND** 种子数据写入不报"表不存在"

#### Scenario: 手动导入可只导数据

- **WHEN** 使用者在已有表结构的 MySQL 库上只想补入示例种子数据
- **THEN** 可以只执行种子数据文件而不触发任何建表或删除

### Requirement: 会话表由会话存储机制独占创建

使用 JDBC 会话存储的示例，其会话表 MUST 由会话存储机制自身的初始化流程创建，MUST NOT 同时由示例的建表脚本创建同名表。

#### Scenario: 默认启动后会话可写

- **WHEN** 一个使用 JDBC 会话存储的示例以默认 profile 启动并有用户登录
- **THEN** 会话数据可写入会话表
- **AND** 启动过程中没有第二处脚本重复创建同一张会话表

#### Scenario: 切换 MySQL 时会话表来源唯一

- **WHEN** 同一个示例以 `mysql` profile 启动
- **THEN** 会话表仍只有唯一的创建者，且遵循 MySQL 不自动重建的原则

### Requirement: 示例 SQL 变更须经双向兼容校验

示例工程新增或修改的 SQL 文件 MUST 与框架级脚本一样，同时可被 MySQL 与 H2(MySQL 兼容模式) 执行，并 MUST 被仓库现有的 SQL 兼容性检查覆盖。

#### Scenario: 新增示例脚本进入检查范围

- **WHEN** 某示例工程新增一个 SQL 文件
- **THEN** 执行仓库的 SQL 兼容性检查脚本时该文件被纳入检查
- **AND** 检查全部通过后示例方可视为完成变更
