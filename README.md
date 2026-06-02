# Mail System Backend

华南理工大学软件开发综合实训“邮件系统”后端项目。

当前目标是完成一个基于 Spring Boot 的站内邮件系统后端：用户可以注册、登录、发送邮件、查看收件箱、查看已发送、查看详情、标记已读、逻辑删除邮件，并为已删除列表、垃圾邮箱、统计、用户设置和邮件分析结果预留实现空间。

接口契约以 `docs/api.md` 为最高优先级。

## Current Stage

当前仓库处于后端 MVP 起步阶段：

- 已有 Spring Boot 基础骨架。
- 已有健康检查接口。
- 已更新 API、MVP、PRD、数据库设计和建表 SQL。
- 后续开发应先实现 P0 接口，再补齐 P1。

当前已有接口：

```http
GET /api/health
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": "mail-system-backend is running"
}
```

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Maven
- MySQL 8.x
- MyBatis
- Apifox 或类似 HTTP 工具用于接口测试

第一阶段不提前引入 Redis、WebSocket、完整 Spring Security 或 AI SDK。登录鉴权优先使用简单 token 方案。

## Directory Structure

```text
mail-system-backend
├─ AGENTS.md
├─ README.md
├─ pom.xml
├─ docs
│  ├─ 00-index.md
│  ├─ api.md
│  ├─ mvp.md
│  ├─ prd.md
│  ├─ database.md
│  └─ git-workflow.md
├─ sql
│  ├─ schema.sql
│  └─ init-data.sql
└─ src
   └─ main
      ├─ java
      │  └─ com/scut/mailsystem
      │     ├─ common
      │     ├─ config
      │     ├─ controller
      │     ├─ dto
      │     ├─ entity
      │     ├─ exception
      │     ├─ mapper
      │     ├─ service
      │     ├─ utils
      │     └─ vo
      └─ resources
         ├─ application.yml
         └─ mapper
```

## MVP Scope

按 `docs/api.md` 的优先级开发：

P0：

- 用户注册、登录、当前用户信息。
- 发送邮件。
- 收件箱、已发送、邮件详情。
- 标记已读、逻辑删除。

P1：

- 用户设置和 AI 配置状态。
- 已删除列表、垃圾邮箱、侧边栏统计。
- 搜索、过滤、优先级、风险等级和分析字段展示。

P2：

- 修改密码。
- 恢复邮件。
- 重新分析邮件。

## Environment Requirements

- JDK 17
- Maven 3.8+
- MySQL 8.x
- IntelliJ IDEA
- Apifox 或 Postman

检查 Java 和 Maven：

```bash
java -version
mvn -version
```

## Database Initialization

数据库结构和初始化数据维护在：

```text
sql/schema.sql
sql/init-data.sql
```

当前核心表：

- `sys_user`
- `user_settings`
- `mail_message`
- `mail_recipient`
- `mail_analysis`

初始化或重建数据库：

```bash
mysql -u root -p < sql/schema.sql
```

注意：当前 `schema.sql` 是开发环境重建脚本，会删除并重建核心表。执行前确认本地数据可以清空。

导入测试账号和默认用户设置：

```bash
mysql -u root -p < sql/init-data.sql
```

默认测试用户包括 `admin`、`alice`、`bob`，默认密码均为 `123456`。

## Configuration

基础配置文件：

```text
src/main/resources/application.yml
```

当前配置：

```yaml
server:
  port: 8080

spring:
  application:
    name: mail-system-backend
```

后续接入 MySQL 时，在本地配置中补充数据源。不要提交真实数据库密码、私有 token 或 API Key。

示例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mail_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## Start Backend

在项目根目录执行：

```bash
mvn spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080/api/health
```

也可以先编译：

```bash
mvn clean package
```

再运行生成的 jar：

```bash
java -jar target/mail-system-backend-0.0.1-SNAPSHOT.jar
```

## API Docs

API 契约维护在：

```text
docs/api.md
```

统一成功响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

