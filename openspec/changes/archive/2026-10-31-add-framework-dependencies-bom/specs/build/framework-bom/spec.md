# Spec Delta

## Purpose

约束框架依赖版本表以独立可 import 的 Maven BOM 对外交付，使外部项目在不把 `cu-parent` 设为自己 parent 的前提下也能免版本号引入框架，并明确该方式不自带的构建前提由谁负责。

## ADDED Requirements

### Requirement: 依赖版本表可独立引入

框架 SHALL 提供一个单一的 BOM 坐标，外部项目 import 该 BOM 后引用框架自身模块及受管第三方库时 MUST 无需声明 `<version>`，且 MUST NOT 因此被迫占用自己的 `<parent>` 位置。

#### Scenario: 免继承引入框架 starter

- **WHEN** 一个 `<parent>` 为 `spring-boot-starter-parent` 的外部项目 import 框架 BOM，并声明任一框架 starter 坐标而不写版本
- **THEN** 依赖解析成功，该 starter 及其框架内传递依赖都取到 BOM 中的版本

#### Scenario: 上游版本矩阵随 BOM 一并生效

- **WHEN** 同一外部项目声明由 spring-boot / spring-cloud / spring-cloud-alibaba BOM 管理的依赖而不写版本
- **THEN** 解析成功并取到框架 BOM 所对齐的上游版本

#### Scenario: 外部项目的 parent 选择权不受影响

- **WHEN** 外部项目已把 `cu-parent` 之外的任意 POM 设为 parent
- **THEN** 该项目的 `<parent>` 保持不变，仍可获得框架的版本表

### Requirement: 版本表单一事实源

框架自身的依赖版本表 SHALL 只存在于一处：`cu-parent` 与外部消费方 MUST 通过同一 BOM 获得版本，框架内部模块声明依赖时不写版本，MUST NOT 在 BOM 之外再维护一份可能漂移的同坐标版本。

#### Scenario: 框架内模块免版本声明

- **WHEN** 框架任一模块声明受管依赖（框架内坐标或第三方坐标）而不写 `<version>`
- **THEN** 构建成功，实际版本与 BOM 中的声明一致

#### Scenario: 同坐标不在他处重复定版

- **WHEN** 检查仓库内任一 POM 的 `<dependencyManagement>`
- **THEN** 除 BOM 外不再出现为同一坐标指定版本的条目

### Requirement: BOM 只交付版本不交付构建配置

BOM MUST NOT 携带 `<build>`、`<plugins>`、`<repositories>` 或直接影响编译行为的配置；因此不继承 `cu-parent` 的消费方 SHALL 自备框架无法经由 BOM 传递的构建前提，且这些前提 MUST 在接入文档中显式给出。

#### Scenario: 私有仓库来源需消费方自备

- **WHEN** 外部项目只 import BOM 而未声明 shibboleth 仓库，却引入了以 compile 作用域传递 OpenSAML 的 `login-security-spring-boot-starter`，且其本地仓库中没有这些构件的缓存
- **THEN** 该传递依赖解析失败（OpenSAML 只发布在 Shibboleth 仓库，Maven Central 上没有对应版本），且接入文档已给出需要补上的仓库声明

#### Scenario: 本地缓存掩盖缺失的仓库声明

- **WHEN** 同一项目在未声明 shibboleth 仓库的情况下解析，但本地仓库已有 OpenSAML 构件缓存
- **THEN** 解析仍然成功，因此不能据此判定仓库声明多余；该项只在干净仓库上可复现

#### Scenario: 编译参数由消费方的 parent 或文档补足

- **WHEN** 外部项目的 parent 未开启 `-parameters` 编译参数
- **THEN** 接入文档已说明需自行开启，否则不写名称的 `@RequestParam` 等注解在运行期无法解析参数名

### Requirement: BOM 只收录可解析的对外坐标

BOM 中声明的每个框架坐标 MUST 能被已 import BOM 的外部项目解析；不参与对外供给的模块 MUST NOT 出现在 BOM 中。

#### Scenario: 示例模块不入 BOM

- **WHEN** 检查 BOM 的 `<dependencyManagement>`
- **THEN** 其中不含任何 `sample` 模块坐标

#### Scenario: 未安装的模块不入 BOM

- **WHEN** 某框架模块不参与安装到本地或远程仓库（如代码生成器）
- **THEN** BOM 不为其声明版本坐标，避免给出解析不到的依赖
