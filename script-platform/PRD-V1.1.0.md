# 脚本管理平台 — V1.1.0 迭代需求文档（PRD）

> **版本**：v1.1.0（增量）
> **基线版本**：v1.0.0
> **创建日期**：2026-05-27
> **文档状态**：评审稿
> **产品负责人**：待定

> 本文档**仅描述 V1.1.0 相对 V1.0.0 的变更**，未列出的内容沿用 V1.0.0 PRD。阅读本文需配合 V1.0.0 PRD 一同查阅。

---

## 目录

1. [变更摘要](#1-变更摘要)
2. [菜单目录结构调整](#2-菜单目录结构调整)
3. [Python 脚本管理调整](#3-python-脚本管理调整)
4. [SQL 脚本管理（新增）](#4-sql-脚本管理新增)
5. [脚本在线测试功能（新增）](#5-脚本在线测试功能新增)
6. [数据库变更](#6-数据库变更)
7. [接口变更](#7-接口变更)
8. [审计事件类型扩展](#8-审计事件类型扩展)
9. [权限矩阵增量](#9-权限矩阵增量)
10. [验收标准增量](#10-验收标准增量)
11. [里程碑计划](#11-里程碑计划)
12. [风险与约束（新增项）](#12-风险与约束新增项)
13. [附录 A：开发自检清单](#附录-a开发自检清单)

---

## 1. 变更摘要

| 变更点 | 描述 |
| --- | --- |
| C1：菜单结构 | "脚本管理"由单层菜单调整为可展开父节点，下挂 **Python 脚本** / **SQL 脚本** 两个子菜单 |
| C2：Python 脚本字段扩展 | `script_info` 表新增 4 个告警相关字段：`alert_level` / `alert_source_log` / `alert_template` / `alert_group` |
| C3：SQL 脚本管理（新模块） | 新增 SQL 脚本的元信息维护、在线查看、在线编辑、阈值配置等能力，新建 `sql_info` 表 |
| C4：在线测试 | Python 脚本 / SQL 脚本均新增 **【测试】** 功能：Python 走子进程执行 `python <path>`，SQL 通过可配置的 HTTP 接口提交执行 |

---

## 2. 菜单目录结构调整

> 对应需求：C1

### 2.1 调整后的页面结构

```
脚本管理平台
├── 登录页
└── 主控台（侧边栏导航）
    ├── 首页 Dashboard
    ├── 用户管理
    ├── 脚本管理                       ← 父节点（可展开 / 收起）
    │   ├── Python 脚本                ← v1.1.0 调整：原"脚本管理"内容下沉到此
    │   │   ├── 脚本列表
    │   │   ├── 查看脚本（代码预览页）
    │   │   └── 编辑脚本（在线编辑器页 + 测试）
    │   └── SQL 脚本                   ← v1.1.0 新增子菜单
    │       ├── 脚本列表
    │       ├── 查看脚本（代码预览页）
    │       └── 编辑脚本（在线编辑器页 + 测试）
    ├── 审计日志
    └── 服务监控
```

### 2.2 路由调整

| 改动类型 | 原路由（V1.0.0） | 新路由（V1.1.0） |
| --- | --- | --- |
| 重命名（语义化） | `/scripts` | `/scripts/python` |
| 新增 | — | `/scripts/sql` |
| 兼容 | 旧路径 `/scripts` 默认重定向到 `/scripts/python`，下个 minor 版本下线 | — |

### 2.3 侧边栏交互

- "脚本管理"父节点点击仅展开/收起，不跳转
- 默认展开"Python 脚本"
- 子节点选中态独立高亮，父节点联动高亮

---

## 3. Python 脚本管理调整

> 对应需求：C2、C4（测试入口）

### 3.1 数据模型字段新增（仅列增量）

| 字段名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `alert_level` | varchar(128) | ❌ | 告警等级：`L1` / `L2` / `L3` |
| `alert_source_log` | varchar(128) | ❌ | 告警日志来源（如：日志文件路径、SLS LogStore 名等） |
| `alert_template` | varchar(128) | ❌ | 通知模版 ID 或名称 |
| `alert_group` | varchar(128) | ❌ | 通知群（钉钉群名 / 飞书群 ID 等） |

> 其余字段（`script_name` / `script_path` / `module` / `description` / `owner` / `alert_dingtalk` / `alert_phone` / `schedule_cron` / `schedule_desc` / `script_content` / `version` / `status` / 时间戳）保持 V1.0.0 原样。

### 3.2 列表页增量

- **搜索条件区**：新增"告警等级"下拉筛选（多选：L1 / L2 / L3）
- **列定义增量**：在"预警钉钉"前新增一列 **告警等级**，使用彩色 Tag（L1 红 / L2 橙 / L3 蓝）
- **操作列增量**：在 `【查看】` `【编辑】` 之间新增 `【测试】` 按钮（admin 及以上可见，详见 §5）

### 3.3 查看脚本页增量

- 右上角元信息区追加：**告警等级 Tag**
- 元信息面板追加：告警日志来源、通知模版、通知群
- 工具栏追加：`【测试】` 按钮（admin 及以上）

### 3.4 编辑脚本页增量

- 表单新增 4 个字段输入控件：
  - `alert_level`：`Select`，选项 L1 / L2 / L3
  - `alert_source_log`：`Input`
  - `alert_template`：`Select` 支持自由输入（历史值下拉）
  - `alert_group`：`Select` 支持自由输入（历史值下拉）
- 底部按钮新增 `【保存并测试】`：先调用保存接口，成功后自动触发测试（详见 §5）
- 其他保存逻辑（版本号 +1、本地文件先备份再覆盖、写审计日志）维持 V1.0.0

---

## 4. SQL 脚本管理（新增）

> 对应需求：C3、C4

### 4.1 功能概述

集中管理日常巡检、灰度核对、慢查询验证类的 SQL 脚本，提供：

- 元信息（名称、模块、负责人、调度频率、告警配置等）维护
- SQL 内容在线查看 / 编辑（SQL 语法高亮）
- 阈值（`threshold`）配置：执行结果首列单行整型值或行数超过阈值时触发告警（具体语义由对接的执行平台定义，本平台仅做配置存储与传递）
- 在线测试：调用配置的 HTTP 接口提交 SQL，回显结果（详见 §5.2）
- 与 Python 脚本子模块在 UI 骨架上保持一致；差异点仅在数据模型、编辑器语法、测试实现方式

### 4.2 数据模型 `sql_info`

> **表名说明**：原始需求草稿中表名写作 `script_info`，与 V1.0.0 Python 脚本表冲突。本 PRD 正式定义为 **`sql_info`**，避免歧义。

| 字段名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | bigint | 系统生成 | 主键，自增 |
| `sql_name` | varchar(128) | ✅ | SQL 脚本名称 |
| `module` | varchar(64) | ✅ | 归属模块 |
| `description` | text | ✅ | 功能说明 |
| `owner` | varchar(64) | ✅ | 负责人（关联用户名） |
| `alert_dingtalk` | varchar(256) | ❌ | 预警钉钉 Webhook |
| `alert_phone` | varchar(64) | ❌ | 预警电话 |
| `alert_level` | varchar(128) | ❌ | 告警等级 `L1` / `L2` / `L3` |
| `alert_source_log` | varchar(128) | ❌ | 告警日志来源 |
| `alert_template` | varchar(128) | ❌ | 通知模版 |
| `alert_group` | varchar(128) | ❌ | 通知群 |
| `schedule_cron` | varchar(64) | ❌ | Cron 表达式 |
| `schedule_desc` | varchar(128) | ❌ | 调度频率人读描述 |
| `threshold` | int | ❌ | 阈值，默认 0；超过即告警 |
| `sql_content` | longtext | ✅ | SQL 脚本内容 |
| `version` | int | 系统生成 | 版本号，每次编辑 +1，初始为 1 |
| `status` | tinyint | ✅ | 1=启用，0=禁用 |
| `created_at` | datetime | 系统生成 | 创建时间 |
| `updated_at` | datetime | 系统生成 | 最后修改时间 |

### 4.3 列表页

**搜索条件**：SQL 名称（模糊）、归属模块（动态加载）、负责人（模糊）、告警等级（多选）、状态。

**操作按钮**：`【+ 新增 SQL】`、`【重置】`、`【搜索】`。

**列定义**：

| 列名 | 字段 | 展示说明 |
| --- | --- | --- |
| 序号 | — | 行号 |
| SQL 名称 | `sql_name` | 文字 |
| 功能说明 | `description` | 超 50 字符省略 + Tooltip |
| 归属模块 | `module` | Tag 标签 |
| 负责人 | `owner` | 文字 |
| 告警等级 | `alert_level` | 彩色 Tag（L1 红 / L2 橙 / L3 蓝） |
| 预警钉钉 | `alert_dingtalk` | 无则 `—` |
| 预警电话 | `alert_phone` | 无则 `—` |
| 调度频率 | `schedule_desc` | 无则 `—` |
| 阈值 | `threshold` | 数字，无则 `—` |
| 版本 | `version` | `v{n}` |
| 状态 | `status` | Tag |
| 操作 | — | `【查看】` / `【编辑】` / `【测试】` / `【删除】` |

**分页**：默认每页 20 条，可切换 10 / 20 / 50 / 100。

### 4.4 查看 SQL 页

- Monaco Editor，`language: 'sql'`，只读
- 顶部展示：名称、版本号、最后更新时间、负责人、告警等级 Tag、阈值
- 元信息面板：模块、调度频率、预警钉钉、预警电话、告警日志来源、通知模版、通知群
- 工具栏：`【复制代码】` `【返回列表】` `【测试】`（admin 及以上）

### 4.5 编辑 SQL 页

- Monaco Editor，`language: 'sql'`，自动缩进，关键字大写提示
- 元信息区（只读展示）
- 可编辑字段：`sql_content`、`description`、`module`、`owner`、`alert_dingtalk`、`alert_phone`、`alert_level`、`alert_source_log`、`alert_template`、`alert_group`、`schedule_cron`、`schedule_desc`、`threshold`、`status`
- **保存动作**：版本号 +1，记录操作人和时间，写入审计日志（事件 `SQL_SCRIPT_UPDATE`）。**SQL 脚本不落地服务器文件，不执行 V1.0.0 中的"备份原文件再覆盖"逻辑**，仅在数据库维护版本
- 底部按钮：`【保存】` `【保存并测试】` `【取消】`
- 离开未保存提示与 Python 子模块一致

### 4.6 删除 SQL

- 二次确认弹窗："确认删除 SQL **{sql_name}**？此操作不可撤销。"
- 删除写审计日志（`SQL_SCRIPT_DELETE`）

---

## 5. 脚本在线测试功能（新增）

> 对应需求：C4

### 5.1 通用约定

| 项 | 说明 |
| --- | --- |
| 入口 | 列表页操作列 `【测试】`、查看页右上角 `【测试】`、编辑页底部 `【保存并测试】` |
| 权限 | 仅 `super_admin` / `admin` 可触发；只读用户不可见入口，强行调用接口返回 403 |
| 反馈 | 触发后按钮置 Loading；执行结果以 **右侧抽屉**（宽 600px）滑出展示，不离开当前页 |
| 审计 | 每次测试均落审计日志，事件类型见 §8 |
| 输出截断 | 任一文本输出字段单次最多保留 64KB，超出截断并附 `[truncated]` 标记 |

### 5.2 Python 脚本测试

| 项 | 说明 |
| --- | --- |
| 接口 | `POST /api/python-scripts/{id}/test` |
| 实现方式 | 后端使用 `ProcessBuilder` 启动子进程执行 `python <script_path>`，捕获 `stdout` / `stderr` / `exitCode` 与执行耗时 |
| 工作目录 | `script_path` 所在目录 |
| 解释器 | 默认 `python`，可通过配置项 `script.test.python.interpreter` 指定（如 `python3` 或绝对路径） |
| 单次超时 | 默认 60 秒，配置项 `script.test.python.timeout-sec`；超时后强制 `process.destroyForcibly()` |
| 并发上限 | 专用 `ThreadPoolTaskExecutor`，默认核心 2 / 最大 10 / 队列 50 |
| 安全约束 | 仅执行 `script_path` 指向的脚本，**不接受 UI 注入额外命令行参数**；运行用户为后端服务进程用户 |

**响应结构**：

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

### 5.3 SQL 脚本测试

| 项 | 说明 |
| --- | --- |
| 接口 | `POST /api/sql-scripts/{id}/test` |
| 实现方式 | 后端调用配置化的 HTTP 执行接口，将 SQL 文本作为 Body 提交，解析响应后返回前端 |
| 配置项（双源） | `application.yml` + `sys_config` 表，`sys_config` 优先级更高，**修改后无需重启即生效** |
| 关键配置键 | `script.test.sql.endpoint`、`script.test.sql.method`（默认 `POST`）、`script.test.sql.headers`（JSON Map，含鉴权）、`script.test.sql.timeout-sec`（默认 30） |
| 请求模板（默认） | `{ "sqlId": <id>, "sqlName": "...", "sqlContent": "...", "operator": "<currentUser>" }` |
| 成功判定 | HTTP 2xx **且** 响应中 `code == 0` |
| 输出 | 透传外部接口响应；同时本平台返回结构化字段 `{ success, durationMs, summary, raw, errorMessage }` |
| 安全约束 | 平台仅传递 SQL 文本，由外部执行平台做语句白名单（DDL/DML 拦截） |

**响应结构（成功）**：

```json
{
  "success": true,
  "durationMs": 412,
  "summary": { "rowCount": 17, "columns": ["uid", "name"], "preview": [[1, "alice"], [2, "bob"]] },
  "raw": { "code": 0, "msg": "ok", "data": [ ... ] }
}
```

**响应结构（失败）**：

```json
{
  "success": false,
  "durationMs": 88,
  "errorMessage": "ER_PARSE_ERROR: You have an error in your SQL syntax",
  "raw": { "code": 1064, "msg": "..." }
}
```

### 5.4 测试结果抽屉 UI

```
┌─ 测试结果 ─────────────────────────────── × ─┐
│ 脚本：sync_user.py    触发：14:32:08         │
│ 总耗时：327 ms        状态：● 失败            │
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

- **输出**：Python = `stdout`；SQL = `summary` 表格视图
- **错误**：Python = `stderr`（红色高亮）；SQL = `errorMessage`
- **原始响应**：Python = exitCode + 完整 JSON；SQL = 透传 raw（折叠 JSON）

### 5.5 性能要求

| 指标 | 要求 |
| --- | --- |
| Python 脚本测试响应 | ≤ 60 秒（受脚本执行时长决定，超时即中断） |
| SQL 脚本测试响应 | ≤ 30 秒（受外部接口决定，超时即中断） |

---

## 6. 数据库变更

### 6.1 `script_info` 表升级（V1.0.0 → V1.1.0）

```sql
ALTER TABLE `script_info`
  ADD COLUMN `alert_level`      VARCHAR(128) DEFAULT NULL COMMENT '告警等级L1，L2，L3' AFTER `alert_phone`,
  ADD COLUMN `alert_source_log` VARCHAR(128) DEFAULT NULL COMMENT '告警日志来源'        AFTER `alert_level`,
  ADD COLUMN `alert_template`   VARCHAR(128) DEFAULT NULL COMMENT '通知模版'          AFTER `alert_source_log`,
  ADD COLUMN `alert_group`      VARCHAR(128) DEFAULT NULL COMMENT '通知群'           AFTER `alert_template`,
  ADD KEY `idx_alert_level` (`alert_level`);
```

### 6.2 新建 `sql_info` 表

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

### 6.3 新建 `sys_config` 表（SQL 测试热配置依赖）

> V1.0.0 §4.4.14 提到"由数据库 `sys_config` 双重支持"但未给 DDL；V1.1.0 SQL 测试也复用此表，本版本正式落地。

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

**默认配置项初始化**：

| `config_key` | 示例值 | 说明 |
| --- | --- | --- |
| `script.test.sql.endpoint` | `https://sql-runner.example.com/run` | SQL 执行平台 URL |
| `script.test.sql.method` | `POST` | HTTP 方法 |
| `script.test.sql.headers` | `{"Authorization":"Bearer xxx"}` | 自定义请求头（JSON） |
| `script.test.sql.timeout-sec` | `30` | 超时（秒） |
| `script.test.python.interpreter` | `python3` | Python 解释器路径 |
| `script.test.python.timeout-sec` | `60` | Python 测试超时（秒） |

---

## 7. 接口变更

### 7.1 Python 脚本接口路径调整

为兼容 V1.0.0 已发布的 `/api/scripts/*`：

- 新增首选路径 `/api/python-scripts/*`
- 旧路径 `/api/scripts/*` 保留为别名（Deprecated），下个 minor 版本下线

| Method | Path | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/python-scripts` | Python 脚本列表（分页+搜索，搜索条件支持 `alertLevel`） | 全员 |
| GET | `/api/python-scripts/{id}` | 获取脚本详情 | 全员 |
| POST | `/api/python-scripts` | 新增脚本（请求体新增 4 字段） | admin 及以上 |
| PUT | `/api/python-scripts/{id}` | 编辑脚本（请求体新增 4 字段；版本+1，备份本地文件） | admin 及以上 |
| DELETE | `/api/python-scripts/{id}` | 删除脚本 | super_admin |
| PATCH | `/api/python-scripts/{id}/status` | 切换状态 | admin 及以上 |
| POST | `/api/python-scripts/{id}/test` | **(新增)** 测试 Python 脚本 | admin 及以上 |

### 7.2 SQL 脚本接口（全部新增）

| Method | Path | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/sql-scripts` | SQL 脚本列表（分页+搜索） | 全员 |
| GET | `/api/sql-scripts/{id}` | 获取 SQL 脚本详情 | 全员 |
| POST | `/api/sql-scripts` | 新增 SQL 脚本 | admin 及以上 |
| PUT | `/api/sql-scripts/{id}` | 编辑 SQL 脚本（版本+1） | admin 及以上 |
| DELETE | `/api/sql-scripts/{id}` | 删除 SQL 脚本 | super_admin |
| PATCH | `/api/sql-scripts/{id}/status` | 切换状态 | admin 及以上 |
| POST | `/api/sql-scripts/{id}/test` | 测试 SQL 脚本（调用外部 HTTP 接口） | admin 及以上 |

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

### 7.3 系统配置接口（全部新增）

| Method | Path | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/sys-configs` | 配置列表（按 `keyPrefix` 过滤） | super_admin |
| GET | `/api/sys-configs/{key}` | 获取单个配置 | super_admin |
| PUT | `/api/sys-configs/{key}` | 更新配置（热生效） | super_admin |

---

## 8. 审计事件类型扩展

> V1.0.0 已落库的 `SCRIPT_*` 事件继续视为 Python 脚本事件，无需迁移。

新增以下事件类型枚举：

| 事件值 | 描述 |
| --- | --- |
| `PYTHON_SCRIPT_TEST` | Python 脚本在线测试 |
| `SQL_SCRIPT_VIEW` | 查看 SQL 脚本（可选记录） |
| `SQL_SCRIPT_CREATE` | 新增 SQL 脚本 |
| `SQL_SCRIPT_UPDATE` | 编辑 SQL 脚本 |
| `SQL_SCRIPT_DELETE` | 删除 SQL 脚本 |
| `SQL_SCRIPT_TEST` | SQL 脚本在线测试 |

**审计列表页 UI 联动**：事件类型 Tag 颜色按业务域区分：用户(蓝) / Python(青) / SQL(紫) / 监控(橙)。

---

## 9. 权限矩阵增量

仅列 V1.1.0 新增条目；V1.0.0 既有条目保持不变。

| 功能 | 超级管理员 | 普通管理员 | 只读用户 |
| --- | :---: | :---: | :---: |
| Python 脚本测试 | ✅ | ✅ | ❌ |
| SQL 脚本列表/内容查看 | ✅ | ✅ | ✅ |
| SQL 脚本新增/编辑 | ✅ | ✅ | ❌ |
| SQL 脚本删除 | ✅ | ❌ | ❌ |
| SQL 脚本测试 | ✅ | ✅ | ❌ |
| 系统配置（`sys_config`）查看/编辑 | ✅ | ❌ | ❌ |

---

## 10. 验收标准增量

### 10.1 Python 脚本管理（新增条目）

- [ ] 列表/查看/编辑页正确展示与保存 4 个新字段（`alert_level` / `alert_source_log` / `alert_template` / `alert_group`）
- [ ] 列表搜索条件支持按"告警等级"过滤
- [ ] `【测试】` 按钮可触发执行，结果抽屉正确展示 stdout / stderr / exitCode / durationMs
- [ ] 测试超时（默认 60 秒）后子进程被强制中断，前端展示超时错误
- [ ] 测试操作写入审计日志（事件 `PYTHON_SCRIPT_TEST`），输出做截断处理
- [ ] 只读用户调用 `/test` 接口返回 403

### 10.2 SQL 脚本管理（全新模块）

- [ ] 列表/查看/编辑/删除四件套功能正常
- [ ] SQL 语法高亮、关键字大写提示生效
- [ ] 新增/编辑表单可保存所有字段（含 `threshold` 与 4 个告警字段）
- [ ] 编辑后版本号 +1，写入审计日志（`SQL_SCRIPT_UPDATE`）
- [ ] `【测试】` 按钮调用配置的 HTTP 接口，结果抽屉正确展示成功/失败两种状态
- [ ] 测试操作写入审计日志（`SQL_SCRIPT_TEST`）
- [ ] 修改 `sys_config` 中的 `script.test.sql.endpoint` / `script.test.sql.headers` 后，**无需重启即生效**
- [ ] 只读用户调用 `/test` 接口返回 403

### 10.3 菜单与路由

- [ ] 侧边栏"脚本管理"为可展开父节点，下挂"Python 脚本"、"SQL 脚本"
- [ ] 默认展开"Python 脚本"，子节点选中态独立高亮
- [ ] 旧路由 `/scripts` 自动重定向到 `/scripts/python`

---

## 11. 里程碑计划

| 阶段 | 内容 | 预计时长 |
| --- | --- | --- |
| 阶段一 | DB 迁移：`script_info` 增 4 字段、新增 `sql_info` / `sys_config` | 0.5 天 |
| 阶段二 | 后端：Python 脚本 4 字段贯通 + `PythonProcessTester` + `/test` 接口 + 审计扩展 | 1.5 天 |
| 阶段三 | 后端：SQL 脚本 CRUD + `SqlHttpTester` + `/test` 接口 + 审计扩展 | 2 天 |
| 阶段四 | 后端：`sys_config` CRUD + 配置热加载（事件机制） | 0.5 天 |
| 阶段五 | 前端：菜单父子结构 + Python 脚本表单/列表新增 4 字段 | 1 天 |
| 阶段六 | 前端：SQL 脚本子模块（列表 / 查看 / 编辑） | 2 天 |
| 阶段七 | 前端：通用"测试结果抽屉"组件 + Python/SQL 接入 | 1.5 天 |
| 阶段八 | 联调、测试、Bug 修复、上线 | 1 天 |
| **合计** | — | **约 10 个工作日** |

---

## 12. 风险与约束（新增项）

| 风险 | 影响 | 应对措施 |
| --- | --- | --- |
| Python 测试长时间执行 | 占用线程池、影响系统 | 单次默认 60 秒超时；专用 `ThreadPoolTaskExecutor`，并发上限默认 10；超时强制 `process.destroyForcibly()` |
| Python 测试可能产生副作用 | 误改生产数据 | 文档与 UI 提示用户："测试 = 真实执行"；建议脚本顶部支持 `--dry-run` 自检 |
| SQL 测试外部接口不可达 | 测试不可用 | 接口超时 30 秒，捕获网络异常返回友好错误；UI 抽屉提示运维检查 `sys_config` 中的 endpoint |
| SQL 测试结果体积过大 | 内存/前端卡顿 | 后端对响应体做 256KB 截断，前端 raw 区折叠展示 |
| SQL 误执行写操作 | 数据被改 | 平台仅传递 SQL 文本，由外部执行平台做语句白名单（DDL/DML 拦截），平台层补充审计 |
| 新增 4 字段历史数据为 NULL | 列表筛选不准 | 升级 SQL 默认 NULL；前端筛选明确支持"未设置"选项 |

---

## 附录 A：开发自检清单

| 类别 | 改动项 |
| --- | --- |
| DB | `script_info` 增 4 列 + `idx_alert_level` 索引（DDL 见 §6.1） |
| DB | 新建 `sql_info` 表（DDL 见 §6.2） |
| DB | 新建 `sys_config` 表 + 默认数据初始化（DDL 见 §6.3） |
| 后端 | `ScriptInfo` 实体 / DTO（`ScriptSaveRequest` / `ScriptQuery`）/ Mapper XML 同步 4 字段 |
| 后端 | 新增 `SqlInfo` 实体 / Mapper / Service / Controller / DTO 全套 |
| 后端 | 新增 `IScriptTester` 接口 + `PythonProcessTester` + `SqlHttpTester` 实现 |
| 后端 | 新增 `POST /api/python-scripts/{id}/test` 与 `POST /api/sql-scripts/{id}/test` |
| 后端 | 新增 `SysConfig` 实体 + Service + Controller + 热更新事件（`@EventListener`） |
| 后端 | `AuditEventType` 枚举新增 `PYTHON_SCRIPT_TEST` / `SQL_SCRIPT_*`；`AuditAspect` 拦截新接口 |
| 后端 | 专用 `TaskExecutor` Bean：`pythonTesterExecutor`（核心 2 / 最大 10 / 队列 50） |
| 前端 | 路由 `/scripts/python/*` / `/scripts/sql/*`，旧 `/scripts` 重定向到 `/scripts/python` |
| 前端 | 侧边栏菜单改为可展开父节点 |
| 前端 | `PythonScriptForm.vue` 增加 4 字段输入；列表新增"告警等级"列与筛选 |
| 前端 | 新增 `SqlScriptList.vue` / `SqlScriptView.vue` / `SqlScriptEdit.vue` / `SqlScriptForm.vue` |
| 前端 | 通用组件 `TestResultDrawer.vue` |
| 前端 | API 模块 `api/pythonScript.js` / `api/sqlScript.js` / `api/sysConfig.js` |
| 配置 | `application.yml` 新增 `script.test.python.*` / `script.test.sql.*` 默认值 |
