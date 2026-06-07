# AccountBook 项目最终说明文档

本文档用于总结本项目从 Android Studio 初始项目结构逐步开发到当前版本的文件改动情况。内容按项目结构说明每个新增或重点修改文件的作用，以及文件中具体实现的功能。

## 一、项目整体说明

本项目是一个基于 Java、SQLite、SharedPreferences 和 XML 布局实现的家庭账本 Android 应用。

当前版本已经实现以下主要功能：

- 用户注册、登录、登录态保存和退出登录。
- 用户名长度限制为 1-10 位，密码长度限制为 6-12 位。
- 数据库升级到账号规则版本时，对旧数据执行一次性清空，避免历史数据与新规则不一致。
- 账单新增、编辑、列表展示和批量删除。
- 收入、支出分类管理，支持默认分类和自定义分类。
- 按类型、分类、备注、年份、月份筛选查询账单。
- 按年或按月统计收入、支出、结余和支出分类排行。
- 首页展示本期收支概览和最近账单。
- 个人中心展示当前用户、修改密码、退出登录。

## 二、根目录文档文件

### `DEVELOPMENT_GUIDE.md`

文件作用：

- 作为项目开发指导文档。
- 用于说明课程设计背景、模块划分、数据库设计、开发顺序和验收要点。

具体实现：

- 梳理项目采用的技术栈：Java、SQLite、SharedPreferences、XML 布局。
- 将家庭账本拆分为用户管理、账单管理、分类管理、查询筛选、统计分析、个人中心等模块。
- 记录每个模块的开发目标和测试方向。

### `FILE_CHANGE_LOG.md`

文件作用：

- 作为项目文件变更日志。
- 用于记录开发过程中每个新增或修改文件的用途和改动摘要。

具体实现：

- 按时间和模块记录新增 Activity、布局、数据库方法、工具类和资源文件。
- 记录后续修复内容，例如账单编辑修复、查询页拆分、日期选择器、账号规则限制等。
- 记录最终账号规则调整：不再保留运行时历史用户清理函数，仅通过数据库版本升级对旧库清空一次。

### `FINAL_PROJECT_SUMMARY.md`

文件作用：

- 当前最终说明文档。
- 用于从项目整体角度总结所有主要文件的作用和实现内容。

具体实现：

- 按 Android 项目结构说明 Java、XML、Drawable、Values、Manifest 和文档文件。
- 便于后续答辩、维护或交接时快速理解项目。

## 三、Manifest 配置文件

### `app/src/main/AndroidManifest.xml`

文件作用：

- Android 应用清单文件。
- 声明应用主题、图标、备份配置和所有 Activity。
- 指定应用启动入口。

具体实现：

- 将应用主题设置为 `@style/Theme.AccountBook`。
- 保留默认应用图标和备份配置。
- 注册以下页面：
  - `LoginActivity`
  - `RegisterActivity`
  - `MainActivity`
  - `RecordEditActivity`
  - `RecordListActivity`
  - `RecordSearchActivity`
  - `StatisticsActivity`
  - `CategoryActivity`
  - `ProfileActivity`
- 将 `LoginActivity` 设置为启动页，配置 `MAIN` 和 `LAUNCHER` intent-filter。

## 四、数据库和工具类

### `app/src/main/java/com/example/accountbook/db/DatabaseHelper.java`

文件作用：

- SQLite 数据库帮助类。
- 负责创建数据库表、数据库升级、用户数据、账单数据、分类数据和统计查询。

具体实现：

- 数据库名称为 `account_book.db`。
- 当前数据库版本为 `4`。
- 创建三张核心表：
  - `users`：保存用户 id、用户名、密码、创建时间。
  - `records`：保存账单 id、用户 id、类型、分类、金额、日期、备注、创建时间。
  - `categories`：保存分类 id、用户 id、分类名、类型、是否默认分类。
