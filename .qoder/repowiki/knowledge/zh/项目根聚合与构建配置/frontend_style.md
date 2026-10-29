## 1. 使用的系统/方法

本仓库是一个 Spring Boot 多模块 Java 框架工程，**不包含任何前端构建工具链**（未检出 `package.json`、`tailwind.config.*`、Vue/React 源码等）。唯一的 UI 代码位于 `sample/login-boot-sample/src/main/resources/static/`，由静态资源直接托管：

- HTML：`index.html`（首页）、`login.html`（登录页）
- CSS：`css/login.css`
- JS：`js/index.js`（通过 `<script src="/js/index.js">` 引入）

样式方案是 **原生 CSS + CSS Custom Properties（`:root` 变量）**，没有使用任何第三方组件库或 CSS 预处理器。布局主要依赖 Flexbox 与少量 CSS Grid（`.profile`），并采用移动端优先的响应式断点（`viewport` meta 已声明，但无媒体查询；页面本身以固定最大宽度居中为主）。

## 2. 关键文件

- `sample/login-boot-sample/src/main/resources/static/css/login.css` — 全部样式定义
- `sample/login-boot-sample/src/main/resources/static/index.html` — 示例首页入口
- `sample/login-boot-sample/src/main/resources/static/js/index.js` — 登录态校验与跳转逻辑（仅 JS，不涉及样式）

其他 sample 模块（如 `auth-core-boot-sample`、`basic-boot-sample`）仅包含后端样板代码，未发现 CSS/HTML 资源。

## 3. 架构与约定

### 设计令牌（Design Tokens）
所有视觉变量集中在 `:root` 中，作为单一事实来源：

| Token | 值 | 用途 |
|---|---:|---|
| `--border` | `#e3e6eb` | 边框、分割线 |
| `--text` | `#1f2430` | 正文文本色 |
| `--muted` | `#6b7280` | 次要文字、提示 |
| `--accent` | `#2f6fed` | 主色调（按钮、focus、hover） |
| `--danger` | `#d33a3a` | 错误状态 |
| `--notice` | `#f2f6ff` | 信息提示背景 |

### 全局重置与排版
- `* { box-sizing: border-box; }` 作为唯一 reset
- 字体栈：`-apple-system, "Segoe UI", "Microsoft YaHei", Arial, sans-serif`，字号基准 `15px`，行高 `1.6`
- 页面背景 `#f5f6f8`，内容区 `margin: 48px 16px`

### 组件类（BEM 风格命名，非严格 BEM）
- `.card` — 卡片容器（`max-width: 420px`，圆角 10px）
- `.title` / `.subtitle` — 标题与副标题
- `.field` + `.field label/.field input` — 表单字段组合
- `.primary` — 主操作按钮（也用于 `<a>` 链接时自行设置 `display:block;text-align:center`，见注释）
- `.ghost` — 幽灵按钮（空背景、描边）
- `.notice` / `.error` / `.hint` — 反馈文案块
- `.divider` — 带横线的分隔条（伪元素实现）
- `.sso-list` / `.sso-button` — SSO 登录按钮列表
- `.profile` — 使用 CSS Grid（`grid-template-columns: 72px 1fr`）展示用户信息
- `.actions` — 垂直排列的操作区

### 交互约定
- 输入框 focus 时边框颜色切换为 `var(--accent)`，不显示 outline
- 禁用态统一用 `opacity: .6; cursor: progress`（`.primary:disabled`、`.ghost:disabled`）
- 链接复用 `.primary` 时需显式加 `.link` 以获得块级居中效果（代码注释明确说明）

## 4. 约定与约束

- **无构建流程**：CSS 源文件即为最终产物，不经 PostCSS/Sass/Less 编译，也不存在任何打包配置。
- **无组件库**：未引入 Bootstrap、Ant Design、Element 等任何 UI 库，所有样式手写。
- **设计令牌集中管理**：新增颜色必须添加到 `:root` 中的变量，禁止在规则内硬编码颜色字面量（当前所有颜色均通过 `var(--*)` 引用，该模式在当前文件中一致且可视为观察到的约定）。
- **命名风格**：类名采用语义化短词（`.card`、`.primary`、`.ghost`），未使用 CSS Modules、Scoped CSS 或 BEM 双下划线/三下划线语法；冲突风险由单页面作用域承担。
- **响应式策略**：通过 viewport meta 启用缩放，但未编写任何媒体查询；页面以桌面端居中卡片为主，小屏上仅靠 `padding` 和 `max-width` 自适应。
- **国际化**：HTML `lang="zh-CN"`，字体栈显式包含 `Microsoft YaHei`，面向中文环境。

## 结论

该仓库的前端样式范围仅限于登录示例页，是一套轻量、自给自足的 CSS + CSS Variables 方案，适合作为 SSO 登录/主页的参考实现，不具备跨模块复用的设计系统能力。