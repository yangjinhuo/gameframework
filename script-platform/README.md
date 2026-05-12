# Python 在线脚本管理平台

基于 `Spring Boot + Java 8 + MyBatis + MySQL 5.7 + Vue 3 + Element Plus + Monaco Editor` 的 Python 脚本统一管理平台，前后端分离。

## 目录结构

```
script-platform/
├── backend/                # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/scriptplatform/
│       │   ├── ScriptPlatformApplication.java
│       │   ├── aspect/       # 审计 & 权限切面
│       │   ├── common/       # 公共类：R / PageQuery / PageResult
│       │   ├── config/       # Web / 密码 / 数据初始化
│       │   ├── controller/   # REST Controller
│       │   ├── dto/          # 请求 / 查询 DTO
│       │   ├── entity/       # 实体
│       │   ├── exception/    # 业务异常 & 全局异常处理
│       │   ├── mapper/       # MyBatis Mapper 接口
│       │   ├── security/     # JWT、拦截器、上下文、@RequireRole
│       │   ├── service/      # 业务服务及实现
│       │   └── util/         # 工具类
│       └── resources/
│           ├── application.yml
│           ├── db/           # schema.sql / data.sql
│           └── mapper/       # MyBatis XML
└── frontend/               # Vue 3 前端
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/              # axios 封装的接口
        ├── components/       # 公共组件（Monaco 代码编辑器）
        ├── layout/           # 主布局（侧边栏 + 顶部）
        ├── router/
        ├── store/            # Pinia store
        ├── utils/            # 请求工具
        └── views/            # 登录 / 首页 / 用户 / 脚本 / 审计
```

## 功能概览

按 PRD 需求实现：

- 登录 & JWT 鉴权，刷新、登出
- 三种角色：超级管理员 / 普通管理员 / 只读用户 —— 后端 `@RequireRole` 切面 + 前端路由守卫
- 用户管理：分页、条件筛选、新增、编辑（支持修改密码/不改密码）、启停、删除、超级管理员保护
- 脚本管理：分页、条件筛选、新增、在线查看（Monaco 只读）、在线编辑（Monaco 可写 + 离开前提示）、基于 `version` 的乐观锁、软删除开关
- 审计日志：所有写操作自动通过 `@AuditEvent` + AOP 切面落库；密码字段脱敏；支持分页查询与 Excel 导出
- 首页 Dashboard 概览统计

## 快速开始

### 1. 初始化数据库（MySQL 5.7）

```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/data.sql   # 可选，插入示例脚本
```

默认账号（首次启动由应用自动写入 `sys_user`）：

| 用户名 | 密码 | 角色 |
| ------ | ---- | ---- |
| admin  | Admin@123  | super_admin |
| devops | Devops@123 | admin |
| viewer | Viewer@123 | readonly |

修改 `backend/src/main/resources/application.yml` 中的数据库连接信息（默认 `127.0.0.1:3306 / root / root`）。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
# or
mvn clean package -DskipTests
java -jar target/script-platform-backend.jar
```

监听端口：`8080`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认 `http://localhost:5173`，开发服务器通过 Vite 代理 `/api` → `http://127.0.0.1:8080`。

生产构建：

```bash
npm run build
# dist 下的静态资源可部署到任意 Web 服务器
```

## 接口一览

| 分类 | Method | Path | 权限 |
| ---- | ------ | ---- | ---- |
| 认证 | POST   | `/api/auth/login`          | 公开 |
| 认证 | POST   | `/api/auth/logout`         | 登录 |
| 认证 | POST   | `/api/auth/refresh`        | 登录 |
| 用户 | GET    | `/api/users`               | super_admin |
| 用户 | POST   | `/api/users`               | super_admin |
| 用户 | PUT    | `/api/users/{id}`          | super_admin |
| 用户 | PATCH  | `/api/users/{id}/status`   | super_admin |
| 用户 | DELETE | `/api/users/{id}`          | super_admin |
| 用户 | GET    | `/api/users/me`            | 登录 |
| 脚本 | GET    | `/api/scripts`             | 全员 |
| 脚本 | GET    | `/api/scripts/{id}`        | 全员 |
| 脚本 | GET    | `/api/scripts/modules`     | 全员 |
| 脚本 | POST   | `/api/scripts`             | admin 及以上 |
| 脚本 | PUT    | `/api/scripts/{id}`        | admin 及以上 |
| 脚本 | PATCH  | `/api/scripts/{id}/status` | admin 及以上 |
| 脚本 | DELETE | `/api/scripts/{id}`        | super_admin |
| 审计 | GET    | `/api/audit-logs`          | 全员 |
| 审计 | GET    | `/api/audit-logs/export`   | admin 及以上 |
| 首页 | GET    | `/api/dashboard/stats`     | 全员 |

## 安全设计

- 密码使用 Spring Security 提供的 BCrypt（cost = 10）
- JWT Token 有效期 8 小时，密钥可通过 `jwt.secret` 环境变量覆盖
- 审计日志中 `request_params` 内的 `password` 字段会被替换为 `***`
- 前端路由守卫和后端 `@RequireRole` 双重鉴权
- CORS 已开启，生产环境建议收紧 `allowedOriginPatterns`

## 乐观锁

`PUT /api/scripts/{id}` 需传入当前 `version`，服务端使用 `update ... where id = ? and version = ?` 并自增版本号；如版本不匹配，返回提示 `脚本已被他人修改，请刷新后重试`。