- 在 `onUpgrade()` 中处理账号规则调整：
  - 当旧版本小于 4 时，删除并重建 `categories`、`records`、`users` 三张表。
  - 这只用于本次账号长度规则变更后的旧数据清空，不作为以后每次升级都清空数据库的通用逻辑。
- 用户相关方法：
  - `isUsernameExists()`：判断用户名是否已存在。
  - `registerUser()`：注册用户并初始化默认分类。
  - `validateLogin()`：校验用户名和密码并返回用户 id。
  - `isUserExists()`：判断当前登录态中的用户是否仍存在。
  - `checkPassword()`：校验当前用户旧密码。
  - `updatePassword()`：修改用户密码。
- 分类相关方法：
  - `ensureDefaultCategories()`：为新用户或无分类用户初始化默认收支分类。
  - `addCategory()`：添加自定义分类，重复分类使用冲突忽略。
  - `getCategoriesByType()`：按收入或支出读取分类。
  - `getAllCategories()`：读取当前用户所有分类。
  - `deleteCustomCategory()`：只允许删除自定义分类，不删除默认分类。
- 账单相关方法：
  - `addRecord()`：新增账单。
  - `getRecordsByUser()`：读取当前用户全部账单。
  - `getRecentRecords()`：读取首页最近账单。
  - `getRecordById()`：编辑账单时读取单条账单。
  - `updateRecord()`：保存账单编辑结果。
  - `deleteRecord()`：删除指定账单。
- 查询和统计方法：
  - `searchRecords()`：支持按类型、分类、备注关键字、日期前缀组合查询。
  - `getMonthlyTotal()`：统计某月收入或支出总额。
  - `getPeriodTotal()`：按年份或年月统计收入或支出总额。
  - `getMonthlyCategoryTotals()`：统计某月分类支出排行。
  - `getPeriodCategoryTotals()`：按年份或年月统计分类支出排行。

### `app/src/main/java/com/example/accountbook/util/SessionManager.java`

文件作用：

- 登录态管理工具类。
- 使用 SharedPreferences 保存当前登录用户信息。

具体实现：

- 使用 `account_book_session` 作为 SharedPreferences 文件名。
- 保存字段：
  - `user_id`
  - `username`
- 提供方法：
  - `saveLogin()`：登录成功后保存用户 id 和用户名。
  - `isLoggedIn()`：判断是否存在有效登录态。
  - `getUserId()`：读取当前用户 id。
  - `getUsername()`：读取当前用户名。
  - `logout()`：清空登录态。

### `app/src/main/java/com/example/accountbook/util/AccountValidator.java`

文件作用：

- 账号规则校验工具类。
- 统一管理用户名和密码长度规则。

具体实现：

- `isUsernameValid()`：校验用户名长度为 1-10 位。
- `isPasswordValid()`：校验密码长度为 6-12 位。
- `usernameRuleText()`：返回用户名规则提示文案。
- `passwordRuleText()`：返回密码规则提示文案。
- 注册、登录、修改密码页面统一调用该工具类，避免规则散落在多个页面中。

## 五、Java 页面控制文件

### `app/src/main/java/com/example/accountbook/LoginActivity.java`

文件作用：

- 登录页面控制逻辑。
- 应用启动入口页面。

具体实现：

- 加载 `activity_login.xml`。
- 初始化 `SessionManager` 和 `DatabaseHelper`。
- 如果用户已登录：
  - 通过 `isUserExists()` 判断数据库中用户是否仍存在。
  - 用户存在则直接进入首页。
  - 用户不存在则清空登录态。
- 登录按钮逻辑：
  - 校验用户名非空。
  - 校验密码非空。
  - 校验用户名长度 1-10 位。
  - 校验密码长度 6-12 位。
  - 调用 `validateLogin()` 校验 SQLite 中的用户。
  - 登录成功后保存登录态并进入首页。
- 注册入口逻辑：
  - 点击后跳转到 `RegisterActivity`。

### `app/src/main/java/com/example/accountbook/RegisterActivity.java`

文件作用：

