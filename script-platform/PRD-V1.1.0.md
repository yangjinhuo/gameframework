# Python / SQL 在线脚本管理平台 — 产品需求文档（PRD）

> **版本**：v1.1.0
> **创建日期**：2026-05-12
> **更新日期**：2026-05-27
> **基线版本**：v1.0.0（2026-05-25）
> **文档状态**：评审稿
> **产品负责人**：待定

---

## 版本变更记录

| 版本     | 日期         | 变更摘要                                                                                                                                                                                                                                                                                                          |
| ------ | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| v1.0.0 | 2026-05-25 | 首版基线：用户管理、脚本管理（Python）、审计日志、服务监控（4.4 Apple 风格状态页）                                                                                                                                                                                                                                                            |
| v1.1.0 | 2026-05-27 | 1) 调整菜单目录结构，**脚本管理拆分为 Python 脚本 / SQL 脚本** 两个二级菜单；<br/>2) 新增 **SQL 脚本管理模块**（数据模型 `sql_info` / 列表 / 查看 / 在线编辑 / 测试 / 告警配置）；<br/>3) Python 脚本元数据扩展 4 个告警字段：`alert_level` / `alert_source_log` / `alert_template` / `alert_group`；<br/>4) Python / SQL 脚本均新增 **在线测试**功能（Python：`python xxx.py`；SQL：调用配置的 HTTP 接口） |

---

## 目录

