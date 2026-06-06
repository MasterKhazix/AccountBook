# 项目文件修改记录

本文档用于记录本项目中每个被修改或新增文件的用途、修改内容和后续维护说明。以后每次修改文件，都需要同步更新本文件。

## 记录规则

- 每个被修改或新增的文件都要有一条记录。
- 记录内容保持简洁，说明“文件作用”和“本次大致修改”即可。
- 如果后续继续修改同一个文件，在对应条目下追加“后续修改记录”。
- 不在代码文件里写大量解释，详细说明统一放到本文档。

## 当前已修改或新增文件

### `DEVELOPMENT_GUIDE.md`

文件作用：

- 家庭账本 App 后续开发总指引。
- 根据课程大作业要求整理功能模块、页面模块、数据库设计、开发顺序、报告章节和验收清单。

本次修改：

- 新增该文件。
- 明确当前项目采用 Java、SQLite、SharedPreferences、XML 布局等课程技术。
- 将“家庭账本”选题拆成用户管理、账单管理、查询筛选、统计分析、分类管理、个人中心等模块。

后续修改记录：

- 暂无。

### `FILE_CHANGE_LOG.md`

文件作用：

- 记录项目中每个修改文件的用途和修改摘要。
- 作为后续开发时的文件维护日志。

本次修改：

- 新增该文件。
- 记录目前已经新增或修改过的文件。

后续修改记录：

- 追加记录登录页、注册页、输入框背景、Manifest 启动页调整等文件变更。

### `app/src/main/AndroidManifest.xml`

文件作用：

- Android 应用清单文件。
- 负责声明 Activity、应用主题、启动入口等核心配置。

本次修改：

- 将 App 启动入口从 `MainActivity` 调整为 `LoginActivity`。
- 新增注册页 `RegisterActivity` 声明。
- 保留 `MainActivity`，作为登录成功后的首页。

后续修改记录：

- 暂无。

### `app/src/main/java/com/example/accountbook/LoginActivity.java`

文件作用：

- 登录页面控制逻辑。
- 当前负责用户名、密码输入校验，以及登录成功后跳转首页。

本次修改：

- 新增该文件。
- 加载 `activity_login.xml`。
- 实现用户名和密码非空校验。
- 登录按钮目前先做临时校验，后续会替换为 SQLite 用户表校验。
- “没有账号？去注册”按钮跳转到 `RegisterActivity`。

后续修改记录：

- 暂无。

### `app/src/main/java/com/example/accountbook/RegisterActivity.java`

文件作用：

- 注册页面控制逻辑。
- 当前负责用户名、密码、确认密码输入校验。

本次修改：

- 新增该文件。
- 加载 `activity_register.xml`。
- 实现用户名非空、密码非空、两次密码一致校验。
- 注册成功后暂时返回登录页，后续会接入 SQLite 保存用户。

后续修改记录：

- 暂无。

### `app/src/main/java/com/example/accountbook/MainActivity.java`

文件作用：

- App 当前主页面的 Java 控制逻辑。
- 负责加载 `activity_main.xml`，处理系统栏边距，并绑定首页按钮点击事件。

本次修改：

- 保留原来的 `EdgeToEdge` 和系统栏适配逻辑。
- 新增首页按钮点击绑定方法 `bindHomeActions()`。
- 新增临时 Toast 提示方法 `showComingSoon()`。
- 当前按钮只做交互反馈，后续再跳转到具体 Activity 或接入数据库功能。

后续修改记录：

- 暂无。

### `app/src/main/res/layout/activity_main.xml`

文件作用：

- App 首页界面布局文件。
- 当前用于展示家庭账本首页，包括账单总览、快捷记账、功能入口和最近账单。

本次修改：

- 删除默认 `Hello World` 页面。
- 新增可滚动首页结构。
- 增加本月结余、收入、支出展示区域。
- 增加“记一笔支出”“记一笔收入”快捷按钮。
- 增加账单列表、统计分析、分类管理、筛选查询入口。
- 增加最近账单静态预览。

后续修改记录：

- 暂无。

### `app/src/main/res/layout/activity_login.xml`

文件作用：

- 登录页面布局文件。
- 展示用户名输入框、密码输入框、登录按钮和注册链接。

本次修改：

- 新增该文件。
- 使用与首页一致的背景、卡片和按钮风格。
- 预留登录表单，后续接入数据库登录校验。

后续修改记录：

- 暂无。

### `app/src/main/res/layout/activity_register.xml`

文件作用：

- 注册页面布局文件。
- 展示用户名、密码、确认密码输入框和注册按钮。

本次修改：

- 新增该文件。
- 使用与首页一致的背景、卡片和按钮风格。
- 提供返回登录入口。
- 预留注册表单，后续接入数据库保存用户。

后续修改记录：

- 暂无。

### `app/src/main/res/values/colors.xml`

文件作用：

- 管理项目中复用的颜色资源。
- 供布局、按钮、卡片、文字等 UI 元素引用。

本次修改：

- 保留默认 `black` 和 `white`。
- 新增页面背景色、卡片色、主色、强调色、收入色、支出色、正文色、次要文字色和分割线颜色。

后续修改记录：

- 暂无。

### `app/src/main/res/values/strings.xml`

文件作用：

- 管理项目中复用的字符串资源。
- 当前主要保存应用名称 `app_name`。

本次修改：

- 曾临时改为“家庭账本”。
- 根据批注要求，已恢复为 `AccountBook`。

后续修改记录：

- 暂无。

### `app/src/main/res/drawable/bg_page.xml`

文件作用：

- 首页整体背景资源。

本次修改：

- 新增浅色页面背景，供首页根布局使用。

后续修改记录：

- 暂无。

### `app/src/main/res/drawable/bg_input.xml`

文件作用：

- 表单输入框背景资源。
- 当前用于登录页和注册页的 EditText。

本次修改：

- 新增白色输入框背景。
- 设置 8dp 圆角、浅色边框和内部 padding。

后续修改记录：

- 暂无。

### `app/src/main/res/drawable/bg_card.xml`

文件作用：

- 首页卡片背景资源。
- 用于总览卡片、功能入口卡片、最近账单卡片等。

本次修改：

- 新增白色卡片背景。
- 设置 8dp 圆角和浅色边框。

后续修改记录：

- 暂无。

### `app/src/main/res/drawable/bg_primary_button.xml`

文件作用：

- 主操作按钮背景资源。
- 当前用于“记一笔支出”按钮。

本次修改：

- 新增绿色实心按钮背景。
- 设置 8dp 圆角。

后续修改记录：

- 暂无。

### `app/src/main/res/drawable/bg_outline_button.xml`

文件作用：

- 次级操作按钮背景资源。
- 当前用于“记一笔收入”按钮。

本次修改：

- 新增白底描边按钮背景。
- 设置 8dp 圆角和绿色边框。

后续修改记录：

- 暂无。

### `app/src/main/res/drawable/bg_income_badge.xml`

文件作用：

- 收入标签背景资源。
- 当前用于最近账单中的“收入”标记。

本次修改：

- 新增浅绿色标签背景。
- 设置 6dp 圆角。

后续修改记录：

- 暂无。

### `app/src/main/res/drawable/bg_expense_badge.xml`

文件作用：

- 支出标签背景资源。
- 当前用于最近账单中的“支出”标记。

本次修改：

- 新增浅红色标签背景。
- 设置 6dp 圆角。

后续修改记录：

- 暂无。
