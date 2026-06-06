# 家庭账本 Android 大作业开发傻瓜式指引

本文档根据《移动终端程序设计》结课报告要求整理，用来指导当前项目 `AccountBook` 的后续开发。当前项目基础信息如下：

- 项目包名：`com.example.accountbook`
- 开发语言：Java
- 最低 SDK：API 24
- 运行要求：Android 8.0 及以上更合适，真机或 Android Studio 模拟器均可
- 当前技术方向：Android SDK + Java + XML 布局 + Activity + SQLite + SharedPreferences

## 1. 作业要求转成开发目标

这次大作业不能只做一个简单页面，也不能只做单表增删改查。评分重点要求：

- 多个功能模块，业务逻辑清晰。
- 多页面交互，页面跳转正常。
- 界面简洁友好，布局合理，风格统一。
- 数据增删改查完整，输入验证合理。
- 使用 Android 课程技术，如 Activity、XML 布局、事件处理、SQLite、SharedPreferences、Intent 等。
- 项目必须能正常启动、运行流畅、核心功能不崩溃。
- 最终报告要能对应代码、功能、数据库、测试用例。

所以本项目建议做成一款“家庭账本 App”，目标是：用户登录后可以记录家庭收入和支出，按分类查看账单，查看月度统计，并维护个人信息。

## 2. 最终必须完成的功能模块

建议至少完成以下 6 个模块。按这个范围做，既不会太简单，也方便写报告。

### 2.1 用户管理模块

必须做：

- 用户注册
- 用户登录
- 退出登录
- 修改密码
- 登录状态保存

建议技术：

- 用户数据保存到 SQLite 的 `users` 表。
- 登录状态保存到 SharedPreferences。
- 登录成功后跳转到首页。
- 未登录时打开 App 先进入登录页。

验收标准：

- 用户名为空、密码为空时有提示。
- 注册时重复用户名不能再次注册。
- 登录账号密码错误时有提示。
- 修改密码后旧密码不能继续登录。

### 2.2 账单记录模块

必须做：

- 添加收入账单
- 添加支出账单
- 编辑账单
- 删除账单
- 查看账单详情

每条账单至少包含：

- 金额
- 类型：收入 / 支出
- 分类：餐饮、交通、购物、工资、生活缴费、其他等
- 日期
- 备注
- 所属用户

建议技术：

- 账单数据保存到 SQLite 的 `records` 表。
- 首页或账单列表使用 RecyclerView 展示。
- 添加和编辑可以使用同一个 Activity，通过 Intent 传入账单 id 区分模式。

验收标准：

- 金额不能为空，且必须大于 0。
- 分类不能为空。
- 添加成功后列表立即能看到。
- 编辑后数据正确更新。
- 删除前弹出确认框。

### 2.3 账单查询模块

必须做：

- 按收入 / 支出筛选
- 按分类筛选
- 按日期或月份筛选
- 按关键字搜索备注

建议技术：

- 查询条件通过 SQLite `WHERE` 语句实现。
- 页面顶部放筛选控件，下方用 RecyclerView 展示结果。

验收标准：

- 不选条件时显示全部账单。
- 选择支出只显示支出。
- 选择某个月只显示该月数据。
- 搜索不存在内容时显示空状态提示。

### 2.4 统计分析模块

必须做：

- 显示本月收入总额
- 显示本月支出总额
- 显示本月结余
- 显示不同支出分类的金额汇总

建议做：

- 首页显示简洁的总览卡片。
- 统计页用列表或简单进度条展示分类占比。
- 不强制画复杂图表，能清楚展示统计结果即可。

建议技术：

- SQLite 使用 `SUM`、`GROUP BY` 统计。
- 统计逻辑写在数据库帮助类中，Activity 只负责展示。

验收标准：

- 新增一条支出后，总支出会变化。
- 新增一条收入后，总收入会变化。
- 删除账单后统计会同步变化。

### 2.5 分类管理模块

建议做，能提高“非简单单表操作”的评分：

- 查看默认分类
- 新增自定义分类
- 删除自定义分类

分类至少包含：

- 分类名称
- 分类类型：收入 / 支出
- 所属用户

建议技术：

- 分类保存到 SQLite 的 `categories` 表。
- 首次注册或首次进入时初始化默认分类。

验收标准：

- 添加账单时能选择分类。
- 新增分类后，添加账单页面能看到。
- 分类名称为空不能保存。

### 2.6 个人中心模块

必须或建议做：

- 显示当前用户名
- 修改密码入口
- 清除当前登录状态
- 关于 App 页面

验收标准：

- 点击退出登录后返回登录页。
- 重新打开 App 时，如果已退出，不能直接进入首页。

## 3. 建议页面清单

至少做以下页面。页面越清楚，报告越好写。

