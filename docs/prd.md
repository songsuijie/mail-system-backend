# Mail System Backend MVP PRD

## 1. 文档目的

本文档面向前端联调和项目演示，说明第一版邮件系统需要支持的页面、用户流程、交互规则和接口范围。

接口路径、请求字段、响应字段、错误码、枚举值和分页格式以 `docs/api.md` 为最高优先级。本文档只做产品和页面层说明。

## 2. 产品定位

本项目是课程实训中的站内邮件系统后端。用户在同一个系统内注册、登录，并通过用户名向其他已存在用户发送邮件。

当前版本不接入真实 SMTP、POP3、IMAP，不做附件、多收件人、CC、BCC、草稿、邮件撤回和 WebSocket 通知。

接口文档已为搜索过滤、垃圾邮箱、已删除列表、用户设置、统计数量和 AI 分析结果预留接口。开发时按 P0、P1、P2 分阶段完成。

## 3. 用户角色

第一版只有普通用户，不区分管理员和复杂角色。

| 角色 | 说明 |
| --- | --- |
| 未登录用户 | 只能注册、登录、访问健康检查 |
| 登录用户 | 可以发送邮件、查看收件箱、已发送、详情、标记已读、删除邮件，并在 P1 阶段访问已删除、垃圾邮箱、设置和统计 |

## 4. 页面范围

### 4.1 登录 / 注册页

建议路由：

```text
/auth
```

需要接口：

```http
POST /api/auth/register
POST /api/auth/login
```

注册表单：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| username | 是 | 用户名，全局唯一 |
| password | 是 | 密码 |
| nickname | 否 | 昵称，不传时后端默认等于 username |

登录表单：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| username | 是 | 用户名 |
| password | 是 | 密码 |

交互规则：

- 登录成功后保存 `token`。
- 后续业务请求统一携带 `Authorization: Bearer <token>`。
- 失败时展示后端返回的 `message`。
- `POST /api/auth/logout` 在后端可选实现；如果未实现，前端删除本地 token 即可退出。

### 4.2 主布局

建议路由：

```text
/
```

主布局包含：

- 侧边栏：收件箱、已发送、已删除、垃圾邮箱、写邮件、设置。
- 顶部区域：当前用户昵称或用户名、退出入口。
- 内容区域：根据路由展示列表、详情、写邮件或设置页面。

需要接口：

```http
GET /api/users/me
GET /api/mails/statistics
```

说明：

- `GET /api/users/me` 属于 P0。
- `GET /api/mails/statistics` 属于 P1；未实现前，前端可以隐藏数量或在进入列表页后使用列表总数。

### 4.3 收件箱页

建议路由：

```text
/mails/inbox
```

需要接口：

```http
GET /api/mails/inbox
```

P0 展示：

- 发件人。
- 主题。
- 正文摘要 `snippet`。
- 发送时间 `sentAt`。
- 当前用户已读状态 `read`。

P1 展示：

- 优先级 `priority` / `priorityLabel`。
- 垃圾等级 `spamLevel`。
- 风险等级 `riskLevel` / `riskLabel`。
- 分析状态 `analysisStatus`。

查询参数：

- P0：`page`、`size`。
- P1：`keyword`、`readStatus`、`senderUsername`、`priority`、`startTime`、`endTime`。

交互规则：

- 点击列表项进入邮件详情页。
- 未读邮件需要有明显视觉区分。
- 已删除邮件和垃圾邮件不出现在普通收件箱。

### 4.4 已发送页

建议路由：

```text
/mails/sent
```

需要接口：

```http
GET /api/mails/sent
```

P0 展示：

- 收件人。
- 主题。
- 正文摘要。
- 发送时间。

P1 查询参数：

- `keyword`
- `recipientUsername`
- `startTime`
- `endTime`

规则：

- 只展示当前用户发送过的邮件。
- 已发送列表不受收件人删除状态影响。
- 当前版本只支持单收件人。

### 4.5 写邮件页

建议路由：

```text
/mails/compose
```

需要接口：

```http
POST /api/mails
```

说明：

- `POST /api/mails` 属于 P0。
- 当前最终版不提供 `/api/users/search`，前端直接输入收件人的 `recipientUsername`。

表单字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| recipientUsername | 是 | 收件人的系统用户名 |
| subject | 是 | 邮件主题 |
| content | 是 | 邮件正文 |

交互规则：

- 提交前校验收件人、主题、正文不能为空。
- 收件人不存在时展示后端错误。
- 发送成功后可跳转到已发送页，也可清空表单继续写信。
- 当前版本不展示附件、CC、BCC、多收件人入口。

### 4.6 邮件详情页

建议路由：

```text
/mails/:mailId
```

需要接口：

```http
GET /api/mails/{mailId}
PATCH /api/mails/{mailId}/read
DELETE /api/mails/{mailId}
```

展示字段：

- `subject`
- `content`
- `sender`
- `recipient`
- `sentAt`
- `currentUserRole`
- `read`
- `deleted`
- `spam`
- `analysis`

交互规则：

- 发件人和收件人可查看详情。
- 其他用户无权查看，后端返回 `40003`。
- 收件人打开详情时，如果原本未读，详情接口会自动标记为已读。
- `PATCH /api/mails/{mailId}/read` 仍用于显式标记已读。
- 只有收件人显示删除操作。
- 删除成功后邮件进入已删除列表，并从普通收件箱和垃圾邮箱移除。

