# Python 在线脚本管理平台 — 产品需求文档（PRD）

> **版本**：v1.1.0
> **创建日期**：2026-05-12
> **更新日期**：2026-05-25（新增 4.4 服务监控模块）
> **文档状态**：评审稿
> **产品负责人**：待定

---

## 目录

1. [项目背景与目标](#1-项目背景与目标)
2. [用户角色](#2-用户角色)
3. [整体架构与页面结构](#3-整体架构与页面结构)
4. [功能模块详细说明](#4-功能模块详细说明)
   - 4.1 [用户管理模块](#41-用户管理模块)
   - 4.2 [脚本管理模块](#42-脚本管理模块)
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

随着业务规模增长，维护的 Python 自动化脚本数量持续增加，且依赖的对外服务越来越多，当前面临以下痛点：

| 痛点         | 描述                                  |
| ---------- | ----------------------------------- |
| 脚本分散       | 脚本散落在各服务器目录，缺乏统一入口管理                |
| 版本混乱       | 无法追溯脚本修改历史，出现问题难以回滚                 |
| 责任不清       | 脚本负责人信息缺失，故障时无法快速联系                 |
| 安全隐患       | 任何人均可随意修改脚本，无操作审计                   |
| 监控缺失       | 脚本与依赖服务的预警信息（钉钉、电话、邮件）无统一配置         |
| 故障感知滞后     | 服务可用性、响应时间无主动巡检，问题发生后才被动处理          |

### 1.2 目标

构建一个 **Python 在线脚本管理平台 + 服务监控平台**，实现：

- 集中化管理所有 Python 脚本的元信息与内容
- 提供在线查看与编辑能力，支持语法高亮
- 完善的用户权限体系，保障操作安全
- 全操作审计追踪，满足合规要求
- 配置脚本预警通道（钉钉 + 电话 + 邮件），提升响应效率
- 对关键依赖服务进行**主动巡检 + 多通道告警**，类 Apple System Status 的可视化总览

---

## 2. 用户角色

| 角色        | 说明       | 主要权限                          |
| --------- | -------- | ----------------------------- |
| **超级管理员** | 系统最高权限用户 | 用户管理、脚本管理、审计查看、监控配置、系统配置      |
| **普通管理员** | 日常运维人员   | 脚本查看/编辑、审计查看、监控查看/配置、告警确认     |
| **只读用户**  | 观察者角色    | 脚本查看、审计查看、监控状态查看（只读）          |

---

## 3. 整体架构与页面结构

```
Python 脚本管理平台
├── 登录页
└── 主控台（侧边栏导航）
    ├── 首页 Dashboard（脚本数 / 用户数 / 监控状态汇总）
    ├── 用户管理
    │   ├── 用户列表
    │   ├── 新增用户（弹窗/抽屉）
    │   └── 编辑用户（弹窗/抽屉）
    ├── 脚本管理
    │   ├── 脚本列表
    │   ├── 查看脚本（代码预览页）
    │   └── 编辑脚本（在线编辑器页）
    ├── 审计日志
    │   └── 审计日志列表
    └── 服务监控
        ├── 监控总览（Apple 风格状态页 - 默认页）
        ├── 监控配置列表
        ├── 新增 / 编辑监控（抽屉）
        ├── 监控详情（趋势图 + 检查历史）
        └── 告警记录
```

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
| `password`   | varchar(255) | ✅    | 密码，采用 bcrypt 加密存储，前端不展示                 |
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

#### 4.2.1 功能概述

提供 Python 脚本的 **元信息管理**、**在线内容查看** 和 **在线代码编辑** 能力。

#### 4.2.2 脚本信息数据模型

| 字段名              | 类型           | 必填   | 说明                             |
| ---------------- | ------------ | ---- | ------------------------------ |
| `id`             | bigint       | 系统生成 | 主键，自增                          |
| `script_name`    | varchar(128) | ✅    | 脚本名称                           |
| `script_path`    | varchar(512) | ✅    | 脚本在服务器上的绝对路径                   |
| `module`         | varchar(64)  | ✅    | 归属模块（如：数据同步、报表生成、监控告警）         |
| `description`    | text         | ✅    | 功能说明，描述脚本业务用途                  |
| `owner`          | varchar(64)  | ✅    | 负责人（关联用户名）                     |
| `alert_dingtalk` | varchar(256) | ❌    | 预警钉钉 Webhook URL 或群名称          |
| `alert_phone`    | varchar(64)  | ❌    | 预警电话号码                         |
| `schedule_cron`  | varchar(64)  | ❌    | 调度频率（Cron 表达式，如 `0 */1 * * *`） |
| `schedule_desc`  | varchar(128) | ❌    | 调度频率人读描述（如：每小时执行一次）            |
| `script_content` | longtext     | ✅    | 脚本内容（Python 代码）                |
| `version`        | int          | 系统生成 | 版本号，每次编辑 +1，初始为 1              |
| `status`         | tinyint      | ✅    | 1=启用，0=禁用                      |
| `created_at`     | datetime     | 系统生成 | 创建时间                           |
| `updated_at`     | datetime     | 系统生成 | 最后修改时间                         |

#### 4.2.3 脚本列表页

**搜索条件区：**

| 搜索字段 | 控件类型          |
| ---- | ------------- |
| 脚本名称 | 文本框（模糊搜索）     |
| 归属模块 | 下拉框（动态加载已有模块） |
| 负责人  | 文本框（模糊搜索）     |
| 状态   | 下拉框           |

**列表表格列定义：**

| 列名   | 字段               | 展示说明                   |
| ---- | ---------------- | ---------------------- |
| 序号   | —                | 行号                     |
| 脚本名称 | `script_name`    | 文字                     |
| 脚本路径 | `script_path`    | 文字，超长时 Tooltip 展示完整路径  |
| 功能说明 | `description`    | 文字，超 50 字符省略 + Tooltip |
| 归属模块 | `module`         | Tag 标签                 |
| 负责人  | `owner`          | 文字                     |
| 预警钉钉 | `alert_dingtalk` | 文字，无则展示 `—`            |
| 预警电话 | `alert_phone`    | 文字，无则展示 `—`            |
| 调度频率 | `schedule_desc`  | 文字，无则展示 `—`            |
| 版本   | `version`        | `v{n}` 格式              |
| 状态   | `status`         | Tag                    |
| 操作   | —                | `【查看】` `【编辑】`          |

**分页：** 默认每页 20 条。

#### 4.2.4 查看脚本页

- 展示脚本 **完整内容**，使用代码编辑器（只读模式，如 Monaco Editor / CodeMirror）
- 语法高亮：Python 语法
- 右上角展示：脚本名称、版本号、最后更新时间、负责人
- 提供 `【复制代码】` 按钮
- 提供 `【返回列表】` 按钮
- 左侧/顶部显示元信息面板（路径、归属模块、调度频率、预警钉钉、预警电话）

#### 4.2.5 编辑脚本页

- 使用在线代码编辑器（Monaco Editor，支持 Python 语法高亮 + 自动缩进）
- 编辑器上方展示脚本元信息（只读展示）
- 支持编辑字段：
  - 脚本内容（代码编辑区）
  - 功能说明
  - 归属模块
  - 负责人
  - 预警钉钉
  - 预警电话
  - 调度频率
  - 状态
- 保存时：版本号 +1，记录操作人和时间，写入审计日志，并且保存到对应的本地目录上，先备份原来的这个文件，再覆盖写入
- 底部按钮：`【保存】` `【取消】`
- 离开页面时如有未保存内容，弹出提示："您有未保存的更改，确定离开？"

---

### 4.3 审计日志模块

#### 4.3.1 功能概述

记录平台所有 **写操作**（增删改），提供操作追溯能力，满足安全合规要求。

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

| 事件值                | 描述         |
| ------------------ | ---------- |
| `USER_CREATE`      | 新增用户       |
| `USER_UPDATE`      | 修改用户       |
| `USER_DELETE`      | 删除用户       |
| `USER_LOGIN`       | 用户登录       |
| `USER_LOGOUT`      | 用户登出       |
| `SCRIPT_VIEW`      | 查看脚本（可选记录） |
| `SCRIPT_EDIT`      | 编辑脚本       |
| `SCRIPT_CREATE`    | 新增脚本       |
| `SCRIPT_DELETE`    | 删除脚本       |
| `MONITOR_CREATE`   | 新增监控配置     |
| `MONITOR_UPDATE`   | 修改监控配置     |
| `MONITOR_DELETE`   | 删除监控配置     |
| `MONITOR_TEST`     | 手动触发一次检查   |
| `MONITOR_ALERT`    | 触发告警       |
| `MONITOR_ALERT_ACK`| 告警确认       |

#### 4.3.3 审计日志列表页

**搜索条件区：**

| 搜索字段 | 控件类型    | 说明           |
| ---- | ------- | ------------ |
| 操作时间 | 日期范围选择器 | 起止日期时间       |
| 事件类型 | 下拉多选框   | 支持多选         |
| 操作人  | 文本输入框   | 精确搜索         |
| 操作结果 | 下拉框     | 全部 / 成功 / 失败 |

**列表表格列定义：**

| 列名     | 字段                | 展示说明                     |
| ------ | ----------------- | ------------------------ |
| 序号     | —                 | 行号                       |
| 操作时间   | `event_time`      | `YYYY-MM-DD HH:mm:ss`    |
| 事件类型   | `event_type`      | 中文 Tag 展示（不同类型不同颜色）      |
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

#### 4.4.1 功能概述

对外部依赖服务（HTTP API、网关、数据库、内网 TCP 服务等）进行 **主动巡检**，提供：

- 类 Apple System Status 的全局状态总览页，一眼看清所有服务健康度
- 监控配置 CRUD（地址、频率、阈值、负责人、告警通道）
- 周期性调度执行检查并落库
- 触发阈值时通过 **钉钉 / 邮箱 / 电话** 多通道告警
- 告警确认与抑制策略，避免告警风暴
- 30 / 90 天可用率（SLA）统计与响应时间趋势图

#### 4.4.2 状态分级

| 状态                | 颜色   | 含义                                  |
| ----------------- | ---- | ----------------------------------- |
| `OPERATIONAL`     | 绿色 ●  | 正常，所有最近检查均通过且响应时间正常                 |
| `DEGRADED`        | 黄色 ●  | 性能下降，检查通过但响应时间持续超过响应时间阈值            |
| `PARTIAL_OUTAGE`  | 橙色 ●  | 部分中断，最近检查存在间歇失败（失败比例 ≥ 30% 且未达连续阈值） |
| `MAJOR_OUTAGE`    | 红色 ●  | 完全中断，连续失败次数 ≥ 配置阈值（默认 3）            |
| `MAINTENANCE`     | 蓝色 ●  | 维护中，由人工置位，期间不告警                     |

> 全局状态：取所有启用监控中"最严重"状态作为顶部全局状态。

#### 4.4.3 检查类型

| 类型     | 说明                                       | 关键参数                  |
| ------ | ---------------------------------------- | --------------------- |
| `HTTP` | HTTP/HTTPS GET/POST 请求，校验状态码 + 响应时间 + 关键字 | URL、Method、Header、Body |
| `TCP`  | 建立 TCP 连接，校验是否连通                          | host:port             |
| `PING` | ICMP Ping                                | host                  |
| `CMD`  | 执行平台预置脚本（受限白名单），通过返回码判断                   | 内置 cmd 标识             |

#### 4.4.4 服务监控配置数据模型 `monitor_config`

| 字段名                  | 类型            | 必填   | 说明                                              |
| -------------------- | ------------- | ---- | ----------------------------------------------- |
| `id`                 | bigint        | 系统生成 | 主键，自增                                           |
| `system_name`        | varchar(128)  | ✅    | 系统/服务名称（如：订单系统、风控网关）                            |
| `module`             | varchar(64)   | ❌    | 业务分组（用于总览页分组展示）                                 |
| `check_type`         | varchar(16)   | ✅    | `HTTP` / `TCP` / `PING` / `CMD`                 |
| `target_url`         | varchar(512)  | ✅    | 检查地址（HTTP 为 URL；TCP 为 host:port；PING 为 host）    |
| `http_method`        | varchar(8)    | ❌    | HTTP 方法，默认 `GET`                                |
| `http_headers`       | text          | ❌    | HTTP 请求头（JSON）                                  |
| `http_body`          | text          | ❌    | HTTP 请求体                                        |
| `expected_status`    | varchar(64)   | ❌    | 期望状态码，支持 `200`、`2xx`、`200,201` 多个               |
| `expected_keyword`   | varchar(256)  | ❌    | 响应体应包含的关键字（命中即通过）                               |
| `check_interval_sec` | int           | ✅    | 检查频率（秒），最小 30，默认 60                             |
| `timeout_sec`        | int           | ✅    | 单次超时时间（秒），默认 5                                  |
| `rt_threshold_ms`    | int           | ❌    | 响应时间阈值（ms），超过即标记 `DEGRADED`                     |
| `fail_threshold`     | int           | ✅    | 连续失败次数 ≥ 该值触发告警，默认 3                            |
| `recovery_threshold` | int           | ✅    | 连续成功次数 ≥ 该值视为恢复，默认 2                            |
| `alert_channels`     | varchar(64)   | ❌    | 启用通道，逗号分隔：`dingtalk,email,phone`                |
| `alert_dingtalk`     | varchar(512)  | ❌    | 钉钉机器人 Webhook URL                               |
| `alert_email`        | varchar(256)  | ❌    | 告警接收邮箱（多个用逗号分隔）                                 |
| `alert_phone`        | varchar(128)  | ❌    | 告警电话号码（多个用逗号分隔）                                 |
| `suppress_minutes`   | int           | ✅    | 同一告警抑制时长（分钟），默认 10                              |
| `owner`              | varchar(64)   | ✅    | 系统负责人（用户名）                                      |
| `alert_receiver`     | varchar(256)  | ✅    | 告警接收人姓名（用于钉钉 @ 与告警卡片展示）                         |
| `current_status`     | varchar(32)   | 系统维护 | 当前状态枚举                                          |
| `last_check_time`    | datetime      | 系统维护 | 最近一次检查时间                                        |
| `last_rt_ms`         | int           | 系统维护 | 最近一次响应时间                                        |
| `consecutive_fail`   | int           | 系统维护 | 当前连续失败次数                                        |
| `consecutive_succ`   | int           | 系统维护 | 当前连续成功次数                                        |
| `enabled`            | tinyint       | ✅    | 1=启用调度，0=暂停                                     |
| `created_at`         | datetime      | 系统生成 | 创建时间                                            |
| `updated_at`         | datetime      | 系统生成 | 最后修改时间                                          |
| `remark`             | varchar(256)  | ❌    | 备注                                              |

#### 4.4.5 检查历史数据模型 `monitor_check_record`

| 字段名             | 类型            | 说明                              |
| --------------- | ------------- | ------------------------------- |
| `id`            | bigint        | 主键，自增                           |
| `monitor_id`    | bigint        | 关联 `monitor_config.id`          |
| `check_time`    | datetime      | 本次检查时间                          |
| `success`       | tinyint       | 1=成功，0=失败                       |
| `rt_ms`         | int           | 响应时间（ms），失败/超时则记 -1             |
| `http_status`   | int           | HTTP 状态码（HTTP 类型）               |
| `error_message` | varchar(1024) | 失败原因（超时、连接拒绝、关键字未命中等）           |
| `created_at`    | datetime      | 入库时间                            |

> **保留策略**：30 天，按天分区或定时任务清理。

#### 4.4.6 告警记录数据模型 `monitor_alert`

| 字段名             | 类型            | 说明                                       |
| --------------- | ------------- | ---------------------------------------- |
| `id`            | bigint        | 主键，自增                                    |
| `monitor_id`    | bigint        | 关联 `monitor_config.id`                   |
| `system_name`   | varchar(128)  | 冗余系统名                                    |
| `alert_level`   | varchar(16)   | `WARN` / `CRITICAL`                      |
| `alert_reason`  | varchar(512)  | 触发原因（如：连续 3 次超时；HTTP 503）                |
| `start_time`    | datetime      | 告警起始时间                                   |
| `recovery_time` | datetime      | 恢复时间，未恢复为 NULL                           |
| `channels_sent` | varchar(64)   | 实际发送的通道（逗号分隔）                            |
| `send_result`   | text          | 各通道发送结果 JSON                             |
| `acked`         | tinyint       | 是否已确认 1=已确认 0=未确认                        |
| `acked_by`      | varchar(64)   | 确认人                                      |
| `acked_at`      | datetime      | 确认时间                                     |
| `created_at`    | datetime      | 入库时间                                     |

#### 4.4.7 调度与状态机

- 后端基于 Spring `@Scheduled` 启动一个**主调度器**，每 5 秒扫描一次 `monitor_config` 中 `enabled = 1` 的记录，根据 `check_interval_sec` 与 `last_check_time` 决定是否触发本轮检查
- 实际执行通过 `ThreadPoolTaskExecutor` 并发，单监控的检查不阻塞其他监控
- 每次检查完成后：
  1. 写入 `monitor_check_record`
  2. 更新 `monitor_config` 的 `last_check_time` / `last_rt_ms` / `consecutive_fail` / `consecutive_succ` / `current_status`
  3. 状态变更时触发告警/恢复通知

**状态机伪代码：**

```
on check_result(success, rt_ms):
    if success:
        consecutive_succ += 1
        consecutive_fail = 0
        if rt_threshold_ms 设置 且 rt_ms > rt_threshold_ms:
            status = DEGRADED
        else:
            status = OPERATIONAL
        if 之前是告警状态 且 consecutive_succ >= recovery_threshold:
            发送恢复通知，关闭最近未恢复的告警
    else:
        consecutive_fail += 1
        consecutive_succ = 0
        if consecutive_fail >= fail_threshold:
            status = MAJOR_OUTAGE
            若距离上次告警时间 >= suppress_minutes：触发告警
        elif consecutive_fail > 0:
            status = PARTIAL_OUTAGE
```

#### 4.4.8 告警通道实现

| 通道       | 实现方式                                                  |
| -------- | ----------------------------------------------------- |
| 钉钉       | 调用配置的 Webhook，发送 Markdown 卡片，包含系统名、错误原因、时间、@ 接收人      |
| 邮箱       | Spring Mail（SMTP），HTML 模板（标题色按告警级别）                   |
| 电话       | 对接第三方语音平台（如阿里云语音通知 / 容联云），通过 `IAlertCaller` 接口实现，可替换 |

> **抑制策略**：同一 `monitor_id` 在一次故障未恢复期间，每 `suppress_minutes` 分钟最多发送 1 次告警；恢复后重置。

#### 4.4.9 监控总览页（默认页）

仿 Apple System Status：

- 顶部显示**全局状态横幅**（绿/黄/橙/红/蓝），文案如"All Services Operational"
- 按 `module` 字段分组，每组一张卡片
- 每行一条服务，左侧服务名 + 当前状态圆点；右侧最近 90 天可用率小色块（每天 1 格，绿色=>99.9%，黄色>=99%，红色<99%）
- 鼠标悬停色块显示该天可用率
- 点击行进入【监控详情页】

**搜索/筛选**：状态、关键字（系统名）。

**右上角操作**：`【刷新】`、`【监控配置】`、`【告警记录】`。

#### 4.4.10 监控配置列表页

**搜索条件**：系统名、负责人、状态、检查类型、启用/暂停。

**操作按钮**：`【+ 新增监控】`、`【批量启用】`、`【批量暂停】`。

**列定义**：

| 列名      | 字段                   | 说明                            |
| ------- | -------------------- | ----------------------------- |
| 序号      | —                    | 行号                            |
| 系统名     | `system_name`        | 文字                            |
| 检查类型    | `check_type`         | Tag                           |
| 检查地址    | `target_url`         | 文字（超长 Tooltip）                |
| 频率      | `check_interval_sec` | 秒，转换为 "每 N 秒/分"               |
| RT 阈值   | `rt_threshold_ms`    | ms                            |
| 失败阈值    | `fail_threshold`     | 次                             |
| 当前状态    | `current_status`     | 彩色 Tag                        |
| 最近响应    | `last_rt_ms`         | ms / "-"                      |
| 最近检查    | `last_check_time`    | `YYYY-MM-DD HH:mm:ss`         |
| 负责人     | `owner`              | 文字                            |
| 告警人     | `alert_receiver`     | 文字                            |
| 调度状态    | `enabled`            | Switch（启用/暂停）                 |
| 操作      | —                    | `【查看】`/`【编辑】`/`【立即检查】`/`【删除】` |

#### 4.4.11 新增 / 编辑监控（抽屉式）

按 `4.4.4` 数据模型分组展示：

1. **基础信息**：系统名、模块、检查类型、检查地址、HTTP 方法/Header/Body（仅 HTTP）
2. **频率与超时**：检查频率、超时时间
3. **判定阈值**：期望状态码、期望关键字、响应时间阈值、连续失败阈值、连续恢复阈值
4. **告警通道**：钉钉 Webhook、邮箱、电话；抑制时长
5. **责任人**：系统负责人、告警接收人
6. **其他**：启用开关、备注

**校验**：
- HTTP 类型必须填合法 URL；TCP 类型校验 `host:port` 格式
- `check_interval_sec` ≥ 30，且 ≥ `timeout_sec`
- 至少启用一个告警通道（启用后才能保存"启用"状态，否则提示）

#### 4.4.12 监控详情页

- 顶部：系统名 + 当前状态 + 30/90 天可用率 + `【立即检查】` `【编辑】` `【暂停/启用】`
- 中部：**响应时间趋势图**（最近 24h / 7d / 30d 切换），用 ECharts 折线图，失败点用红点标记
- 底部 Tab：
  - **检查历史**：分页表格（时间、成功/失败、响应时间、HTTP 状态码、错误原因）
  - **告警历史**：分页表格（时间、级别、原因、通道、是否恢复、确认人）

#### 4.4.13 告警记录页

| 列名      | 字段              | 说明                  |
| ------- | --------------- | ------------------- |
| 序号      | —               | 行号                  |
| 系统名     | `system_name`   | 文字                  |
| 级别      | `alert_level`   | `WARN` / `CRITICAL` |
| 原因      | `alert_reason`  | 文字                  |
| 起始时间    | `start_time`    | 时间                  |
| 恢复时间    | `recovery_time` | 时间（未恢复显示"持续中"）      |
| 通道      | `channels_sent` | Tag 列表              |
| 是否确认    | `acked`         | 是 / 否               |
| 确认人/时间  | —               | 文字                  |
| 操作      | —               | `【确认】`、`【查看详情】`     |

**搜索条件**：系统名、级别、起止时间、是否已确认。

#### 4.4.14 平台告警管理后台配置

由于电话告警依赖三方平台，密钥/AppId 等通过 **配置文件 + 数据库 `sys_config`**（可后台维护）双重支持，运维可在不重启服务的情况下切换或更新供应商。

---

## 5. 非功能性需求

### 5.1 性能要求

| 指标                 | 要求                  |
| ------------------ | ------------------- |
| 页面首屏加载             | ≤ 3 秒（正常网络）         |
| 列表查询响应             | ≤ 1 秒（数据量 ≤ 10 万条）  |
| 脚本保存响应             | ≤ 2 秒               |
| 监控调度抖动             | ≤ 5 秒（约定频率为 ±5s 内执行） |
| 监控总览页加载（≤ 200 个服务） | ≤ 2 秒               |
| 并发用户数              | 支持 50 人同时在线         |
| 单实例可承载监控数          | ≥ 500 条（HTTP 1 分钟级） |

### 5.2 安全要求

- 密码存储：**bcrypt** 算法加密，cost factor ≥ 10
- 接口认证：**JWT Token**，有效期 8 小时，支持刷新
- 防止 SQL 注入：使用 ORM 或参数化查询
- 防止 XSS：前端内容输出转义
- 脚本与监控配置传输：HTTPS 加密
- 敏感字段（密码、Webhook、电话号码）在审计日志中**部分脱敏**（钉钉 Webhook 截短中间，电话保留前三后四）

### 5.3 可用性要求

- 系统可用率 ≥ 99.5%
- 支持浏览器：Chrome ≥ 90、Edge ≥ 90、Firefox ≥ 88

### 5.4 可扩展性

- 前后端分离架构
- RESTful API 设计，便于后续对接其他系统
- 检查类型与告警通道均通过策略接口（`IChecker` / `IAlertChannel`）实现，便于新增类型
- 监控调度后续可平滑替换为 XXL-Job 等分布式调度

---

## 6. 数据库表结构设计

### 6.1 用户表 `sys_user`

```sql
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
  `password`    VARCHAR(255) NOT NULL COMMENT '加密密码(bcrypt)',
  `role`        VARCHAR(32)  NOT NULL DEFAULT 'admin' COMMENT '角色',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 6.2 脚本表 `script_info`

```sql
CREATE TABLE `script_info` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `script_name`     VARCHAR(128)  NOT NULL COMMENT '脚本名称',
  `script_path`     VARCHAR(512)  NOT NULL COMMENT '脚本路径',
  `module`          VARCHAR(64)   NOT NULL COMMENT '归属模块',
  `description`     TEXT          COMMENT '功能说明',
  `owner`           VARCHAR(64)   NOT NULL COMMENT '负责人',
  `alert_dingtalk`  VARCHAR(256)  DEFAULT NULL COMMENT '预警钉钉',
  `alert_phone`     VARCHAR(64)   DEFAULT NULL COMMENT '预警电话',
  `schedule_cron`   VARCHAR(64)   DEFAULT NULL COMMENT 'Cron表达式',
  `schedule_desc`   VARCHAR(128)  DEFAULT NULL COMMENT '调度频率描述',
  `script_content`  LONGTEXT      NOT NULL COMMENT '脚本内容',
  `version`         INT           NOT NULL DEFAULT 1 COMMENT '版本号',
  `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_owner`  (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脚本信息表';
```

### 6.3 审计日志表 `audit_log`

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

### 6.4 服务监控配置表 `monitor_config`

```sql
CREATE TABLE `monitor_config` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `system_name`        VARCHAR(128) NOT NULL COMMENT '系统名',
  `module`             VARCHAR(64)  DEFAULT NULL COMMENT '业务分组',
  `check_type`         VARCHAR(16)  NOT NULL COMMENT '检查类型 HTTP/TCP/PING/CMD',
  `target_url`         VARCHAR(512) NOT NULL COMMENT '检查地址',
  `http_method`        VARCHAR(8)   DEFAULT 'GET' COMMENT 'HTTP方法',
  `http_headers`       TEXT         DEFAULT NULL COMMENT 'HTTP请求头JSON',
  `http_body`          TEXT         DEFAULT NULL COMMENT 'HTTP请求体',
  `expected_status`    VARCHAR(64)  DEFAULT NULL COMMENT '期望状态码',
  `expected_keyword`   VARCHAR(256) DEFAULT NULL COMMENT '期望关键字',
  `check_interval_sec` INT          NOT NULL DEFAULT 60 COMMENT '检查频率(秒)',
  `timeout_sec`        INT          NOT NULL DEFAULT 5 COMMENT '超时(秒)',
  `rt_threshold_ms`    INT          DEFAULT NULL COMMENT '响应时间阈值(ms)',
  `fail_threshold`     INT          NOT NULL DEFAULT 3 COMMENT '连续失败阈值',
  `recovery_threshold` INT          NOT NULL DEFAULT 2 COMMENT '连续恢复阈值',
  `alert_channels`     VARCHAR(64)  DEFAULT NULL COMMENT '启用通道',
  `alert_dingtalk`     VARCHAR(512) DEFAULT NULL COMMENT '钉钉Webhook',
  `alert_email`        VARCHAR(256) DEFAULT NULL COMMENT '告警邮箱',
  `alert_phone`        VARCHAR(128) DEFAULT NULL COMMENT '告警电话',
  `suppress_minutes`   INT          NOT NULL DEFAULT 10 COMMENT '抑制分钟',
  `owner`              VARCHAR(64)  NOT NULL COMMENT '系统负责人',
  `alert_receiver`     VARCHAR(256) NOT NULL COMMENT '告警接收人',
  `current_status`     VARCHAR(32)  NOT NULL DEFAULT 'OPERATIONAL' COMMENT '当前状态',
  `last_check_time`    DATETIME     DEFAULT NULL COMMENT '最近检查时间',
  `last_rt_ms`         INT          DEFAULT NULL COMMENT '最近响应时间ms',
  `consecutive_fail`   INT          NOT NULL DEFAULT 0,
  `consecutive_succ`   INT          NOT NULL DEFAULT 0,
  `enabled`            TINYINT      NOT NULL DEFAULT 1 COMMENT '调度启停',
  `remark`             VARCHAR(256) DEFAULT NULL,
  `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner`  (`owner`),
  KEY `idx_status` (`current_status`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务监控配置表';
```

### 6.5 监控检查记录表 `monitor_check_record`

```sql
CREATE TABLE `monitor_check_record` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `monitor_id`    BIGINT        NOT NULL COMMENT '监控配置ID',
  `check_time`    DATETIME      NOT NULL COMMENT '检查时间',
  `success`       TINYINT       NOT NULL COMMENT '1成功0失败',
  `rt_ms`         INT           DEFAULT NULL COMMENT '响应时间ms',
  `http_status`   INT           DEFAULT NULL COMMENT 'HTTP状态码',
  `error_message` VARCHAR(1024) DEFAULT NULL COMMENT '失败原因',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_monitor_time` (`monitor_id`, `check_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控检查历史';
```

### 6.6 监控告警记录表 `monitor_alert`

```sql
CREATE TABLE `monitor_alert` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `monitor_id`     BIGINT        NOT NULL,
  `system_name`    VARCHAR(128)  NOT NULL,
  `alert_level`    VARCHAR(16)   NOT NULL COMMENT 'WARN/CRITICAL',
  `alert_reason`   VARCHAR(512)  NOT NULL,
  `start_time`     DATETIME      NOT NULL,
  `recovery_time`  DATETIME      DEFAULT NULL,
  `channels_sent`  VARCHAR(64)   DEFAULT NULL,
  `send_result`    TEXT          DEFAULT NULL,
  `acked`          TINYINT       NOT NULL DEFAULT 0,
  `acked_by`       VARCHAR(64)   DEFAULT NULL,
  `acked_at`       DATETIME      DEFAULT NULL,
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_monitor` (`monitor_id`),
  KEY `idx_start`   (`start_time`),
  KEY `idx_acked`   (`acked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控告警记录';
```

---

## 7. 接口设计（RESTful API）

### 7.1 认证接口

| Method | Path                | 说明        |
| ------ | ------------------- | --------- |
| POST   | `/api/auth/login`   | 登录，返回 JWT |
| POST   | `/api/auth/logout`  | 登出        |
| POST   | `/api/auth/refresh` | 刷新 Token  |

### 7.2 用户管理接口

| Method | Path                     | 说明          |
| ------ | ------------------------ | ----------- |
| GET    | `/api/users`             | 用户列表（分页+搜索） |
| POST   | `/api/users`             | 新增用户        |
| PUT    | `/api/users/{id}`        | 修改用户        |
| DELETE | `/api/users/{id}`        | 删除用户        |
| PATCH  | `/api/users/{id}/status` | 切换状态        |

### 7.3 脚本管理接口

| Method | Path                       | 说明          |
| ------ | -------------------------- | ----------- |
| GET    | `/api/scripts`             | 脚本列表（分页+搜索） |
| GET    | `/api/scripts/{id}`        | 获取脚本详情（含内容） |
| POST   | `/api/scripts`             | 新增脚本        |
| PUT    | `/api/scripts/{id}`        | 编辑脚本（版本+1）  |
| PATCH  | `/api/scripts/{id}/status` | 切换状态        |

### 7.4 审计日志接口

| Method | Path                     | 说明          |
| ------ | ------------------------ | ----------- |
| GET    | `/api/audit-logs`        | 日志列表（分页+搜索） |
| GET    | `/api/audit-logs/export` | 导出 Excel    |

### 7.5 服务监控接口

| Method | Path                                | 说明                                 | 权限                |
| ------ | ----------------------------------- | ---------------------------------- | ----------------- |
| GET    | `/api/monitors`                     | 监控配置列表（分页 + 关键字 / 状态 / 类型 / 启停 筛选） | 全员                |
| GET    | `/api/monitors/{id}`                | 监控配置详情                             | 全员                |
| POST   | `/api/monitors`                     | 新增监控配置                             | admin 及以上         |
| PUT    | `/api/monitors/{id}`                | 编辑监控配置                             | admin 及以上         |
| DELETE | `/api/monitors/{id}`                | 删除监控配置                             | super_admin       |
| PATCH  | `/api/monitors/{id}/enabled`        | 启用/暂停调度                            | admin 及以上         |
| POST   | `/api/monitors/{id}/test`           | 手动触发一次检查并返回结果                      | admin 及以上         |
| GET    | `/api/monitors/status-overview`     | Apple 风格状态总览（按 module 分组聚合）        | 全员                |
| GET    | `/api/monitors/{id}/check-records`  | 检查历史（分页 + 时间范围 + 成功/失败）            | 全员                |
| GET    | `/api/monitors/{id}/uptime`         | 可用率：?range=24h\|7d\|30d\|90d       | 全员                |
| GET    | `/api/monitors/{id}/rt-trend`       | 响应时间趋势（聚合粒度自适应）                    | 全员                |
| GET    | `/api/alerts`                       | 告警列表（分页 + 系统名 / 级别 / 已确认 / 时间）     | 全员                |
| GET    | `/api/alerts/{id}`                  | 告警详情                               | 全员                |
| PATCH  | `/api/alerts/{id}/ack`              | 确认告警                               | admin 及以上         |
| GET    | `/api/alerts/export`                | 导出 Excel                           | admin 及以上         |

**示例 - 新增监控请求体：**

```json
{
  "systemName": "订单系统",
  "module": "交易",
  "checkType": "HTTP",
  "targetUrl": "https://order.example.com/health",
  "httpMethod": "GET",
  "expectedStatus": "200",
  "expectedKeyword": "ok",
  "checkIntervalSec": 60,
  "timeoutSec": 5,
  "rtThresholdMs": 2000,
  "failThreshold": 3,
  "recoveryThreshold": 2,
  "alertChannels": "dingtalk,email",
  "alertDingtalk": "https://oapi.dingtalk.com/robot/send?access_token=xxx",
  "alertEmail": "ops@example.com",
  "suppressMinutes": 10,
  "owner": "zhangsan",
  "alertReceiver": "张三",
  "enabled": 1
}
```

**示例 - 状态总览响应体：**

```json
{
  "globalStatus": "DEGRADED",
  "summary": { "total": 24, "operational": 22, "degraded": 1, "outage": 1, "maintenance": 0 },
  "groups": [
    {
      "module": "交易",
      "items": [
        {
          "id": 1,
          "systemName": "订单系统",
          "status": "OPERATIONAL",
          "lastRtMs": 120,
          "uptime90d": 0.9997,
          "dailyUptime90d": [1, 1, 1, 0.99, 1, ...]
        }
      ]
    }
  ]
}
```

---

## 8. 交互与 UI 规范

### 8.1 整体布局

- 顶部导航栏：Logo、系统名称、当前用户信息、退出按钮
- 左侧侧边栏：功能模块导航（支持收缩），新增"服务监控"一级菜单，含子项：监控总览 / 监控配置 / 告警记录
- 主内容区：面包屑 + 内容卡片

### 8.2 通用交互规范

| 场景   | 规范                         |
| ---- | -------------------------- |
| 操作成功 | 右上角绿色 Toast 提示，3 秒自动消失     |
| 操作失败 | 右上角红色 Toast 提示，需手动关闭       |
| 删除操作 | 必须二次确认弹窗，含操作对象名称           |
| 表单校验 | 实时校验 + 提交校验，错误信息显示在字段下方    |
| 加载状态 | 列表、按钮均显示 Loading 状态，防止重复提交 |
| 空数据  | 展示空状态插图 + 提示文案             |

### 8.3 代码编辑器规范

- 编辑器：Monaco Editor（VS Code 同款）
- 主题：Dark（VS Code Dark+）
- 行号：显示
- 字体：JetBrains Mono, Fira Code（monospace）
- Tab 大小：4 空格
- 自动缩进：开启
- 查看模式：`readOnly: true`，禁用编辑

### 8.4 服务监控 UI 布局（简要）

#### 8.4.1 监控总览（默认）— 仿 Apple System Status

```
┌────────────────────────────────────────────────────────────────────┐
│ 服务监控总览                  全局：● 全部正常       [刷新][监控配置] │
├────────────────────────────────────────────────────────────────────┤
│ 【交易】                                                            │
│  ▸ 订单系统           ● 正常       响应 120ms       可用率 99.97%   │
│    最近 90 天:  ▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮                     │
│  ▸ 支付网关           ● 性能下降   响应 1850ms      可用率 99.21%   │
│    最近 90 天:  ▮▮▮▮▮▯▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮▮                     │
│ 【数据】                                                            │
│  ▸ 用户中心 API       ● 中断       响应 超时        可用率 97.13%   │
└────────────────────────────────────────────────────────────────────┘
```

#### 8.4.2 监控配置列表

```
┌──────────────────────────────────────────────────────────────────────┐
│ [系统名] [负责人] [类型 ▾] [状态 ▾]  [搜索][重置]   [+ 新增监控]      │
├──┬──────┬─────┬────────┬────┬────┬────┬─────┬─────┬────┬────┬───────┤
│#│系统名│类型 │ 检查地址│频率 │RT阈值│失败阈值│状态│最近响应│负责人│启停│ 操作 │
├──┼──────┼─────┼────────┼────┼────┼────┼─────┼─────┼────┼────┼───────┤
│1│订单系统│HTTP│https://...│60s │2000│ 3  │● 正常│120ms│张三│ ●  │查/编/检/删│
└──────────────────────────────────────────────────────────────────────┘
```

#### 8.4.3 新增/编辑监控（抽屉，450px 宽）

```
┌─ 基础信息 ─────────────────────────────┐
│ 系统名 *  [_______________]            │
│ 业务分组  [_______________]            │
│ 检查类型 * (•) HTTP ( ) TCP ( ) PING   │
│ 检查地址 * [_______________]           │
│ HTTP方法  [GET ▾]                      │
│ Header    [JSON Editor]                │
│ Body      [JSON Editor]                │
├─ 频率与超时 ──────────────────────────┤
│ 检查频率 * [60] 秒                     │
│ 超时       [5] 秒                       │
├─ 阈值 ────────────────────────────────┤
│ 期望状态码  [200,2xx]                  │
│ 期望关键字  [____________]             │
│ 响应时间阈值 [2000] ms                  │
│ 连续失败阈值 [3]                        │
│ 连续恢复阈值 [2]                        │
├─ 告警通道 ────────────────────────────┤
│ ☑ 钉钉  Webhook [_______]              │
│ ☑ 邮箱  [a@b.com,...]                  │
│ ☐ 电话  [13800000000]                  │
│ 抑制时长 [10] 分钟                      │
├─ 责任人 ──────────────────────────────┤
│ 系统负责人 [_____]  告警接收人 [_____] │
├─ 其他 ────────────────────────────────┤
│ 调度启用 [●]    备注 [____________]    │
│                          [取消] [确定] │
└────────────────────────────────────────┘
```

#### 8.4.4 监控详情

```
┌──────────────────────────────────────────────────────────────────┐
│  订单系统  ● 正常   30天可用率 99.97%   [立即检查][编辑][暂停]    │
├──────────────────────────────────────────────────────────────────┤
│  响应时间趋势  [24h | 7d | 30d]                                  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │   ╱╲     ╱╲      ╱╲                                       │  │
│  │ ╱    ╲ ╱   ╲ ╱       ╲___                                 │  │
│  └────────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────────┤
│  [ 检查历史 ]  [ 告警历史 ]                                      │
│  时间                  状态   响应     HTTP   错误信息            │
│  2026-05-25 10:00:00   成功   120 ms   200    -                   │
│  2026-05-25 09:59:00   失败   -        503    Service Unavailable │
└──────────────────────────────────────────────────────────────────┘
```

#### 8.4.5 告警记录

```
┌────────────────────────────────────────────────────────────────────┐
│ [系统名][级别][时间范围][已确认 ▾]  [搜索][重置]   [导出 Excel]    │
├──┬──────┬────┬──────┬──────────┬──────────┬─────┬────┬───────────┤
│#│系统名│级别│ 原因 │ 起始时间  │ 恢复时间  │ 通道 │确认│   操作    │
├──┼──────┼────┼──────┼──────────┼──────────┼─────┼────┼───────────┤
│1│订单系统│CRIT│连续3次超时│ 10:00 │ 10:05    │钉/邮│ 是 │查看/已确认│
└────────────────────────────────────────────────────────────────────┘
```

---

## 9. 权限矩阵

| 功能          | 超级管理员 | 普通管理员 | 只读用户 |
| ----------- |:-----:|:-----:|:----:|
| 用户列表查看      | ✅     | ❌     | ❌    |
| 用户新增/修改/删除  | ✅     | ❌     | ❌    |
| 脚本列表查看      | ✅     | ✅     | ✅    |
| 脚本内容查看      | ✅     | ✅     | ✅    |
| 脚本编辑        | ✅     | ✅     | ❌    |
| 审计日志查看      | ✅     | ✅     | ✅    |
| 审计日志导出      | ✅     | ✅     | ❌    |
| 监控总览查看      | ✅     | ✅     | ✅    |
| 监控配置新增/修改   | ✅     | ✅     | ❌    |
| 监控配置删除      | ✅     | ❌     | ❌    |
| 监控启停 / 立即检查 | ✅     | ✅     | ❌    |
| 告警查看        | ✅     | ✅     | ✅    |
| 告警确认        | ✅     | ✅     | ❌    |
| 告警导出        | ✅     | ✅     | ❌    |

---

## 10. 验收标准

### 10.1 用户管理

- [ ] 可正常新增用户，密码加密存储（数据库中不可见明文）
- [ ] 用户名唯一性校验有效
- [ ] 修改用户时不填密码，原密码不变
- [ ] 删除用户后用户无法登录
- [ ] 搜索功能按用户名模糊、注册时间范围正确过滤
- [ ] 分页功能正常

### 10.2 脚本管理

- [ ] 脚本列表正确展示所有字段
- [ ] 查看脚本页 Python 语法高亮正常
- [ ] 编辑脚本保存后版本号 +1
- [ ] 编辑脚本操作写入审计日志
- [ ] 离开编辑页有未保存内容时有提示

### 10.3 审计日志

- [ ] 用户增删改操作均产生审计记录
- [ ] 脚本编辑操作均产生审计记录
- [ ] 监控配置增删改与告警/确认操作均产生审计记录
- [ ] 密码字段在审计记录中脱敏（显示为 `***`）
- [ ] 时间、事件类型、操作人搜索过滤正确
- [ ] 导出 Excel 文件数据与列表一致

### 10.4 服务监控

- [ ] HTTP/TCP/PING 三种检查类型按配置频率正常执行
- [ ] 连续失败达阈值后状态切换为 `MAJOR_OUTAGE`
- [ ] 响应时间超阈值时状态显示为 `DEGRADED`
- [ ] 告警按启用通道发送（钉钉、邮箱、电话），同一故障周期内按 `suppress_minutes` 抑制
- [ ] 服务恢复后发送恢复通知并自动关闭未恢复告警
- [ ] 监控总览页加载时间 ≤ 2 秒，分组与状态展示正确
- [ ] 详情页响应时间趋势、检查历史、告警历史展示正确
- [ ] `MAINTENANCE` 状态下不发送告警
- [ ] 暂停的监控不再调度
- [ ] 手动 `立即检查` 不影响正常调度节奏

---

## 11. 里程碑计划

| 阶段     | 内容                           | 预计时长          |
| ------ | ---------------------------- | ------------- |
| 阶段一    | 架构搭建、数据库设计、登录认证              | 3 天           |
| 阶段二    | 用户管理模块（前后端）                  | 3 天           |
| 阶段三    | 脚本管理模块（前后端）                  | 5 天           |
| 阶段四    | 审计日志模块（前后端）                  | 2 天           |
| 阶段五    | 服务监控 - 配置 CRUD + 调度引擎 + 检查类型 | 4 天           |
| 阶段六    | 服务监控 - 告警通道 + 状态机 + 抑制       | 3 天           |
| 阶段七    | 服务监控 - 总览页 + 详情图表 + 告警记录     | 3 天           |
| 阶段八    | 联调测试、压测、Bug 修复、上线            | 3 天           |
| **合计** |                              | **约 26 个工作日** |

---

## 12. 风险与约束

| 风险             | 影响        | 应对措施                                  |
| -------------- | --------- | ------------------------------------- |
| 脚本文件读写权限       | 编辑脚本功能失效  | 后端服务以具备文件读写权限的用户运行，或提供路径映射机制          |
| 大文件脚本加载慢       | 编辑器卡顿     | 对超过 1MB 的脚本文件进行分段加载或给出警告              |
| 并发编辑冲突         | 脚本内容覆盖    | 引入乐观锁机制（基于版本号），保存时校验版本                |
| 审计/检查记录增长过快    | 存储压力      | 审计 180 天、检查记录 30 天 自动清理，必要时按天分区       |
| 监控调度抖动 / 任务堆积  | 巡检不准时     | 限制单实例监控数（≤ 500），使用专用线程池，超时强中断；后续替换为 XXL-Job |
| 告警风暴           | 钉钉群/邮箱被打爆 | 抑制策略 + 升级合并；同一故障期内只通知 1 次             |
| 三方语音平台接口变化     | 电话告警失败    | 通过 `IAlertChannel` 抽象，可热切换；记录发送结果便于排查 |
| 内网监控对象网络可达性    | 检查失败误报    | 部署多 Probe（后续扩展），单实例仅监控可达对象            |

---