| 页面 | Activity 建议名称 | 主要功能 |
| --- | --- | --- |
| 登录页 | `LoginActivity` | 输入账号密码、登录、跳转注册 |
| 注册页 | `RegisterActivity` | 创建账号 |
| 首页 | `MainActivity` | 展示本月收入、支出、结余和主要入口 |
| 账单列表页 | `RecordListActivity` | 展示、筛选、搜索账单 |
| 添加/编辑账单页 | `RecordEditActivity` | 添加或修改账单 |
| 账单详情页 | `RecordDetailActivity` | 查看详情、删除入口 |
| 统计页 | `StatisticsActivity` | 月度统计、分类统计 |
| 分类管理页 | `CategoryActivity` | 分类增删查 |
| 个人中心页 | `ProfileActivity` | 修改密码、退出登录、关于 App |

如果时间紧，最低完成：

1. 登录页
2. 注册页
3. 首页
4. 账单列表页
5. 添加/编辑账单页
6. 统计页
7. 个人中心页

## 4. 建议数据库设计

数据库名建议：`account_book.db`

数据库帮助类建议：`DatabaseHelper.java`

### 4.1 用户表 `users`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | 用户 id |
| `username` | TEXT UNIQUE NOT NULL | 用户名 |
| `password` | TEXT NOT NULL | 密码 |
| `created_at` | TEXT | 创建时间 |

### 4.2 分类表 `categories`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | 分类 id |
| `user_id` | INTEGER | 所属用户 id |
| `name` | TEXT NOT NULL | 分类名称 |
| `type` | TEXT NOT NULL | `income` 或 `expense` |
| `is_default` | INTEGER | 是否默认分类，1 是，0 否 |

### 4.3 账单表 `records`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | 账单 id |
| `user_id` | INTEGER NOT NULL | 所属用户 id |
| `category_id` | INTEGER | 分类 id |
| `type` | TEXT NOT NULL | `income` 或 `expense` |
| `amount` | REAL NOT NULL | 金额 |
| `record_date` | TEXT NOT NULL | 日期，格式如 `2026-06-06` |
| `note` | TEXT | 备注 |
| `created_at` | TEXT | 创建时间 |

## 5. 推荐代码目录结构

当前项目是 Java，可以按下面结构放文件：

```text
app/src/main/java/com/example/accountbook/
  MainActivity.java
  LoginActivity.java
  RegisterActivity.java
  RecordListActivity.java
  RecordEditActivity.java
  RecordDetailActivity.java
  StatisticsActivity.java
  CategoryActivity.java
  ProfileActivity.java

  db/
    DatabaseHelper.java

  model/
    User.java
    Record.java
    Category.java

  adapter/
    RecordAdapter.java
    CategoryAdapter.java

  util/
    SessionManager.java
    DateUtils.java
```

XML 布局建议：

```text
app/src/main/res/layout/
  activity_login.xml
  activity_register.xml
  activity_main.xml
  activity_record_list.xml
  activity_record_edit.xml
  activity_record_detail.xml
  activity_statistics.xml
  activity_category.xml
  activity_profile.xml
  item_record.xml
  item_category.xml
```

## 6. 开发顺序

按照下面顺序开发，最不容易乱。

### 第 1 步：先搭页面跳转

先不写数据库，先把这些页面建出来：

- 登录页
- 注册页
- 首页
- 账单列表页
- 添加账单页
- 统计页
- 个人中心页

每个页面先放标题和按钮，确认点击按钮能正常跳转。

完成标准：

- App 能启动。
- 每个 Activity 都已在 `AndroidManifest.xml` 注册。
- 所有按钮点击不崩溃。

### 第 2 步：做用户注册登录

先建 `DatabaseHelper.java` 和 `users` 表。

实现：

- 注册用户
- 检查用户名是否重复
- 登录校验
- SharedPreferences 保存 `user_id` 和 `username`

完成标准：

- 注册成功后可以登录。
- 登录成功进入首页。
- 退出登录后回到登录页。

### 第 3 步：做账单增删改查

建 `records` 表和 `categories` 表。

实现：

- 添加账单
- 查询当前用户账单
- RecyclerView 展示账单
- 编辑账单
- 删除账单

完成标准：

- 能新增 3 条以上账单。
- 列表能显示金额、类型、分类、日期。
- 编辑和删除后列表刷新正确。

### 第 4 步：做筛选和搜索

实现：

- 类型筛选
- 分类筛选
- 月份筛选
- 备注关键字搜索

完成标准：

- 每个筛选条件单独可用。
- 条件组合后不崩溃。
- 没有数据时显示“暂无账单”。

### 第 5 步：做统计页

实现：

- 本月收入
- 本月支出
- 本月结余
- 分类支出汇总