### 4.7 已删除页

建议路由：

```text
/mails/trash
```

需要接口：

```http
GET /api/mails/trash
```

说明：

- 该接口属于 P1。
- 当前版本删除是收件人侧逻辑删除。
- 已删除页只展示当前用户删除过的收件邮件。
- 恢复邮件接口 `PATCH /api/mails/{mailId}/restore` 属于 P2，不强制第一阶段实现。

### 4.8 垃圾邮箱页

建议路由：

```text
/mails/spam
```

需要接口：

```http
GET /api/mails/spam
```

说明：

- 该接口属于 P1。
- 只展示当前用户收到的垃圾或高风险邮件。
- 垃圾邮箱需要展示风险等级、垃圾等级和风险原因。
- 当前版本不强制支持“移出垃圾邮箱”。

### 4.9 设置页

建议路由：

```text
/settings
```

需要接口：

```http
GET /api/users/settings
PUT /api/users/settings
```

说明：

- 设置页属于 P1。
- 当前版本只适配 OpenAI Compatible 格式的大模型服务。
- 后端不能返回完整 API Key，只能返回脱敏后的 `maskedApiKey`。
- 用户未配置模型时，`modelConfigured` 应为 `false`。
- 模型调用失败不能影响邮件主流程。

### 4.10 修改密码弹窗

需要接口：

```http
PUT /api/users/password
```

说明：

- 该接口属于 P2。
- 请求体包含 `oldPassword`、`newPassword`、`confirmPassword`。
- 前端和后端都需要校验两次新密码一致。

## 5. 全局接口约定

### 5.1 统一响应

成功响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败响应：

```json
{
  "code": 40001,
  "message": "用户名或密码错误",
  "data": null
}
```

前端判断规则：

- `code === 0`：请求成功。
- `code === 40002`：未登录或 token 无效，清理登录态并跳转登录页。
- `code === 40003`：无权限。
- `code === 40004`：资源不存在。
- `code === 40005`：用户名已存在。
- `code === 40006`：收件人不存在。

### 5.2 分页

分页请求参数：

```text
page=1&size=10
```

分页响应结构：

```json
{
  "page": 1,
  "size": 10,
  "total": 26,
  "totalPages": 3,
  "records": []
}
```

### 5.3 时间格式

后端统一返回 ISO 8601 字符串：

```text
2026-05-25T16:04:00
```

前端负责格式化为相对时间或本地展示格式。

## 6. 接口清单

| 模块 | 方法 | 接口 | 优先级 |
| --- | --- | --- | --- |
| 认证 | POST | `/api/auth/register` | P0 |
| 认证 | POST | `/api/auth/login` | P0 |
| 认证 | POST | `/api/auth/logout` | P0，可选后端实现 |
| 用户 | GET | `/api/users/me` | P0 |
| 用户 | PUT | `/api/users/password` | P2 |
| 设置 | GET | `/api/users/settings` | P1 |
| 设置 | PUT | `/api/users/settings` | P1 |
| 邮件 | POST | `/api/mails` | P0 |
| 邮件 | GET | `/api/mails/inbox` | P0/P1 |
| 邮件 | GET | `/api/mails/sent` | P0/P1 |
| 邮件 | GET | `/api/mails/trash` | P1 |
| 邮件 | GET | `/api/mails/spam` | P1 |
| 邮件 | GET | `/api/mails/{mailId}` | P0/P1 |
| 邮件状态 | PATCH | `/api/mails/{mailId}/read` | P0 |
| 邮件状态 | DELETE | `/api/mails/{mailId}` | P0 |
| 邮件状态 | PATCH | `/api/mails/{mailId}/restore` | P2 |
| 统计 | GET | `/api/mails/statistics` | P1 |
| AI | POST | `/api/mails/{mailId}/analysis/retry` | P2 |
| 健康检查 | GET | `/api/health` | 已有接口 |

## 7. 推荐联调流程

P0：

1. 注册用户 `alice`。
2. 注册用户 `bob`。
3. `alice` 登录。
4. `alice` 给 `bob` 发送邮件。
5. `alice` 查看已发送。
6. `bob` 登录。
7. `bob` 查看收件箱。
8. `bob` 查看邮件详情，详情接口自动标记已读。
9. 第三个用户尝试查看该邮件详情，返回无权限。
10. `bob` 调用标记已读接口。
11. `bob` 删除邮件。
12. `bob` 再查收件箱，该邮件不再出现。

P1：

1. `bob` 查看已删除列表。
2. 查看邮箱统计数量。
3. 测试收件箱搜索、未读筛选和优先级筛选。
4. 测试垃圾邮箱列表。
5. 测试用户设置读取和更新。

## 8. 当前不做

- SMTP、POP3、IMAP 接入。
- 附件上传、下载、预览。
- 多收件人、CC、BCC。
- 草稿箱。
- 邮件撤回。
- WebSocket 实时通知。
- 管理员后台。
- 复杂角色权限。
- 流式输出。
- AI thinking 展示。
- Claude 原生协议适配。
- 多模型配置。