- 注册页面控制逻辑。
- 负责新用户创建。

具体实现：

- 加载 `activity_register.xml`。
- 读取用户名、密码、确认密码输入。
- 校验用户名非空。
- 校验密码非空。
- 校验用户名长度 1-10 位。
- 校验密码长度 6-12 位。
- 校验两次密码一致。
- 调用 `isUsernameExists()` 检查用户名重复。
- 调用 `registerUser()` 写入 SQLite。
- 注册成功后返回登录页。

### `app/src/main/java/com/example/accountbook/MainActivity.java`

文件作用：

- 应用首页控制逻辑。
- 展示账本总览、快捷记账入口、功能入口和最近账单。

具体实现：

- 加载 `activity_main.xml`。
- 进入首页时检查当前登录态用户是否仍存在。
- 用户不存在时自动退出并返回登录页。
- 绑定首页按钮：
  - 个人中心。
  - 记一笔支出。
  - 记一笔收入。
  - 账单列表。
  - 统计分析。
  - 分类管理。
  - 筛选查询。
  - 查看全部账单。
- 在 `onResume()` 中刷新首页数据。
- `refreshMonthlySummary()`：
  - 以当前月份为条件统计收入、支出和结余。
- `refreshRecentRecords()`：
  - 读取最近 3 条账单。
  - 无账单时显示空状态。
- 动态创建最近账单行，显示类型、分类、日期、备注和金额。

### `app/src/main/java/com/example/accountbook/RecordEditActivity.java`

文件作用：

- 账单新增和编辑页面控制逻辑。

具体实现：

- 加载 `activity_record_edit.xml`。
- 支持支出和收入两种账单类型。
- 支持新增模式和编辑模式：
  - 新增模式通过 `EXTRA_TYPE` 指定收入或支出。
  - 编辑模式通过 `EXTRA_RECORD_ID` 加载原账单。
- 页面字段包括：
  - 类型单选。
  - 金额。
  - 分类下拉框。
  - 日期。
  - 备注。
- 分类下拉框从 SQLite 分类表读取。
- 切换收入/支出类型时刷新对应分类。
- 日期输入框使用 `DatePickerDialog`，减少手动输入错误。
- 保存时校验：
  - 用户必须已登录。
  - 金额不能为空。
  - 金额格式必须正确。
  - 金额必须大于 0。
  - 日期格式必须为 `yyyy-MM-dd`。
- 根据模式调用 `addRecord()` 或 `updateRecord()`。

### `app/src/main/java/com/example/accountbook/RecordListActivity.java`

文件作用：

- 账单列表页面控制逻辑。
- 展示当前用户全部账单，并支持批量管理。

具体实现：

- 加载 `activity_record_list.xml`。
- 从 SQLite 读取当前用户全部账单。
- 按日期和 id 倒序显示账单。
- 无账单时显示空状态。
- 普通模式：
  - 点击账单进入编辑页面。
  - 点击新增进入记账页面。
  - 点击查询进入筛选查询页面。
- 管理模式：
  - 点击“管理”进入批量选择模式。
  - 每条账单右侧显示选择圆点。
  - 点击账单切换选中状态。
  - 点击“删除所选”弹出确认框。
  - 确认后逐条调用 `deleteRecord()` 删除。
- 动态创建账单行，显示分类、日期、备注、金额和选中标记。

### `app/src/main/java/com/example/accountbook/RecordSearchActivity.java`

文件作用：

- 独立账单筛选查询页面控制逻辑。

具体实现：

- 加载 `activity_record_search.xml`。
- 查询条件包括：
  - 账单类型。
  - 分类。
  - 年份。
  - 月份。
  - 备注关键字。
- 类型筛选支持全部、支出、收入。
- 分类筛选从 SQLite 分类表读取。
- 年份必填，必须为 4 位数字。
- 月份可为空：
  - 为空时查询全年。
  - 填写时查询指定年月。