1. [项目背景与目标](#1-项目背景与目标)
2. [用户角色](#2-用户角色)
3. [整体架构与页面结构](#3-整体架构与页面结构)
4. [功能模块详细说明](#4-功能模块详细说明)
   - 4.1 [用户管理模块](#41-用户管理模块)
   - 4.2 [脚本管理模块](#42-脚本管理模块)
     - 4.2.1 [Python 脚本管理](#421-python-脚本管理)
     - 4.2.2 [SQL 脚本管理（v1.1.0 新增）](#422-sql-脚本管理v110-新增)
     - 4.2.3 [脚本测试功能（v1.1.0 新增）](#423-脚本测试功能v110-新增)
   - 4.3 [审计日志模块](#43-审计日志模块)
   - 4.4 [服务监控模块](#44-服务监控模块)
5. [非功能性需求](#5-非功能性需求)
6. [数据库表结构设计](#6-数据库表结构设计)
7. [接口设计（RESTful API）](#7-接口设计restful-api)
8. [交互与 UI 规范](#8-交互与-ui-规范)
9. [权限矩阵](#9-权限矩阵)
10. [验收标准](#10-验收标准)
11. [里程碑计划](#11-里程碑计划)
12. [风险与约束](#12-风险与约束)

---

## 1. 项目背景与目标

### 1.1 背景

随着业务规模增长，维护的 Python 自动化脚本与日常巡检 SQL 数量持续增加，且依赖的对外服务越来越多，当前面临以下痛点：

| 痛点         | 描述                                |
| ---------- | --------------------------------- |
| 脚本分散       | Python 脚本、巡检 SQL 散落在各服务器/文档，缺乏统一入口 |
| 版本混乱       | 无法追溯脚本修改历史，出现问题难以回滚                |
| 责任不清       | 脚本/SQL 负责人信息缺失，故障时无法快速联系           |
| 安全隐患       | 任何人均可随意修改，无操作审计                   |
| 告警配置分散     | 钉钉群、电话、邮件、告警等级、模版无统一配置入口          |
| 调试链路长      | 改完脚本必须登录服务器才能执行验证，反馈链路慢           |
| 故障感知滞后     | 服务可用性、响应时间无主动巡检，问题发生后才被动处理        |

### 1.2 目标

构建一个 **Python / SQL 在线脚本管理平台 + 服务监控平台**，实现：

- 集中化管理所有 Python 脚本与 SQL 脚本的元信息与内容
- 提供在线查看、编辑、**一键测试**能力，支持语法高亮（Python / SQL）
- 完善的用户权限体系，保障操作安全
- 全操作审计追踪，满足合规要求
- 配置告警通道（钉钉 + 电话 + 邮件）、告警等级（L1/L2/L3）、告警日志来源、通知模版、通知群
- 对关键依赖服务进行**主动巡检 + 多通道告警**，类 Apple System Status 的可视化总览

---

## 2. 用户角色

| 角色        | 说明       | 主要权限                                |
| --------- | -------- | ----------------------------------- |
| **超级管理员** | 系统最高权限用户 | 用户管理、脚本（Python/SQL）管理、审计查看、监控配置、系统配置 |
| **普通管理员** | 日常运维人员   | 脚本（Python/SQL）查看/编辑/测试、审计查看、监控查看/配置、告警确认 |
| **只读用户**  | 观察者角色    | 脚本查看、审计查看、监控状态查看（只读，**不可测试**）       |

---

## 3. 整体架构与页面结构

```
脚本管理平台
├── 登录页
└── 主控台（侧边栏导航）
    ├── 首页 Dashboard（脚本数 / 用户数 / 监控状态汇总）
    ├── 用户管理
    │   ├── 用户列表
    │   ├── 新增用户（弹窗/抽屉）
    │   └── 编辑用户（弹窗/抽屉）
    ├── 脚本管理
    │   ├── Python 脚本
    │   │   ├── 脚本列表
    │   │   ├── 查看脚本（代码预览页）
    │   │   └── 编辑脚本（在线编辑器页 + 测试）
    │   └── SQL 脚本
    │       ├── 脚本列表
    │       ├── 查看脚本（代码预览页）
    │       └── 编辑脚本（在线编辑器页 + 测试）
    ├── 审计日志
    │   └── 审计日志列表
    └── 服务监控
        ├── 监控总览（Apple 风格状态页 - 默认页）
        ├── 监控配置列表
        ├── 新增 / 编辑监控（抽屉）
        ├── 监控详情（趋势图 + 检查历史）
        └── 告警记录
```

> **菜单变更说明（v1.1.0）**：原 V1.0.0 中"脚本管理"为单层菜单，仅管理 Python 脚本。V1.1.0 调整为一级"脚本管理"，下挂"Python 脚本"、"SQL 脚本"两个二级菜单，路由前缀分别为 `/scripts/python` 与 `/scripts/sql`。

---

## 4. 功能模块详细说明

### 4.1 用户管理模块

#### 4.1.1 功能概述

提供平台用户的 **增删改查（CRUD）** 能力，支持用户名和注册时间的筛选搜索。

#### 4.1.2 用户信息数据模型

| 字段名          | 类型           | 必填   | 说明                                      |
| ------------ | ------------ | ---- | --------------------------------------- |
| `id`         | bigint       | 系统生成 | 主键，自增                                   |
| `username`   | varchar(64)  | ✅    | 用户名，全局唯一，3~32 位字母数字下划线                  |
| `password`   | varchar(255) | ✅    | 密码，md5 存储，前端直接 md5 加密，防止明文传输，前端不展示       |
| `role`       | enum         | ✅    | 角色：`super_admin` / `admin` / `readonly` |
| `status`     | tinyint      | ✅    | 状态：1=启用，0=禁用                            |
| `created_at` | datetime     | 系统生成 | 注册时间                                    |
| `updated_at` | datetime     | 系统生成 | 最后修改时间                                  |
| `remark`     | varchar(256) | ❌    | 备注                                      |

#### 4.1.3 用户列表页

**搜索条件区：**

| 搜索字段 | 控件类型    | 说明           |
| ---- | ------- | ------------ |
| 用户名  | 文本输入框   | 模糊搜索         |
| 注册时间 | 日期范围选择器 | 选择起止日期       |
| 状态   | 下拉框     | 全部 / 启用 / 禁用 |

**操作按钮区：**

- `【+ 新增用户】` 按钮（蓝色主按钮，位于搜索区右侧）
- `【重置】` 按钮（清空搜索条件并重新加载）
- `【搜索】` 按钮

**列表表格列定义：**

| 列名   | 字段           | 说明                         |
| ---- | ------------ | -------------------------- |
| 序号   | —            | 行号                         |
| 用户名  | `username`   | 文字展示                       |
| 角色   | `role`       | Tag 标签展示                   |
| 状态   | `status`     | 绿色=启用 / 红色=禁用，Switch 可直接切换 |
| 注册时间 | `created_at` | `YYYY-MM-DD HH:mm:ss`      |
| 操作   | —            | `【编辑】` `【删除】` 两个操作按钮       |

**分页：** 默认每页 20 条，支持切换 10 / 20 / 50 / 100。

#### 4.1.4 新增 / 编辑用户（弹窗）

**表单字段：**

| 字段   | 控件             | 校验规则                                       |
| ---- | -------------- | ------------------------------------------ |
| 用户名  | Input          | 必填，3~32 位，字母数字下划线，编辑时不可修改                  |
| 密码   | Password Input | 新增必填，编辑时选填（不填则保持原密码），8~32 位，需含大写字母、小写字母、数字 |
| 确认密码 | Password Input | 与密码一致                                      |
| 角色   | Select         | 必填，下拉选择                                    |
| 状态   | Switch         | 默认启用                                       |
| 备注   | Textarea       | 选填，最多 256 字符                               |

**操作按钮：** `【确定】` `【取消】`

#### 4.1.5 删除用户

- 点击删除按钮，弹出确认框："确认删除用户 **{username}**？此操作不可撤销。"
- 超级管理员账号不可删除
- 删除后该用户无法登录，相关审计记录保留

---

### 4.2 脚本管理模块

#### 4.2.0 模块概述（v1.1.0 调整）

脚本管理拆分为两个相互独立、风格一致的子模块：

| 子模块           | 路由前缀              | 适用对象               | 编辑器语法 | 测试方式                        |
| ------------- | ----------------- | ------------------ | ----- | --------------------------- |
| **Python 脚本** | `/scripts/python` | 自动化任务、数据同步、报表生成等   | Python | 后端 `python <path>` 子进程执行    |
| **SQL 脚本**    | `/scripts/sql`    | 数据巡检、慢 SQL 验证、灰度查询 | SQL   | 调用配置化 HTTP 接口提交 SQL 并回显执行结果 |

两个子模块在列表页、查看页、编辑页的交互骨架保持一致；差异点仅在数据模型、编辑器语法、测试实现方式。

---

#### 4.2.1 Python 脚本管理

##### 4.2.1.1 数据模型 `script_info`（v1.1.0 扩展 4 字段）

| 字段名                | 类型           | 必填   | 说明                                |
| ------------------ | ------------ | ---- | --------------------------------- |
| `id`               | bigint       | 系统生成 | 主键，自增                             |
| `script_name`      | varchar(128) | ✅    | 脚本名称及用途                           |
| `script_path`      | varchar(512) | ✅    | 脚本在服务器上的绝对路径                      |
| `module`           | varchar(64)  | ✅    | 归属模块（如：数据同步、报表生成、监控告警）            |
| `description`      | text         | ✅    | 功能说明，描述脚本业务用途                     |
| `owner`            | varchar(64)  | ✅    | 负责人（关联用户名）                        |
| `alert_dingtalk`   | varchar(256) | ❌    | 预警钉钉 Webhook URL 或群名称             |
| `alert_phone`      | varchar(64)  | ❌    | 预警电话号码                            |
| `alert_level`      | varchar(128) | ❌    | **(v1.1.0 新增)** 告警等级 `L1` / `L2` / `L3` |
| `alert_source_log` | varchar(128) | ❌    | **(v1.1.0 新增)** 告警日志来源（如：日志文件路径、SLS LogStore 名） |
| `alert_template`   | varchar(128) | ❌    | **(v1.1.0 新增)** 通知模版 ID 或名称       |
| `alert_group`      | varchar(128) | ❌    | **(v1.1.0 新增)** 通知群（钉钉群名 / 飞书群 ID 等） |
| `schedule_cron`    | varchar(64)  | ❌    | 调度频率（Cron 表达式）                    |
| `schedule_desc`    | varchar(128) | ❌    | 调度频率人读描述                          |
| `script_content`   | longtext     | ✅    | 脚本内容（Python 代码）                   |
| `version`          | int          | 系统生成 | 版本号，每次编辑 +1，初始为 1                 |
| `status`           | tinyint      | ✅    | 1=启用，0=禁用                         |
| `created_at`       | datetime     | 系统生成 | 创建时间                              |
| `updated_at`       | datetime     | 系统生成 | 最后修改时间                            |

##### 4.2.1.2 列表页

**搜索条件**：脚本名称（模糊）、归属模块（动态加载）、负责人（模糊）、告警等级、状态。

**列表列**：序号、脚本名称、脚本路径、功能说明、归属模块、负责人、**告警等级（L1/L2/L3 不同颜色 Tag）**、预警钉钉、预警电话、调度频率、版本（`v{n}`）、状态、操作（`【查看】` / `【编辑】` / `【测试】` / `【删除】`）。

**分页**：默认每页 20 条。

##### 4.2.1.3 查看脚本页

- 展示脚本完整内容（Monaco Editor，只读，Python 语法高亮）
- 右上角展示：脚本名称、版本号、最后更新时间、负责人、**告警等级 Tag**
- 元信息面板（左侧/顶部）：路径、归属模块、调度频率、预警钉钉、预警电话、**告警日志来源、通知模版、通知群**
- 按钮：`【复制代码】` `【返回列表】` `【测试】`（admin 及以上）

##### 4.2.1.4 编辑脚本页

- Monaco Editor，Python 语法高亮 + 自动缩进
- 上方展示元信息（只读）
- 可编辑字段：脚本内容、功能说明、归属模块、负责人、预警钉钉、预警电话、**告警等级、告警日志来源、通知模版、通知群**、调度频率、状态
- **保存动作**：版本号 +1，记录操作人和时间，写入审计日志，并保存到对应的本地目录（先备份原文件 `<path>.bak.<version>`，再覆盖写入）
- 底部按钮：`【保存】` `【保存并测试】` `【取消】`
- 离开页面时如有未保存内容，弹出"您有未保存的更改，确定离开？"

> **告警字段交互建议**：`alert_level` 使用 `Select`（选项：L1 / L2 / L3，颜色：红 / 橙 / 蓝）；`alert_template` 与 `alert_group` 提供下拉 + 自由输入模式（首次输入后下次出现在历史选项中）。

---

#### 4.2.2 SQL 脚本管理（v1.1.0 新增）

##### 4.2.2.1 功能概述

集中管理日常巡检、灰度核对、慢查询验证类的 SQL 脚本，提供：

- 元信息（名称、模块、负责人、调度频率、告警配置等）维护
- SQL 内容在线查看 / 编辑（SQL 语法高亮）
- 阈值（`threshold`）配置：当 SQL 执行结果**首列单行整型值**或**结果行数**超过阈值时触发告警（具体语义由对接的执行平台定义，本平台仅做配置存储与传递）
- 在线测试：调用配置的 HTTP 接口提交 SQL，回显结果（参见 4.2.3）

##### 4.2.2.2 数据模型 `sql_info`

> **表名说明**：原始需求草稿中表名写作 `script_info`，与 4.2.1 Python 脚本表冲突。本 PRD 正式定义为 **`sql_info`**，避免歧义并保持向后兼容。

| 字段名                | 类型           | 必填   | 说明                                  |
| ------------------ | ------------ | ---- | ----------------------------------- |
| `id`               | bigint       | 系统生成 | 主键，自增                               |
| `sql_name`         | varchar(128) | ✅    | SQL 脚本名称                            |
| `module`           | varchar(64)  | ✅    | 归属模块                                |
| `description`      | text         | ✅    | 功能说明                                |
| `owner`            | varchar(64)  | ✅    | 负责人（关联用户名）                          |
| `alert_dingtalk`   | varchar(256) | ❌    | 预警钉钉 Webhook                        |
| `alert_phone`      | varchar(64)  | ❌    | 预警电话                                |
| `alert_level`      | varchar(128) | ❌    | 告警等级 `L1` / `L2` / `L3`             |
| `alert_source_log` | varchar(128) | ❌    | 告警日志来源                              |
| `alert_template`   | varchar(128) | ❌    | 通知模版                                |
| `alert_group`      | varchar(128) | ❌    | 通知群                                 |
| `schedule_cron`    | varchar(64)  | ❌    | Cron 表达式                            |
| `schedule_desc`    | varchar(128) | ❌    | 调度频率人读描述                            |
| `threshold`        | int          | ❌    | 阈值，默认 0；超过即告警                       |
| `sql_content`      | longtext     | ✅    | SQL 脚本内容                            |
| `version`          | int          | 系统生成 | 版本号，每次编辑 +1，初始为 1                   |
| `status`           | tinyint      | ✅    | 1=启用，0=禁用                           |
| `created_at`       | datetime     | 系统生成 | 创建时间                                |
| `updated_at`       | datetime     | 系统生成 | 最后修改时间                              |

##### 4.2.2.3 列表页

**搜索条件**：SQL 名称（模糊）、归属模块、负责人、告警等级、状态。

**列表列**：序号、SQL 名称、功能说明（超 50 字符省略 + Tooltip）、归属模块、负责人、**告警等级**、预警钉钉、预警电话、调度频率、**阈值**、版本、状态、操作（`【查看】` / `【编辑】` / `【测试】` / `【删除】`）。

##### 4.2.2.4 查看 SQL 页

- Monaco Editor，SQL 语法高亮，只读
- 顶部显示：名称、版本、最后更新时间、负责人、告警等级、阈值
- 元信息面板：模块、调度频率、预警钉钉、预警电话、告警日志来源、通知模版、通知群
- 按钮：`【复制】` `【返回列表】` `【测试】`（admin 及以上）

##### 4.2.2.5 编辑 SQL 页

- Monaco Editor，SQL 语法高亮，自动缩进、关键字大写提示
- 可编辑字段：`sql_content`、`description`、`module`、`owner`、`alert_*`、`schedule_*`、`threshold`、`status`
- 保存动作：版本号 +1，写入审计日志（事件类型 `SQL_SCRIPT_UPDATE`）
- 底部按钮：`【保存】` `【保存并测试】` `【取消】`
- 离开未保存提示与 Python 子模块一致

> **注意**：SQL 脚本不落地服务器文件，**无 4.2.1.4 中的本地备份/覆盖逻辑**，仅在数据库中维护版本。

---

#### 4.2.3 脚本测试功能（v1.1.0 新增）

##### 4.2.3.1 入口

- **列表页**：每行操作列新增 `【测试】` 按钮（admin 及以上可见）
- **查看页 / 编辑页**：右上角 `【测试】` 按钮；编辑页提供 `【保存并测试】`（先保存再测试）
- **结果展示**：右侧抽屉（宽 600px）滑出，显示执行结果，可关闭后再次触发

##### 4.2.3.2 Python 脚本测试

| 项目          | 说明                                                                                                |
| ----------- | ------------------------------------------------------------------------------------------------- |
| 触发方式        | `POST /api/python-scripts/{id}/test`                                                              |
| 后端实现        | 使用 `ProcessBuilder` 启动子进程：`python <script_path>`，捕获 `stdout` / `stderr` / `exitCode` 与 `durationMs` |
| 工作目录        | `script_path` 所在目录                                                                                |
| 超时          | 单次测试默认 60 秒，可在 `application.yml` 中通过 `script.test.python.timeout-sec` 调整                          |
| 解释器         | 默认 `python`，可通过 `script.test.python.interpreter` 指定（如 `python3` / 绝对路径）                           |
| 输出截断        | `stdout` / `stderr` 单字段最多 64KB，超出截断并标注 `[truncated]`                                              |
| 安全约束        | 仅 admin 及以上角色可触发；只读用户接口返回 403                                                                     |
| 审计          | 写入审计日志，事件类型 `PYTHON_SCRIPT_TEST`，请求参数记录 `scriptId` / `version`，响应记录 `exitCode` / 截短的输出            |
| 失败行为        | 返回 `success=false`，结构化字段 `{ exitCode, stderr, stdout, errorMessage }`，前端在结果抽屉中以红色高亮 `stderr`        |

**响应示例**：

```json
{
  "success": false,
  "exitCode": 1,
  "durationMs": 327,
  "stdout": "",
  "stderr": "Traceback (most recent call last):\n  File \"sync_user.py\", line 12, in <module>\n    ...",
  "errorMessage": "Process exited with non-zero code"
}
```

##### 4.2.3.3 SQL 脚本测试

| 项目          | 说明                                                                                            |
| ----------- | --------------------------------------------------------------------------------------------- |
| 触发方式        | `POST /api/sql-scripts/{id}/test`                                                             |
| 后端实现        | 调用配置化的 HTTP 执行接口（外部 SQL 执行平台），将 SQL 文本作为 Body 提交，转发响应                                       |
| 配置项        | `script.test.sql.endpoint`：HTTP URL；`script.test.sql.method`：默认 `POST`；`script.test.sql.headers`：JSON Map（含鉴权 Token 等）；`script.test.sql.timeout-sec`：默认 30 |
| 配置维护方式      | `application.yml` + `sys_config` 表双重支持，运维可在管理后台不重启切换                                         |
| 请求模板（默认）   | `{ "sqlId": <id>, "sqlName": "...", "sqlContent": "...", "operator": "<currentUser>" }`         |
| 响应规约        | 透传外部接口响应；同时本平台返回 `{ success, durationMs, raw, summary }`，`success` 由 HTTP 2xx + 响应中 `code==0` 共同判定 |
| 超时          | 默认 30 秒                                                                                      |
| 安全约束        | 仅 admin 及以上角色可触发；只读用户接口返回 403                                                                 |
| 审计          | 事件类型 `SQL_SCRIPT_TEST`，请求参数记录 `sqlId` / `version`，响应记录截短后的 `summary`                          |

**响应示例（成功）**：

```json
{
  "success": true,
  "durationMs": 412,
  "summary": { "rowCount": 17, "columns": ["uid", "name"] },
  "raw": { "code": 0, "msg": "ok", "data": [ ... ] }
}
```

**响应示例（失败）**：

```json
{
  "success": false,
  "durationMs": 88,
  "errorMessage": "ER_PARSE_ERROR: You have an error in your SQL syntax",
  "raw": { "code": 1064, "msg": "..." }
}
```

##### 4.2.3.4 结果抽屉 UI

- 顶部：脚本名 + 测试触发时间 + 总耗时
- 中部：标签页 `【输出】` / `【错误】` / `【原始响应】`
  - Python：输出 = stdout，错误 = stderr，原始响应 = exitCode + 完整对象
  - SQL：输出 = `summary` 表格视图，错误 = `errorMessage`，原始响应 = 透传 JSON
- 底部：`【再次测试】` `【复制结果】` `【关闭】`

---

### 4.3 审计日志模块

#### 4.3.1 功能概述

记录平台所有 **写操作** 与脚本测试操作（增删改/测试），提供操作追溯能力，满足安全合规要求。

#### 4.3.2 审计日志数据模型

| 字段名               | 类型           | 说明                        |
| ----------------- | ------------ | ------------------------- |
| `id`              | bigint       | 主键，自增                     |
| `event_time`      | datetime     | 操作时间，精确到秒                 |
| `event_type`      | varchar(64)  | 事件类型（见下方枚举）               |
| `operator`        | varchar(64)  | 操作人用户名                    |
| `operator_ip`     | varchar(64)  | 操作人 IP 地址                 |
| `target_id`       | bigint       | 操作对象 ID                   |
| `target_name`     | varchar(256) | 操作对象名称（冗余字段）              |
| `request_params`  | text         | 请求参数（JSON 格式，密码字段脱敏）      |
| `response_result` | text         | 响应结果（JSON 格式，截取前 2048 字符） |
| `status`          | tinyint      | 操作结果：1=成功，0=失败            |
| `remark`          | varchar(512) | 备注                        |

**事件类型枚举（`event_type`）：**

| 事件值                   | 描述                | 来源版本 |
| --------------------- | ----------------- | ---- |
| `USER_CREATE`         | 新增用户              | 1.0.0 |
| `USER_UPDATE`         | 修改用户              | 1.0.0 |
| `USER_DELETE`         | 删除用户              | 1.0.0 |
| `USER_LOGIN`          | 用户登录              | 1.0.0 |
| `USER_LOGOUT`         | 用户登出              | 1.0.0 |
| `SCRIPT_VIEW`         | 查看 Python 脚本（可选）  | 1.0.0 |
| `SCRIPT_CREATE`       | 新增 Python 脚本      | 1.0.0 |
| `SCRIPT_EDIT`         | 编辑 Python 脚本      | 1.0.0 |
| `SCRIPT_DELETE`       | 删除 Python 脚本      | 1.0.0 |
| `PYTHON_SCRIPT_TEST`  | **(新增)** Python 脚本在线测试 | 1.1.0 |
| `SQL_SCRIPT_VIEW`     | **(新增)** 查看 SQL 脚本（可选） | 1.1.0 |
| `SQL_SCRIPT_CREATE`   | **(新增)** 新增 SQL 脚本     | 1.1.0 |
| `SQL_SCRIPT_UPDATE`   | **(新增)** 编辑 SQL 脚本     | 1.1.0 |
| `SQL_SCRIPT_DELETE`   | **(新增)** 删除 SQL 脚本     | 1.1.0 |
| `SQL_SCRIPT_TEST`     | **(新增)** SQL 脚本在线测试   | 1.1.0 |
| `MONITOR_CREATE`      | 新增监控配置            | 1.0.0 |
| `MONITOR_UPDATE`      | 修改监控配置            | 1.0.0 |
| `MONITOR_DELETE`      | 删除监控配置            | 1.0.0 |
| `MONITOR_TEST`        | 手动触发一次检查          | 1.0.0 |
| `MONITOR_ALERT`       | 触发告警              | 1.0.0 |
| `MONITOR_ALERT_ACK`   | 告警确认              | 1.0.0 |

> 兼容性说明：v1.0.0 已落库的 `SCRIPT_*` 事件继续视作 Python 脚本事件，无需迁移。

#### 4.3.3 审计日志列表页

**搜索条件区：**

| 搜索字段 | 控件类型    | 说明           |
| ---- | ------- | ------------ |
| 操作时间 | 日期范围选择器 | 起止日期时间       |
| 事件类型 | 下拉多选框   | 支持多选（含新增 SQL_*、PYTHON_SCRIPT_TEST 项） |
| 操作人  | 文本输入框   | 精确搜索         |
| 操作结果 | 下拉框     | 全部 / 成功 / 失败 |

**列表表格列定义：**

| 列名     | 字段                | 展示说明                     |
| ------ | ----------------- | ------------------------ |
| 序号     | —                 | 行号                       |
| 操作时间   | `event_time`      | `YYYY-MM-DD HH:mm:ss`    |
| 事件类型   | `event_type`      | 中文 Tag 展示，按业务域分色：用户(蓝)/Python(青)/SQL(紫)/监控(橙) |
| 操作人    | `operator`        | 文字                       |
| 操作人 IP | `operator_ip`     | 文字                       |
| 操作对象   | `target_name`     | 文字                       |
| 结果     | `status`          | 成功（绿）/ 失败（红）             |
| 请求参数   | `request_params`  | 折叠展示，点击`【详情】`弹窗查看完整 JSON |
| 响应结果   | `response_result` | 折叠展示，点击`【详情】`弹窗查看完整 JSON |

**说明：**

- 审计日志 **只读**，不提供编辑、删除功能
- 审计日志保留周期建议：**180 天**，超期自动归档
- 支持 `【导出 Excel】` 功能（按当前搜索条件导出）

---

### 4.4 服务监控模块

> 本节相对 v1.0.0 无变更，保持原有约定。

#### 4.4.1 功能概述

对外部依赖服务（HTTP API、网关、数据库、内网 TCP 服务等）进行 **主动巡检**，提供：

- 类 Apple System Status 的全局状态总览页
- 监控配置 CRUD（地址、频率、阈值、负责人、告警通道）
- 周期性调度执行检查并落库
- 触发阈值时通过 **钉钉 / 邮箱 / 电话** 多通道告警
- 告警确认与抑制策略，避免告警风暴
- 30 / 90 天可用率（SLA）统计与响应时间趋势图

#### 4.4.2 状态分级

| 状态               | 颜色   | 含义                                  |
| ---------------- | ---- | ----------------------------------- |
| `OPERATIONAL`    | 绿色 ● | 正常                                  |
| `DEGRADED`       | 黄色 ● | 性能下降，响应时间持续超阈值                      |
| `PARTIAL_OUTAGE` | 橙色 ● | 部分中断，间歇失败（失败比例 ≥ 30% 且未达连续阈值）       |
| `MAJOR_OUTAGE`   | 红色 ● | 完全中断，连续失败 ≥ 配置阈值（默认 3）              |
| `MAINTENANCE`    | 蓝色 ● | 维护中，期间不告警                           |

> 全局状态：取所有启用监控中"最严重"状态作为顶部全局状态。

#### 4.4.3 检查类型

| 类型     | 说明                                  | 关键参数                   |
| ------ | ----------------------------------- | ---------------------- |
| `HTTP` | HTTP/HTTPS 请求，校验状态码 + 响应时间 + 关键字   | URL、Method、Header、Body |
| `TCP`  | 建立 TCP 连接，校验是否连通                    | host:port              |
| `PING` | ICMP Ping                           | host                   |
| `CMD`  | 执行平台预置脚本（受限白名单）                     | 内置 cmd 标识              |

#### 4.4.4 ~ 4.4.14

> 数据模型 `monitor_config` / `monitor_check_record` / `monitor_alert`、调度与状态机、告警通道实现、监控总览页、监控配置列表页、新增编辑抽屉、监控详情页、告警记录页、平台告警管理后台配置等条款，均与 V1.0.0 一致，详见 V1.0.0 PRD 4.4 节，本版本不做修改。

---

## 5. 非功能性需求

### 5.1 性能要求

| 指标                       | 要求                   |
| ------------------------ | -------------------- |
| 页面首屏加载                   | ≤ 3 秒（正常网络）          |
| 列表查询响应                   | ≤ 1 秒（数据量 ≤ 10 万条）   |
| 脚本保存响应                   | ≤ 2 秒                |
| **Python 脚本测试响应**         | ≤ 60 秒（受脚本执行时长决定，超时即中断） |
| **SQL 脚本测试响应**            | ≤ 30 秒（受外部接口决定，超时即中断） |
| 监控调度抖动                   | ≤ 5 秒                |
| 监控总览页加载（≤ 200 个服务）       | ≤ 2 秒                |
| 并发用户数                    | 支持 50 人同时在线          |
| 单实例可承载监控数               | ≥ 500 条（HTTP 1 分钟级）  |

### 5.2 安全要求

- 密码存储：前端 md5 传输，数据库存储 md5 串
- 接口认证：**JWT Token**，有效期 8 小时，支持刷新
- 防止 SQL 注入：使用 ORM 或参数化查询（**SQL 脚本测试场景由外部执行平台负责防护，本平台仅传递文本**）
- 防止 XSS：前端内容输出转义
- 脚本与监控配置传输：HTTPS 加密
- 敏感字段脱敏：钉钉 Webhook 截短中间，电话保留前三后四
- **Python 脚本测试**：仅允许执行 `script_path` 指向的当前脚本，**禁止从 UI 注入额外参数**；运行用户为后端服务进程用户，依赖系统层面的 chroot/容器隔离
- **SQL 脚本测试**：调用外部接口时，SQL 文本完整透传，由外部平台做权限/语句白名单校验

### 5.3 可用性要求

- 系统可用率 ≥ 99.5%
- 支持浏览器：Chrome ≥ 90、Edge ≥ 90、Firefox ≥ 88

### 5.4 可扩展性

- 前后端分离架构
- RESTful API 设计
- 检查类型与告警通道均通过策略接口实现
- **脚本测试器**通过 `IScriptTester` 接口抽象，目前内置 `PythonProcessTester` 与 `SqlHttpTester`，后续可扩展 Shell / Groovy / Kotlin 等

---

## 6. 数据库表结构设计

### 6.1 用户表 `sys_user`

```sql
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
  `password`    VARCHAR(255) NOT NULL COMMENT '密码，直接存储，接口传入已经md5',
  `role`        VARCHAR(32)  NOT NULL DEFAULT 'admin' COMMENT '角色',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 6.2 Python 脚本表 `script_info`（v1.1.0 新增 4 字段）

```sql
CREATE TABLE `script_info` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `script_name`      VARCHAR(128)  NOT NULL COMMENT '脚本名称',
  `script_path`      VARCHAR(512)  NOT NULL COMMENT '脚本路径',
  `module`           VARCHAR(64)   NOT NULL COMMENT '归属模块',
  `description`      TEXT          COMMENT '功能说明',
  `owner`            VARCHAR(64)   NOT NULL COMMENT '负责人',
  `alert_dingtalk`   VARCHAR(256)  DEFAULT NULL COMMENT '预警钉钉',
  `alert_phone`      VARCHAR(64)   DEFAULT NULL COMMENT '预警电话',
  `alert_level`      VARCHAR(128)  DEFAULT NULL COMMENT '告警等级L1，L2，L3',
  `alert_source_log` VARCHAR(128)  DEFAULT NULL COMMENT '告警日志来源',
  `alert_template`   VARCHAR(128)  DEFAULT NULL COMMENT '通知模版',
  `alert_group`      VARCHAR(128)  DEFAULT NULL COMMENT '通知群',
  `schedule_cron`    VARCHAR(64)   DEFAULT NULL COMMENT 'Cron表达式',
  `schedule_desc`    VARCHAR(128)  DEFAULT NULL COMMENT '调度频率描述',
  `script_content`   LONGTEXT      NOT NULL COMMENT '脚本内容',
  `version`          INT           NOT NULL DEFAULT 1 COMMENT '版本号',
  `status`           TINYINT       NOT NULL DEFAULT 1 COMMENT '状态',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_owner`  (`owner`),
  KEY `idx_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Python脚本信息表';
```

**升级 SQL（V1.0.0 → V1.1.0 在线迁移）**：

```sql
ALTER TABLE `script_info`
  ADD COLUMN `alert_level`      VARCHAR(128) DEFAULT NULL COMMENT '告警等级L1，L2，L3' AFTER `alert_phone`,
  ADD COLUMN `alert_source_log` VARCHAR(128) DEFAULT NULL COMMENT '告警日志来源'        AFTER `alert_level`,
  ADD COLUMN `alert_template`   VARCHAR(128) DEFAULT NULL COMMENT '通知模版'          AFTER `alert_source_log`,
  ADD COLUMN `alert_group`      VARCHAR(128) DEFAULT NULL COMMENT '通知群'           AFTER `alert_template`,
  ADD KEY `idx_alert_level` (`alert_level`);
```

### 6.3 SQL 脚本表 `sql_info`（v1.1.0 新增）

```sql
CREATE TABLE `sql_info` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sql_name`         VARCHAR(128)  NOT NULL COMMENT 'SQL名称',
  `module`           VARCHAR(64)   NOT NULL COMMENT '归属模块',
  `description`      TEXT          COMMENT '功能说明',
  `owner`            VARCHAR(64)   NOT NULL COMMENT '负责人',
  `alert_dingtalk`   VARCHAR(256)  DEFAULT NULL COMMENT '预警钉钉',
  `alert_phone`      VARCHAR(64)   DEFAULT NULL COMMENT '预警电话',
  `schedule_cron`    VARCHAR(64)   DEFAULT NULL COMMENT 'Cron表达式',
  `schedule_desc`    VARCHAR(128)  DEFAULT NULL COMMENT '调度频率描述',
  `alert_level`      VARCHAR(128)  DEFAULT NULL COMMENT '告警等级L1，L2，L3',
  `alert_source_log` VARCHAR(128)  DEFAULT NULL COMMENT '告警日志来源',
  `alert_template`   VARCHAR(128)  DEFAULT NULL COMMENT '通知模版',
  `alert_group`      VARCHAR(128)  DEFAULT NULL COMMENT '通知群',
  `threshold`        INT           DEFAULT 0 COMMENT '阈值',
  `sql_content`      LONGTEXT      NOT NULL COMMENT 'SQL脚本内容',
  `version`          INT           NOT NULL DEFAULT 1 COMMENT '版本号',
  `status`           TINYINT       NOT NULL DEFAULT 1 COMMENT '状态',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_owner`  (`owner`),
  KEY `idx_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SQL脚本信息表';
```

### 6.4 审计日志表 `audit_log`

> 与 V1.0.0 一致，无 schema 变更。仅 `event_type` 取值范围扩展，详见 4.3.2。

```sql
CREATE TABLE `audit_log` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `event_time`      DATETIME      NOT NULL COMMENT '操作时间',
  `event_type`      VARCHAR(64)   NOT NULL COMMENT '事件类型',
  `operator`        VARCHAR(64)   NOT NULL COMMENT '操作人',
  `operator_ip`     VARCHAR(64)   DEFAULT NULL COMMENT '操作人IP',
  `target_id`       BIGINT        DEFAULT NULL COMMENT '操作对象ID',
  `target_name`     VARCHAR(256)  DEFAULT NULL COMMENT '操作对象名称',
  `request_params`  TEXT          COMMENT '请求参数JSON',
  `response_result` TEXT          COMMENT '响应结果JSON',
  `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '结果:1成功0失败',
  `remark`          VARCHAR(512)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_event_time` (`event_time`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_operator`   (`operator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
```

### 6.5 服务监控相关表

`monitor_config` / `monitor_check_record` / `monitor_alert` 三张表与 V1.0.0 PRD 6.4~6.6 一致，本版本无变更。

### 6.6 系统配置表 `sys_config`（v1.1.0 提前落地）

> v1.0.0 中 4.4.14 提到"由数据库 `sys_config` 双重支持"，v1.1.0 SQL 测试接口同样依赖该表，因此本版本正式给出 DDL：

```sql
CREATE TABLE `sys_config` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `config_key`  VARCHAR(128) NOT NULL COMMENT '配置键',
  `config_val`  TEXT         COMMENT '配置值',
  `description` VARCHAR(256) DEFAULT NULL COMMENT '说明',
  `updated_by`  VARCHAR(64)  DEFAULT NULL COMMENT '最后修改人',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

**SQL 测试相关默认配置项**：

| `config_key`                      | 示例值                                | 说明              |
| --------------------------------- | ---------------------------------- | --------------- |
| `script.test.sql.endpoint`        | `https://sql-runner.example.com/run` | SQL 执行平台 URL    |
| `script.test.sql.method`          | `POST`                             | HTTP 方法         |
| `script.test.sql.headers`         | `{"Authorization":"Bearer xxx"}`    | 自定义请求头（JSON）    |
| `script.test.sql.timeout-sec`     | `30`                               | 超时（秒）           |
| `script.test.python.interpreter`  | `python3`                          | Python 解释器路径    |
| `script.test.python.timeout-sec`  | `60`                               | Python 测试超时（秒）  |

---

## 7. 接口设计（RESTful API）

### 7.1 认证接口

| Method | Path                | 说明                                        |
| ------ | ------------------- | ----------------------------------------- |
| POST   | `/api/auth/login`   | 登录，返回 JWT，需要 md5 密文传输，不接入 UAP 单点登录          |
| POST   | `/api/auth/logout`  | 登出                                        |
| POST   | `/api/auth/refresh` | 刷新 Token                                  |

### 7.2 用户管理接口

| Method | Path                     | 说明          |
| ------ | ------------------------ | ----------- |
| GET    | `/api/users`             | 用户列表（分页+搜索） |
| POST   | `/api/users`             | 新增用户        |
| PUT    | `/api/users/{id}`        | 修改用户        |
| DELETE | `/api/users/{id}`        | 删除用户        |
| PATCH  | `/api/users/{id}/status` | 切换状态        |

### 7.3 Python 脚本管理接口（v1.1.0 路径调整）

> 为兼容 V1.0.0 已经发布的 `/api/scripts/*`，本版本：
> - 新增首选路径 `/api/python-scripts/*`；
> - 旧路径 `/api/scripts/*` 保留为别名（Deprecated），下个 minor 版本下线。

| Method | Path                                | 说明                  | 权限          |
| ------ | ----------------------------------- | ------------------- | ----------- |
| GET    | `/api/python-scripts`               | Python 脚本列表（分页+搜索） | 全员          |
| GET    | `/api/python-scripts/{id}`          | 获取脚本详情（含内容）         | 全员          |
| POST   | `/api/python-scripts`               | 新增脚本                | admin 及以上   |
| PUT    | `/api/python-scripts/{id}`          | 编辑脚本（版本+1，备份本地文件）   | admin 及以上   |
| DELETE | `/api/python-scripts/{id}`          | 删除脚本                | super_admin |
| PATCH  | `/api/python-scripts/{id}/status`   | 切换状态                | admin 及以上   |
| POST   | `/api/python-scripts/{id}/test`     | **(v1.1.0 新增)** 测试 Python 脚本 | admin 及以上   |

**测试接口请求体**：可选 `{ "useDraft": false }`（暂不实现，预留）。

### 7.4 SQL 脚本管理接口（v1.1.0 新增）

| Method | Path                              | 说明                 | 权限          |
| ------ | --------------------------------- | ------------------ | ----------- |
| GET    | `/api/sql-scripts`                | SQL 脚本列表（分页+搜索）   | 全员          |
| GET    | `/api/sql-scripts/{id}`           | 获取 SQL 脚本详情        | 全员          |
| POST   | `/api/sql-scripts`                | 新增 SQL 脚本          | admin 及以上   |
| PUT    | `/api/sql-scripts/{id}`           | 编辑 SQL 脚本（版本+1）    | admin 及以上   |
| DELETE | `/api/sql-scripts/{id}`           | 删除 SQL 脚本          | super_admin |
| PATCH  | `/api/sql-scripts/{id}/status`    | 切换状态               | admin 及以上   |
| POST   | `/api/sql-scripts/{id}/test`      | 测试 SQL 脚本（调用外部 HTTP 接口） | admin 及以上   |

**示例 — 新增 SQL 脚本请求体**：

```json
{
  "sqlName": "每日订单数巡检",
  "module": "交易",
  "description": "统计昨日订单数低于阈值时告警",
  "owner": "zhangsan",
  "alertDingtalk": "https://oapi.dingtalk.com/robot/send?access_token=xxx",
  "alertPhone": "13800000000",
  "alertLevel": "L2",
  "alertSourceLog": "/var/log/order/check.log",
  "alertTemplate": "TEMPL_ORDER_DAILY",
  "alertGroup": "order-ops",
  "scheduleCron": "0 30 9 * * ?",
  "scheduleDesc": "每天 09:30",
  "threshold": 1000,
  "sqlContent": "SELECT COUNT(1) FROM t_order WHERE create_time >= CURDATE() - INTERVAL 1 DAY",
  "status": 1
}
```

**示例 — SQL 测试响应**：

```json
{
  "success": true,
  "durationMs": 412,
  "summary": { "rowCount": 1, "columns": ["cnt"], "preview": [[1234]] },
  "raw": { "code": 0, "msg": "ok", "data": [{ "cnt": 1234 }] }
}
```

### 7.5 审计日志接口

| Method | Path                     | 说明          |
| ------ | ------------------------ | ----------- |
| GET    | `/api/audit-logs`        | 日志列表（分页+搜索） |
| GET    | `/api/audit-logs/export` | 导出 Excel    |

### 7.6 服务监控接口

> 与 V1.0.0 7.5 一致，无变更。

### 7.7 系统配置接口（v1.1.0 新增）

| Method | Path                       | 说明                  | 权限          |
| ------ | -------------------------- | ------------------- | ----------- |
| GET    | `/api/sys-configs`         | 配置列表（按 keyPrefix 过滤） | super_admin |
| GET    | `/api/sys-configs/{key}`   | 获取单个配置              | super_admin |
| PUT    | `/api/sys-configs/{key}`   | 更新配置（热生效）           | super_admin |

---

## 8. 交互与 UI 规范

### 8.1 整体布局

- 顶部导航栏：Logo、系统名称、当前用户信息、退出按钮
- 左侧侧边栏：功能模块导航（支持收缩），"脚本管理"为可展开父节点，下挂"Python 脚本"、"SQL 脚本"
- 主内容区：面包屑 + 内容卡片

### 8.2 通用交互规范

| 场景      | 规范                              |
| ------- | ------------------------------- |
| 操作成功    | 右上角绿色 Toast 提示，3 秒自动消失          |
| 操作失败    | 右上角红色 Toast 提示，需手动关闭            |
| 删除操作    | 必须二次确认弹窗，含操作对象名称                |
| 表单校验    | 实时校验 + 提交校验，错误信息显示在字段下方         |
| 加载状态    | 列表、按钮均显示 Loading 状态，防止重复提交     |
| 空数据    | 展示空状态插图 + 提示文案                  |
| **测试触发** | 按钮进入 Loading 状态，结果抽屉滑出展示（v1.1.0） |

### 8.3 代码编辑器规范

- 编辑器：Monaco Editor（VS Code 同款）
- 主题：Dark（VS Code Dark+）
- 行号：显示
- 字体：JetBrains Mono, Fira Code（monospace）
- Tab 大小：4 空格
- 自动缩进：开启
- 查看模式：`readOnly: true`，禁用编辑
- **语言映射**：Python 模块 `language: 'python'`；SQL 模块 `language: 'sql'`

### 8.4 测试结果抽屉布局（v1.1.0 新增）

```
┌─ 测试结果 ─────────────────────────────── × ─┐
│ 脚本：sync_user.py    触发：14:32:08         │
│ 总耗时：327 ms         状态：● 失败            │
├─────────────────────────────────────────────┤
│ [ 输出 ]  [ 错误 ]  [ 原始响应 ]              │
│ ┌─────────────────────────────────────────┐ │
│ │ Traceback (most recent call last):      │ │
│ │   File "sync_user.py", line 12 ...      │ │
│ └─────────────────────────────────────────┘ │
├─────────────────────────────────────────────┤
│           [再次测试] [复制结果] [关闭]         │
└─────────────────────────────────────────────┘
```

### 8.5 服务监控 UI 布局

> 与 V1.0.0 8.4 一致，无变更。

---

## 9. 权限矩阵

| 功能                       | 超级管理员 | 普通管理员 | 只读用户 |
| ------------------------ |:-----:|:-----:|:----:|
| 用户列表查看                   | ✅     | ❌     | ❌    |
| 用户新增/修改/删除               | ✅     | ❌     | ❌    |
| Python 脚本列表/内容查看         | ✅     | ✅     | ✅    |
| Python 脚本新增/编辑           | ✅     | ✅     | ❌    |
| Python 脚本删除              | ✅     | ❌     | ❌    |
| **Python 脚本测试**          | ✅     | ✅     | ❌    |
| SQL 脚本列表/内容查看            | ✅     | ✅     | ✅    |
| SQL 脚本新增/编辑              | ✅     | ✅     | ❌    |
| SQL 脚本删除                 | ✅     | ❌     | ❌    |
| **SQL 脚本测试**             | ✅     | ✅     | ❌    |
| 审计日志查看                   | ✅     | ✅     | ✅    |
| 审计日志导出                   | ✅     | ✅     | ❌    |
| 监控总览查看                   | ✅     | ✅     | ✅    |
| 监控配置新增/修改                | ✅     | ✅     | ❌    |
| 监控配置删除                   | ✅     | ❌     | ❌    |
| 监控启停 / 立即检查              | ✅     | ✅     | ❌    |
| 告警查看                     | ✅     | ✅     | ✅    |
| 告警确认                     | ✅     | ✅     | ❌    |
| 告警导出                     | ✅     | ✅     | ❌    |
| 系统配置（`sys_config`）查看/编辑 | ✅     | ❌     | ❌    |

---

## 10. 验收标准

### 10.1 用户管理

- [ ] 可正常新增用户，密码加密存储
- [ ] 用户名唯一性校验有效
- [ ] 修改用户时不填密码，原密码不变
- [ ] 删除用户后用户无法登录
- [ ] 搜索功能按用户名模糊、注册时间范围正确过滤
- [ ] 分页功能正常

### 10.2 Python 脚本管理（v1.1.0 调整）

- [ ] 列表正确展示所有字段，**含告警等级 / 通知模版 / 通知群 / 告警日志来源**
- [ ] 新增/编辑表单可保存上述 4 个新字段
- [ ] 查看脚本页 Python 语法高亮正常
- [ ] 编辑脚本保存后版本号 +1，本地文件先备份再覆盖
- [ ] 编辑脚本操作写入审计日志（事件 `SCRIPT_EDIT`）
- [ ] 离开编辑页有未保存内容时有提示
- [ ] **`【测试】` 按钮可触发执行**，结果抽屉正确展示 stdout / stderr / exitCode / durationMs
- [ ] 测试操作写入审计日志（事件 `PYTHON_SCRIPT_TEST`），输出做截断
- [ ] 只读用户调用测试接口返回 403

### 10.3 SQL 脚本管理（v1.1.0 新增）

- [ ] 列表/查看/编辑/删除四件套功能正常
- [ ] SQL 语法高亮、关键字大写提示生效
- [ ] 新增/编辑表单可保存所有字段（含 `threshold`、4 个告警字段）
- [ ] 编辑后版本号 +1，写入审计日志（`SQL_SCRIPT_UPDATE`）
- [ ] **`【测试】` 按钮调用配置的 HTTP 接口**，结果抽屉正确展示
- [ ] 测试操作写入审计日志（`SQL_SCRIPT_TEST`）
- [ ] 修改 `sys_config` 中的 endpoint / headers 后，无需重启即生效
- [ ] 只读用户调用测试接口返回 403

### 10.4 审计日志

- [ ] 用户增删改操作均产生审计记录
- [ ] Python / SQL 脚本编辑、测试操作均产生审计记录
- [ ] 监控配置增删改与告警/确认操作均产生审计记录
- [ ] 密码字段在审计记录中脱敏（显示为 `***`）
- [ ] 时间、事件类型、操作人搜索过滤正确
- [ ] 导出 Excel 文件数据与列表一致

### 10.5 服务监控

> 与 V1.0.0 10.4 一致，无变更。

---

## 11. 里程碑计划

| 阶段     | 内容                                                                  | 预计时长          |
| ------ | ------------------------------------------------------------------- | ------------- |
| 阶段一    | 数据库迁移：`script_info` 增 4 字段，新增 `sql_info` / `sys_config`               | 0.5 天         |
| 阶段二    | 后端：Python 脚本 4 字段补全 + `PythonScriptTester` + `/test` 接口 + 审计扩展      | 1.5 天         |
| 阶段三    | 后端：SQL 脚本 CRUD + `SqlHttpTester` + `/test` 接口 + 审计扩展                 | 2 天           |
| 阶段四    | 后端：`sys_config` CRUD + 配置热加载                                        | 0.5 天         |
| 阶段五    | 前端：菜单调整为父子结构；Python 脚本表单/列表新增 4 字段                                  | 1 天           |
| 阶段六    | 前端：SQL 脚本子模块（列表/查看/编辑/抽屉）                                          | 2 天           |
| 阶段七    | 前端：通用"测试结果抽屉"组件 + Python/SQL 接入                                     | 1.5 天         |
| 阶段八    | 联调、测试、Bug 修复、上线                                                     | 1 天           |
| **合计** |                                                                     | **约 10 个工作日** |

> V1.0.0 已交付的服务监控、审计、用户管理本期不重复投入。

---

## 12. 风险与约束

| 风险                       | 影响              | 应对措施                                                                       |
| ------------------------ | --------------- | -------------------------------------------------------------------------- |
| 脚本文件读写权限                 | 编辑脚本/Python 测试失败 | 后端服务以具备文件读写权限的用户运行；测试时 `cwd` 设为脚本所在目录；记录 stderr 便于排查                       |
| Python 测试长时间执行           | 占用线程池、影响系统      | 单次默认 60 秒超时；专用 `ThreadPoolTaskExecutor`，并发上限默认 10；超时强制 `process.destroyForcibly()` |
| Python 测试可能产生副作用         | 误改生产数据          | 文档与 UI 提示用户："测试 = 真实执行"；建议用户在脚本顶部支持 `--dry-run` 自检                          |
| **SQL 测试外部接口不可达**         | 测试不可用           | 接口超时 30 秒，捕获网络异常返回友好错误；UI 抽屉提示运维检查 `sys_config` 中的 endpoint                |
| **SQL 测试结果体积过大**          | 内存/前端卡顿         | 后端对响应体做 256KB 截断，前端 raw 区折叠展示                                              |
| **SQL 误执行写操作**            | 数据被改            | 平台仅传递 SQL 文本，由外部执行平台做语句白名单（DDL/DML 拦截），平台层补充审计                            |
| 并发编辑冲突                   | 脚本内容覆盖          | 引入乐观锁机制（基于版本号），保存时校验版本                                                     |
| 审计/检查记录增长过快              | 存储压力            | 审计 180 天、检查记录 30 天 自动清理                                                    |
| 监控调度抖动                   | 巡检不准时           | 限制单实例监控数（≤ 500），使用专用线程池                                                    |
| 告警风暴                     | 钉钉群/邮箱被打爆       | 抑制策略 + 升级合并                                                                |
| 三方语音平台接口变化              | 电话告警失败          | 通过 `IAlertChannel` 抽象，可热切换                                                 |
| 内网监控对象网络可达性             | 检查失败误报          | 部署多 Probe（后续扩展）                                                            |

---

## 附录 A：v1.1.0 改动清单（开发自检）

| 类别 | 改动项                                                                                          |
| -- | -------------------------------------------------------------------------------------------- |
| DB | `script_info` 新增 `alert_level` / `alert_source_log` / `alert_template` / `alert_group` 4 列 + 索引 |
| DB | 新建 `sql_info` 表                                                                              |
| DB | 新建 `sys_config` 表 + 默认数据初始化                                                                  |
| 后端 | `ScriptInfo` 实体 / DTO / Mapper XML / Service 同步 4 个新字段                                       |
| 后端 | 新增 `SqlInfo` 实体 / Mapper / Service / Controller / DTO 全套                                     |
| 后端 | 新增 `IScriptTester` 接口 + `PythonProcessTester` + `SqlHttpTester`                              |
| 后端 | `POST /api/python-scripts/{id}/test`、`POST /api/sql-scripts/{id}/test`                       |
| 后端 | `SysConfig` 实体 + Service + Controller + 热更新事件                                                |
| 后端 | `AuditEventType` 枚举新增 `PYTHON_SCRIPT_TEST` / `SQL_SCRIPT_*`                                  |
| 前端 | 路由 `/scripts/python/*` / `/scripts/sql/*`，旧 `/scripts/*` 跳转到 Python 子菜单                       |
| 前端 | 侧边栏菜单改为可展开父节点                                                                                |
| 前端 | `PythonScriptForm.vue` 增加 4 字段输入                                                             |
| 前端 | 新增 `SqlScriptList.vue` / `SqlScriptView.vue` / `SqlScriptEdit.vue` / `SqlScriptForm.vue`     |
| 前端 | 通用组件 `TestResultDrawer.vue`                                                                  |
| 前端 | API 模块 `api/pythonScript.js` / `api/sqlScript.js` / `api/sysConfig.js`                       |
| 配置 | `application.yml` 新增 `script.test.python.*` / `script.test.sql.*`                            |