统一分页响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 26,
    "totalPages": 3,
    "records": []
  }
}
```

业务接口除注册、登录、健康检查外，需要携带：

```http
Authorization: Bearer <token>
```

时间字段统一返回 ISO 8601 字符串，例如：

```text
2026-05-25T16:04:00
```

## Core API List

| 方法 | 接口 | 优先级 |
| --- | --- | --- |
| POST | `/api/auth/register` | P0 |
| POST | `/api/auth/login` | P0 |
| POST | `/api/auth/logout` | P0，可选后端实现 |
| GET | `/api/users/me` | P0 |
| PUT | `/api/users/password` | P2 |
| GET | `/api/users/settings` | P1 |
| PUT | `/api/users/settings` | P1 |
| POST | `/api/mails` | P0 |
| GET | `/api/mails/inbox` | P0/P1 |
| GET | `/api/mails/sent` | P0/P1 |
| GET | `/api/mails/trash` | P1 |
| GET | `/api/mails/spam` | P1 |
| GET | `/api/mails/{mailId}` | P0/P1 |
| PATCH | `/api/mails/{mailId}/read` | P0 |
| DELETE | `/api/mails/{mailId}` | P0 |
| PATCH | `/api/mails/{mailId}/restore` | P2 |
| GET | `/api/mails/statistics` | P1 |
| POST | `/api/mails/{mailId}/analysis/retry` | P2 |
| GET | `/api/health` | 已有接口 |

## Documents

推荐阅读顺序：

1. `AGENTS.md`
2. `docs/00-index.md`
3. `docs/api.md`
4. `docs/mvp.md`
5. `docs/prd.md`
6. `docs/database.md`
7. `docs/git-workflow.md`
8. `README.md`

## Git Workflow

项目使用以下分支模型：

```text
main
dev
feature/*
```

推荐流程：

```bash
git switch dev
git pull origin dev
git switch -c feature/<task-name>
```

提交信息格式：

```text
type(scope): message
```

示例：

```text
docs(api): update mail api contract
feat(auth): implement login api
feat(mail): implement send mail api
fix(mail): fix mail detail permission check
```

详细规则见 `docs/git-workflow.md`。

## Development Branch Plan

所有功能分支都从 `dev` 创建，完成后先合并回 `dev`。`main` 只用于稳定版本、演示版本和最终交付。

| 分支 | 优先级 | 主要范围 | 对应接口 |
| --- | --- | --- | --- |
| `feature/p0-foundation` | P0 | 统一响应、错误码、全局异常、分页对象、简单 token 基础工具、通用时间格式 | 全部接口基础能力 |
| `feature/p0-auth-user` | P0 | 用户注册、登录、退出登录、当前用户信息 | `POST /api/auth/register`、`POST /api/auth/login`、`POST /api/auth/logout`、`GET /api/users/me` |
| `feature/p0-mail-send` | P0 | 发送邮件、收件人校验、邮件主体和收件关系写入、默认分析状态 | `POST /api/mails` |
| `feature/p0-mail-list` | P0 | 收件箱、已发送列表、基础分页、基础排序 | `GET /api/mails/inbox`、`GET /api/mails/sent` |
| `feature/p0-mail-detail-status` | P0 | 邮件详情权限、详情自动已读、显式标记已读、收件人侧逻辑删除 | `GET /api/mails/{mailId}`、`PATCH /api/mails/{mailId}/read`、`DELETE /api/mails/{mailId}` |
| `feature/p1-user-settings` | P1 | 用户设置、AI 配置状态读写和脱敏展示 | `GET /api/users/settings`、`PUT /api/users/settings` |
| `feature/p1-ai-analysis` | P1 | 邮件发送后的默认/规则分析结果、`mail_analysis` 写入、列表和详情分析字段组装、AI 不可用时降级 | `POST /api/mails` 后置分析、`GET /api/mails/inbox`、`GET /api/mails/sent`、`GET /api/mails/trash`、`GET /api/mails/spam`、`GET /api/mails/{mailId}` 中的分析字段 |
| `feature/p1-mail-enhancement` | P1 | 已删除列表、垃圾邮箱、统计数量、列表搜索过滤、普通邮件列表增强 | `GET /api/mails/trash`、`GET /api/mails/spam`、`GET /api/mails/statistics`、列表筛选参数 |
| `feature/p2-optional` | P2 | 修改密码、恢复邮件、重新分析邮件 | `PUT /api/users/password`、`PATCH /api/mails/{mailId}/restore`、`POST /api/mails/{mailId}/analysis/retry` |

推荐开发顺序：

```text
feature/p0-foundation
feature/p0-auth-user
feature/p0-mail-send
feature/p0-mail-list
feature/p0-mail-detail-status
feature/p1-user-settings
feature/p1-ai-analysis
feature/p1-mail-enhancement
feature/p2-optional
```

P0 全部完成并通过 Apifox 闭环测试后，再开始 P1。AI 能力不单独新增 `/api/ai/*` 接口，配置归入 `feature/p1-user-settings`，分析结果生成和展示归入 `feature/p1-ai-analysis`。P2 不阻塞第一版演示。

## MVP Test Flow

P0 联调建议：

1. 注册用户 `alice`。
2. 注册用户 `bob`。
3. `alice` 登录并保存 token。
4. `alice` 给 `bob` 发送邮件。
5. `alice` 查看已发送。
6. `bob` 登录并保存 token。
7. `bob` 查看收件箱。
8. `bob` 查看邮件详情，详情接口自动标记已读。
9. 第三个用户查看该邮件详情，应返回无权限。
10. `bob` 调用标记已读接口。
11. `bob` 删除邮件。
12. `bob` 再查收件箱，确认邮件不再出现。

P1 联调建议：

- 查看已删除列表。
- 查看垃圾邮箱列表。
- 查看侧边栏统计。
- 测试搜索和过滤参数。
- 测试用户设置读取和更新。