- 月份填写时校验范围为 1-12。
- 调用 `searchRecords()` 执行组合查询。
- 查询结果支持点击进入账单编辑页。
- 重置按钮恢复默认年份、清空月份和关键字。

### `app/src/main/java/com/example/accountbook/StatisticsActivity.java`

文件作用：

- 统计分析页面控制逻辑。

具体实现：

- 加载 `activity_statistics.xml`。
- 默认显示当前年月统计。
- 支持用户输入年份和月份。
- 年份必填，必须为 4 位数字。
- 月份可为空：
  - 为空时按全年统计。
  - 填写时按指定年月统计。
- 调用 `getPeriodTotal()` 统计收入和支出。
- 计算结余：收入减支出。
- 调用 `getPeriodCategoryTotals()` 统计支出分类排行。
- 动态创建分类排行项，显示分类名称、金额和占比。
- 无支出数据时显示空状态。

### `app/src/main/java/com/example/accountbook/CategoryActivity.java`

文件作用：

- 分类管理页面控制逻辑。
- 管理收入和支出分类。

具体实现：

- 加载 `activity_category.xml`。
- 支持切换支出分类和收入分类。
- 从 SQLite 读取当前用户对应类型分类。
- 支持添加自定义分类。
- 添加时校验分类名称不能为空。
- 添加重复分类时提示已存在。
- 默认分类显示“默认”，不允许删除。
- 自定义分类显示“删除”，点击后弹出确认框。
- 删除自定义分类时调用 `deleteCustomCategory()`。

### `app/src/main/java/com/example/accountbook/ProfileActivity.java`

文件作用：

- 个人中心页面控制逻辑。
- 展示当前用户信息，支持修改密码和退出登录。

具体实现：

- 加载 `activity_profile.xml`。
- 显示当前登录用户名。
- 修改密码逻辑：
  - 校验旧密码和新密码非空。
  - 校验两次新密码一致。
  - 校验新密码长度 6-12 位。
  - 校验新密码不能与旧密码相同。
  - 调用 `checkPassword()` 校验旧密码。
  - 调用 `updatePassword()` 保存新密码。
  - 修改成功后清空输入框。
- 退出登录逻辑：
  - 调用 `SessionManager.logout()` 清空登录态。
  - 使用 `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` 返回登录页，清空返回栈。

## 六、布局文件

### `app/src/main/res/layout/activity_login.xml`

文件作用：

- 登录页面布局。

具体实现：

- 使用 `ScrollView` 作为根布局，适配不同屏幕高度。
- 使用统一页面背景 `bg_page`。
- 展示应用标题和说明。
- 提供用户名输入框、密码输入框。
- 提供登录按钮和跳转注册入口。
- 输入框使用统一背景 `bg_input`，页面卡片使用 `bg_card`。

### `app/src/main/res/layout/activity_register.xml`

文件作用：

- 注册页面布局。

具体实现：

- 提供返回登录入口。
- 展示创建账号标题。
- 展示账号规则提示：用户名 1-10 位，密码 6-12 位。
- 提供用户名、密码、确认密码输入框。
- 提供注册按钮。
- 使用统一背景、卡片、输入框和主按钮样式。

### `app/src/main/res/layout/activity_main.xml`

文件作用：

- 首页布局。

具体实现：

- 展示应用标题、当前统计月份和个人中心入口。
- 展示本月结余、收入、支出。
- 提供快捷记账按钮：
  - 记一笔支出。
  - 记一笔收入。
- 提供功能入口：
  - 账单列表。
  - 统计分析。
  - 分类管理。
  - 筛选查询。
- 展示最近账单区域和查看全部入口。

### `app/src/main/res/layout/activity_record_edit.xml`

文件作用：

- 账单新增/编辑页面布局。

具体实现：

- 提供返回入口。
- 提供收入/支出类型切换。
- 提供金额输入框。
- 提供分类下拉框。
- 提供日期输入框。
- 提供备注输入框。
- 提供保存按钮。
- 日期输入框配合 Java 中的 `DatePickerDialog` 使用。

