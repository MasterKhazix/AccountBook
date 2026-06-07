# AccountBook

AccountBook 是一个基于 Android 原生技术实现的家庭账本应用，用于记录和管理日常收入、支出、分类、查询和统计数据。项目使用 Java、SQLite、SharedPreferences 和 XML 布局开发，适合作为 Android 课程设计或入门级完整应用项目参考。

## 功能特性

- 用户注册、登录、自动登录和退出登录
- 用户名长度限制：1-10 位
- 密码长度限制：6-12 位
- 账单新增、编辑、列表展示和批量删除
- 收入、支出分类管理
- 默认分类自动初始化
- 支持添加和删除自定义分类
- 按类型、分类、备注关键字筛选账单
- 按年份或指定年月查询账单
- 按年份或指定年月统计收入、支出和结余
- 支出分类排行和占比统计
- 首页展示本月收支总览和最近账单
- 个人中心支持修改密码

## 技术栈

- 开发语言：Java
- UI 布局：Android XML
- 本地数据库：SQLite
- 登录态存储：SharedPreferences
- 页面框架：AppCompatActivity
- 构建工具：Gradle
- 最低 SDK：24
- 目标 SDK：36

## 项目结构

```text
AccountBook
├── app/src/main/AndroidManifest.xml
├── app/src/main/java/com/example/accountbook
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
│   └── util
│       ├── SessionManager.java
│       └── AccountValidator.java
├── app/src/main/res/layout
├── app/src/main/res/drawable
├── app/src/main/res/values
├── DEVELOPMENT_GUIDE.md
├── FILE_CHANGE_LOG.md
└── FINAL_PROJECT_SUMMARY.md
```

## 核心模块说明

### 用户模块

用户模块由 `LoginActivity`、`RegisterActivity`、`ProfileActivity`、`SessionManager` 和 `AccountValidator` 组成。

- `LoginActivity`：负责登录、自动登录判断和跳转注册页。
- `RegisterActivity`：负责新用户注册和账号规则校验。
- `ProfileActivity`：负责展示当前用户、修改密码和退出登录。
- `SessionManager`：使用 SharedPreferences 保存当前登录用户。
- `AccountValidator`：统一校验用户名和密码长度。

### 数据库模块

数据库逻辑集中在 `DatabaseHelper` 中。

当前数据库包含三张表：

- `users`：用户表
- `records`：账单表
- `categories`：分类表

`DatabaseHelper` 提供用户注册登录、账单增删改查、分类管理、筛选查询和统计分析等方法。

### 账单模块

账单模块由 `RecordEditActivity`、`RecordListActivity` 和 `RecordSearchActivity` 组成。

- `RecordEditActivity`：新增或编辑收入、支出账单。
- `RecordListActivity`：展示全部账单，支持进入编辑页和批量删除。
- `RecordSearchActivity`：按类型、分类、年份、月份和备注关键字查询账单。

### 统计模块

统计模块由 `StatisticsActivity` 实现。

支持：

- 按全年统计
- 按指定年月统计
- 统计收入总额
- 统计支出总额
- 计算结余
- 展示支出分类排行和占比

### 分类模块

分类模块由 `CategoryActivity` 和 `DatabaseHelper` 中的分类方法实现。

支持：

- 收入分类和支出分类切换
- 默认分类自动创建
- 添加自定义分类
- 删除自定义分类
- 默认分类不可删除

## 数据库升级说明

当前数据库版本为 `4`。

由于项目后期增加了用户名和密码长度限制，为避免旧数据与新规则不一致，数据库从旧版本升级到版本 `4` 时会清空并重建旧表。该逻辑只针对本次账号规则调整，不是每次升级都会清空数据库。

后续新增数据都通过注册、登录和修改密码入口校验，保证入库数据符合当前规则。

## 运行方式

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle 同步完成。
3. 连接 Android 设备或启动模拟器。
4. 点击 Run 运行应用。

也可以在命令行执行：

```bash
./gradlew assembleDebug
```

Windows 环境可执行：

```bat
gradlew.bat assembleDebug
```

## 使用流程

1. 打开应用后进入登录页。
2. 没有账号时点击注册。
3. 注册成功后返回登录页。
4. 登录后进入首页。
5. 在首页可以快速新增收入或支出。
6. 在账单列表中查看、编辑或批量删除账单。
7. 在分类管理中维护自定义分类。
8. 在筛选查询中按条件查找账单。
9. 在统计分析中查看收支汇总和分类排行。
10. 在个人中心修改密码或退出登录。

## 文档说明

- `DEVELOPMENT_GUIDE.md`：项目开发指导和课程设计说明。
- `FILE_CHANGE_LOG.md`：开发过程中的文件变更记录。
- `FINAL_PROJECT_SUMMARY.md`：最终项目文件说明文档，详细说明每个文件的作用和实现内容。

## 当前状态

当前版本已在本机测试通过，主要功能可以正常使用。

已确认：

- Java 字符串检查通过。
- XML 资源解析通过。
- 旧的临时功能入口已清理。
- 旧的运行时历史用户清理函数已删除。
- 所有主要 Activity 已在 Manifest 中注册。

## License

本项目用于课程设计和学习实践，可根据实际需要自行补充许可证。
