# Mail System Backend

华南理工大学软件开发综合实训“邮件系统”后端项目。

本仓库当前目标是先完成第一版后端 MVP：使用 Spring Boot 提供一套基于 HTTP 的站内邮件后端接口，让同一系统内的用户可以完成注册、登录、发送邮件、查看收件箱、查看已发送、查看详情、标记已读和逻辑删除等基础流程。

实训 PDF 中提到 SMTP、POP3、IMAP、附件、搜索、通知、同步、AI 插件等方向。当前 MVP 阶段先实现可运行、可测试、可演示的基础闭环；附件、搜索、通知、AI 插件等能力作为后续扩展，不阻塞第一版。

## Current Stage

当前仓库处于后端 MVP 起步阶段：

- 已完成项目基础骨架
- 已完成核心文档
- 已完成第一版数据库建表 SQL
- 已提供健康检查接口
- 后续将按 `docs/mvp.md` 和 `docs/api.md` 实现核心业务接口

当前已存在的接口：

```http
GET /api/health
```

响应：

```text
mail-system-backend is running
```

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Maven
- MySQL 8.x
- MyBatis 或 MyBatis Plus（后续实现数据库访问时引入）
- Apifox 或类似 HTTP 工具用于接口测试

第一版 MVP 不提前引入 Redis、Spring Security、WebSocket 或 AI SDK。登录鉴权优先使用简单 token 方案，后续稳定后再考虑 JWT、Redis 或完整 Spring Security。

## Directory Structure

```text
mail-system-backend
├─ AGENTS.md
├─ README.md
├─ pom.xml
├─ docs
│  ├─ 00-index.md
│  ├─ 2026年实训要求.pdf
│  ├─ mvp.md
│  ├─ database.md
│  ├─ api.md
│  └─ git-workflow.md
├─ sql
│  └─ schema.sql
└─ src
   └─ main
      ├─ java
      │  └─ com/scut/mailsystem
      └─ resources
         └─ application.yml
```

目录说明：

- `docs/`：项目需求、MVP 范围、数据库、API 和协作文档
- `sql/`：数据库建表和初始化脚本
- `src/`：后端源代码
- `README.md`：项目启动和演示入口
- `AGENTS.md`：Codex 协作规则和项目约束

## MVP Scope

第一版 MVP 必须完成以下后端能力：

1. 用户注册
2. 用户登录
3. 获取当前登录用户信息
4. 发送站内邮件
5. 查询收件箱
6. 查询已发送邮件
7. 查询邮件详情
8. 标记邮件为已读
9. 逻辑删除收件箱邮件

第一版暂不实现：

- 外部 SMTP、POP3、IMAP 邮件服务器互通
- 附件上传和预览
- 多收件人、抄送、密送
- WebSocket 实时通知
- 复杂管理员和权限系统
- Redis token 管理
- AI 邮件摘要、分类、垃圾邮件识别等功能

详细范围以 `docs/mvp.md` 为准。

## Environment Requirements

本地开发建议环境：

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

数据库结构维护在：

```text
sql/schema.sql
```

当前第一版核心表：

- `sys_user`
- `mail_message`
- `mail_recipient`

初始化数据库：

```bash
mysql -u root -p < sql/schema.sql
```

脚本会创建数据库：

```text
mail_system
```

当前 SQL 使用逻辑外键，不强制添加数据库级外键约束。表关系和字段语义以 `docs/database.md` 为准。

## Configuration

当前基础配置文件：

```text
src/main/resources/application.yml
```

现有配置：

```yaml
server:
  port: 8080

spring:
  application:
    name: mail-system-backend
```

后续接入 MySQL 时，应在 `application.yml` 或本地专用配置中补充数据源配置。不要提交真实数据库密码、私有 token 或 API key。

示例格式：

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

第一版计划接口：

```http
POST   /api/auth/register
POST   /api/auth/login
GET    /api/users/me
POST   /api/mails
GET    /api/mails/inbox
GET    /api/mails/sent
GET    /api/mails/{mailId}
PATCH  /api/mails/{mailId}/read
DELETE /api/mails/{mailId}
GET    /api/health
```

除注册、登录和健康检查外，业务接口通过以下请求头携带登录凭证：

```http
Authorization: Bearer <token>
```

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## Documents

建议阅读顺序：

1. `AGENTS.md`
2. `docs/00-index.md`
3. `docs/mvp.md`
4. `docs/database.md`
5. `docs/api.md`
6. `docs/git-workflow.md`
7. `README.md`

文档职责：

- `docs/mvp.md`：第一版 MVP 范围和验收标准
- `docs/database.md`：数据库设计和字段语义
- `sql/schema.sql`：MySQL 建表 SQL
- `docs/api.md`：前后端接口契约
- `docs/git-workflow.md`：分支、提交和合并规范
- `docs/2026年实训要求.pdf`：课程实训原始要求

## Git Workflow

项目使用以下分支模型：

```text
main
dev
feature/*
```

分支含义：

- `main`：稳定分支，用于阶段交付、演示和最终提交
- `dev`：开发集成分支
- `feature/*`：具体功能或文档任务分支

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
docs(readme): update project startup guide
feat(auth): implement login api
feat(mail): implement send mail api
fix(mail): fix mail detail permission check
```

详细规则见 `docs/git-workflow.md`。

## MVP Test Flow

第一版业务接口完成后，建议使用 Apifox 按以下流程测试：

1. 注册用户 `alice`
2. 注册用户 `bob`
3. 使用 `alice` 登录并保存 token
4. 使用 `bob` 登录并保存 token
5. 使用 `alice` token 向 `bob` 发送邮件
6. 使用 `bob` token 查询收件箱
7. 使用 `alice` token 查询已发送
8. 使用 `bob` token 查询邮件详情
9. 使用第三个用户 token 查询该邮件详情，应返回无权限
10. 使用 `bob` token 标记邮件为已读
11. 使用 `bob` token 删除该邮件
12. 再次查询 `bob` 收件箱，确认邮件不再出现
13. 查询 `alice` 已发送，确认邮件仍然存在

## Future Extensions

当前 MVP 稳定后，可按实训要求继续扩展：

- 附件上传和文件隔离存储
- 邮件搜索和过滤
- 新邮件通知
- 客户端与服务器同步
- AI 垃圾邮件识别、邮件摘要、优先级排序等插件能力

扩展时应保持基础邮件流程独立可用，不要把 AI 或复杂扩展逻辑写死进基础发信流程。