### `app/src/main/res/layout/activity_record_list.xml`

文件作用：

- 账单列表页面布局。

具体实现：

- 提供返回、新增、查询、管理按钮。
- 提供批量删除按钮，默认隐藏。
- 提供空状态提示。
- 提供账单列表容器，由 Java 动态添加账单行。

### `app/src/main/res/layout/activity_record_search.xml`

文件作用：

- 筛选查询页面布局。

具体实现：

- 提供返回入口。
- 提供类型下拉框和分类下拉框。
- 提供年份输入框和月份输入框。
- 提供备注关键字输入框。
- 提供查询和重置按钮。
- 提供空状态提示。
- 提供查询结果容器，由 Java 动态添加结果行。

### `app/src/main/res/layout/activity_statistics.xml`

文件作用：

- 统计分析页面布局。

具体实现：

- 提供返回入口。
- 提供年份和月份输入框。
- 提供查看统计按钮。
- 展示结余、收入、支出统计卡片。
- 展示支出分类排行标题。
- 提供空状态提示。
- 提供分类排行容器，由 Java 动态添加统计项。

### `app/src/main/res/layout/activity_category.xml`

文件作用：

- 分类管理页面布局。

具体实现：

- 提供返回入口。
- 提供收入/支出分类切换。
- 提供分类名称输入框。
- 提供添加分类按钮。
- 提供分类列表容器，由 Java 动态添加分类行。

### `app/src/main/res/layout/activity_profile.xml`

文件作用：

- 个人中心页面布局。

具体实现：

- 提供返回入口。
- 显示当前用户名。
- 提供旧密码、新密码、确认密码输入框。
- 提供修改密码按钮。
- 提供退出登录按钮。
- 提供关于说明区域。

## 七、Drawable 样式资源

### `app/src/main/res/drawable/bg_page.xml`

文件作用：

- 页面统一背景。

具体实现：

- 提供浅色页面背景，供登录、注册、首页、列表等页面根布局使用。

### `app/src/main/res/drawable/bg_card.xml`

文件作用：

- 卡片背景样式。

具体实现：

- 设置白色背景、浅色边框和圆角。
- 用于首页统计卡片、列表项、表单容器和空状态区域。

### `app/src/main/res/drawable/bg_input.xml`

文件作用：

- 输入框背景样式。

具体实现：

- 设置白底、浅色边框、圆角和内部 padding。
- 用于 EditText 和部分 Spinner。

### `app/src/main/res/drawable/bg_primary_button.xml`

文件作用：

- 主按钮背景样式。

具体实现：

- 设置主色实心背景和圆角。
- 用于登录、注册、保存、查询、添加等主要操作按钮。

### `app/src/main/res/drawable/bg_outline_button.xml`

文件作用：

- 次级按钮背景样式。

具体实现：

- 设置白底、主色描边和圆角。
- 用于次级操作按钮，例如重置、收入快捷入口等。

### `app/src/main/res/drawable/bg_income_badge.xml`

文件作用：

- 收入标签背景。

具体实现：

- 用于首页最近账单中的收入标记。

### `app/src/main/res/drawable/bg_expense_badge.xml`

文件作用：

- 支出标签背景。

具体实现：

- 用于首页最近账单中的支出标记。

### `app/src/main/res/drawable/bg_select_circle_off.xml`

文件作用：

- 批量管理模式下未选中圆点背景。

具体实现：

- 使用白底和描边表示未选择状态。

### `app/src/main/res/drawable/bg_select_circle_on.xml`

文件作用：

- 批量管理模式下已选中圆点背景。

具体实现：

- 使用主色实心圆表示已选择状态。

### `app/src/main/res/drawable/ic_launcher_background.xml`

文件作用：

- 默认启动图标背景资源。

具体实现：

- 保留 Android Studio 初始项目生成的图标背景配置。

### `app/src/main/res/drawable/ic_launcher_foreground.xml`

文件作用：