完成标准：

- 统计结果和账单列表数据能对上。
- 添加、删除账单后统计结果会变化。

### 第 6 步：统一界面

统一处理：

- 页面标题
- 按钮颜色
- 输入框风格
- 列表 item 间距
- 空数据提示
- 错误提示

完成标准：

- 界面没有控件重叠。
- 页面风格基本一致。
- 常用操作入口明显。

### 第 7 步：测试和修 bug

重点测试：

- 空用户名登录
- 错误密码登录
- 重复注册
- 金额为空
- 金额为 0 或负数
- 删除账单
- 修改密码
- 退出登录
- 无数据统计

完成标准：

- 核心功能没有崩溃。
- 错误输入都有 Toast 或 TextView 提示。
- 数据不会串到其他用户账号里。

## 7. 报告章节怎么对应开发内容

报告不是最后硬编，开发时就按下面内容保留截图和代码。

### 第 1 章 项目概述

写：

- 为什么做家庭账本。
- 家庭收入支出记录有什么实际意义。
- 本项目用 Android 技术实现本地账本管理。
- 核心功能包括用户登录、账单管理、分类管理、查询筛选、统计分析。

### 第 2 章 系统需求分析

写：

- 目标用户：普通家庭成员、学生、个人记账用户。
- 用户需求：记录日常收入支出，查看消费分类，统计每月结余。
- 功能需求：用户管理、账单管理、分类管理、查询统计、个人中心。

### 第 3 章 系统总体设计

要准备：

- 功能模块结构图。
- App 业务流程图。
- 页面设计说明。
- 数据库表设计。

可以画成：

```text
家庭账本 App
  用户管理
    注册
    登录
    修改密码
  账单管理
    添加账单
    编辑账单
    删除账单
    查询账单
  分类管理
    默认分类
    自定义分类
  统计分析
    月收入统计
    月支出统计
    分类统计
  个人中心
    退出登录
    关于 App
```

### 第 4 章 系统详细实现

建议重点写 2 个核心模块：

- 用户登录注册模块
- 账单增删改查模块

建议贴的代码：

- `LoginActivity.java` 的登录逻辑
- `DatabaseHelper.java` 的建表和查询代码
- `RecordEditActivity.java` 的添加账单逻辑
- `RecordAdapter.java` 的列表展示代码

### 第 5 章 系统测试

测试用例表可以按下面写：

| 测试项目 | 输入数据 | 预期结果 | 实际结果 | 结论 |
| --- | --- | --- | --- | --- |
| 用户注册 | 新用户名和密码 | 注册成功 | 与预期一致 | 通过 |
| 重复注册 | 已存在用户名 | 提示用户名已存在 | 与预期一致 | 通过 |
| 用户登录 | 正确账号密码 | 进入首页 | 与预期一致 | 通过 |
| 添加账单 | 金额 25，分类餐饮 | 添加成功并显示在列表 | 与预期一致 | 通过 |
| 删除账单 | 点击删除并确认 | 账单从列表消失 | 与预期一致 | 通过 |
| 月度统计 | 添加收入和支出 | 收入支出结余正确 | 与预期一致 | 通过 |

### 第 6 章 项目总结

写：

- 学会了 Android Activity 页面开发。
- 学会了 XML 布局和事件处理。
- 学会了 SQLite 本地数据库增删改查。
- 学会了 RecyclerView 列表展示。
- 不足可以写图表不够丰富、界面美观度还有提升空间。

## 8. 最低可交付版本

如果时间不够，必须至少完成：

- 注册
- 登录
- 退出登录
- 添加账单
- 查看账单列表
- 编辑账单
- 删除账单
- 月度收入支出统计
- SQLite 数据库存储
- 5 个以上页面

这个版本基本能满足课程要求，但报告里要把模块关系、数据库、测试写完整。

## 9. 加分优化项

完成基础功能后再做：

- 首页显示最近 5 条账单。
- 分类支出占比进度条。
- 账单按日期倒序排序。
- 删除账单前二次确认。
- 金额输入限制两位小数。
- 支持自定义分类。
- 支持按月份切换统计。
- App 名称和图标换成家庭账本相关。

## 10. 每次开发后的检查清单

每做完一个模块，都检查：

- App 能否正常启动。
- 页面跳转是否正常。
- 输入为空时是否有提示。
- 数据是否写入 SQLite。
- 列表是否刷新。
- 当前用户是否只能看到自己的数据。
- 是否有明显界面重叠。
- 是否有崩溃日志。

最终提交前检查：

- 项目源码完整。
- 数据库功能可用。
- 核心功能全部能现场演示。
- 报告章节完整。
- 报告中的功能、截图、数据库表和代码一致。
- 不要提交无法运行的半成品。