- 默认启动图标前景资源。

具体实现：

- 保留 Android Studio 初始项目生成的图标前景配置。

## 八、Values 资源文件

### `app/src/main/res/values/colors.xml`

文件作用：

- 管理项目通用颜色资源。

具体实现：

- 保留 `black` 和 `white`。
- 新增页面背景色、卡片色、主色、强调色。
- 新增收入色、支出色。
- 新增正文、次级文字和分割线颜色。
- 所有页面统一引用这些颜色，保证界面风格一致。

### `app/src/main/res/values/strings.xml`

文件作用：

- 管理字符串资源。

具体实现：

- 保存应用名称 `app_name`。
- 当前应用名称为 `AccountBook`。

### `app/src/main/res/values/themes.xml`

文件作用：

- 管理应用主题。

具体实现：

- 定义 `Theme.AccountBook`。
- 为整个应用提供统一主题基础。

### `app/src/main/res/values-night/themes.xml`

文件作用：

- 夜间模式主题配置。

具体实现：

- 保留 Android Studio 初始项目生成的夜间主题结构。

## 九、XML 配置和启动图标资源

### `app/src/main/res/xml/backup_rules.xml`

文件作用：

- Android 自动备份规则配置。

具体实现：

- 保留初始项目生成配置。
- 当前项目未针对备份做额外业务规则。

### `app/src/main/res/xml/data_extraction_rules.xml`

文件作用：

- Android 数据提取规则配置。

具体实现：

- 保留初始项目生成配置。
- 当前项目未针对数据迁移或备份做额外规则。

### `app/src/main/res/mipmap-*`

文件作用：

- 应用启动图标资源。

具体实现：

- 保留 Android Studio 初始项目生成的不同分辨率图标。
- Manifest 中通过 `@mipmap/ic_launcher` 和 `@mipmap/ic_launcher_round` 引用。

### `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

文件作用：

- Android 8.0 及以上自适应图标配置。

具体实现：

- 保留初始项目生成的自适应图标结构。

### `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

文件作用：

- Android 8.0 及以上圆形自适应图标配置。

具体实现：

- 保留初始项目生成的圆形图标结构。

## 十、当前项目结构总结

当前项目从初始空白 Android 项目扩展为完整的家庭账本应用，核心结构如下：

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/accountbook/
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── MainActivity.java
│   ├── RecordEditActivity.java
│   ├── RecordListActivity.java
│   ├── RecordSearchActivity.java
│   ├── StatisticsActivity.java
│   ├── CategoryActivity.java
│   ├── ProfileActivity.java
│   ├── db/DatabaseHelper.java
│   └── util/
│       ├── SessionManager.java
│       └── AccountValidator.java
└── res/
    ├── layout/
    ├── drawable/
    ├── values/
    ├── values-night/
    ├── xml/
    └── mipmap*/
```

核心实现关系：

- `LoginActivity` 和 `RegisterActivity` 负责用户入口。
- `SessionManager` 负责登录态。
- `AccountValidator` 负责账号规则。
- `DatabaseHelper` 负责所有 SQLite 数据。
- `MainActivity` 负责首页总览和模块入口。
- `RecordEditActivity`、`RecordListActivity`、`RecordSearchActivity` 负责账单管理和查询。
- `StatisticsActivity` 负责统计分析。
- `CategoryActivity` 负责分类管理。
- `ProfileActivity` 负责个人中心和密码修改。
- XML 布局文件负责页面结构。
- Drawable 和 Values 资源负责统一视觉样式。

## 十一、最终验证情况

本机测试结果：

- 用户已确认本机运行测试无误。

静态检查结果：

- Java 字符串闭合扫描通过。
- XML 资源解析通过。
- 无 `deleteInvalidUsers()` 旧清理函数残留。
- 无 `showComingSoon()` 临时功能入口残留。
- 无 `待接入`、`TODO`、`FIXME` 等未完成业务入口残留。

当前版本可以作为课程项目最终版本提交。
