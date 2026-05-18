# API 接口设计文档

## 1. 文档定位

本文档定义邮件系统后端第一版 MVP 的 HTTP API 契约，是前后端联调和 Apifox 测试的主要依据。

第一版 API 只覆盖基础邮件系统闭环：

1. 用户注册
2. 用户登录
3. 获取当前登录用户信息
4. 发送邮件
5. 查询收件箱
6. 查询已发送邮件
7. 查询邮件详情
8. 标记邮件为已读
9. 逻辑删除收件箱邮件

第一版采用站内邮件模型，收件人通过系统内 `username` 识别，暂不实现真实外部 SMTP、POP3、IMAP 邮件协议互通。

## 2. 基础约定

### 2.1 Base URL

本地开发默认地址：

```text
http://localhost:8080
```

所有业务接口统一以 `/api` 开头。

### 2.2 数据格式

请求体和响应体均使用 JSON：

```http
Content-Type: application/json
```

### 2.3 时间格式

时间字段统一使用字符串格式：

```text
yyyy-MM-dd HH:mm:ss
```

示例：

```text
2026-05-18 16:30:00
```

### 2.4 ID 类型

所有业务 ID 使用长整型数字：

```json
{
  "id": 1
}
```

## 3. 统一响应格式

所有接口统一返回以下结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `code` | number | 是 | 业务状态码 |
| `message` | string | 是 | 响应说明 |
| `data` | any | 否 | 响应数据，失败时可为 `null` |

成功响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1
  }
}
```

失败响应示例：

```json
{
  "code": 400,
  "message": "username is required",
  "data": null
}
```

## 4. 通用状态码

| code | 含义 | 常见场景 |
| --- | --- | --- |
| `200` | 成功 | 请求处理成功 |
| `400` | 请求参数错误 | 缺少必填字段、字段格式不正确 |
| `401` | 未登录或 token 无效 | 未携带 token、token 过期或不存在 |
| `403` | 无权限 | 访问他人邮件、发件人尝试修改收件人状态 |
| `404` | 资源不存在 | 用户不存在、邮件不存在 |
| `409` | 资源冲突 | 用户名已存在 |
| `500` | 服务端错误 | 未预期异常 |

## 5. 鉴权约定

第一版使用简单 token 鉴权，不强制引入 Spring Security、JWT 或 Redis。

登录成功后，后端返回 token。除注册、登录、健康检查外，其他接口都需要在请求头中携带：

```http
Authorization: Bearer <token>
```

示例：

```http
Authorization: Bearer 8f1a2e4b-1234-4c88-9a10-abcdef123456
```

后端通过 token 识别当前登录用户。

## 6. 分页约定

列表接口统一使用以下查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | number | 否 | `1` | 页码，从 1 开始 |
| `size` | number | 否 | `10` | 每页数量 |

分页响应结构：

```json
{
  "page": 1,
  "size": 10,
  "total": 2,
  "items": []
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `page` | number | 当前页码 |
| `size` | number | 每页数量 |
| `total` | number | 总记录数 |
| `items` | array | 当前页数据 |

## 7. 用户与鉴权接口

### 7.1 用户注册

```http
POST /api/auth/register
```

说明：

注册一个系统用户。第一版中 `username` 是登录名，也是发送站内邮件时识别收件人的主要字段。

请求体：

```json
{
  "username": "alice",
  "password": "123456",
  "nickname": "Alice",
  "email": "alice@example.com"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名，唯一，建议 3 到 32 个字符 |
| `password` | string | 是 | 密码，建议 6 到 64 个字符 |
| `nickname` | string | 否 | 昵称，不传时可默认等于 `username` |
| `email` | string | 否 | 邮箱地址，第一版仅作为用户资料展示 |

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "nickname": "Alice",
    "email": "alice@example.com",
    "status": 1,
    "createdAt": "2026-05-18 16:30:00"
  }
}
```

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `400` | `username is required` | 用户名为空 |
| `400` | `password is required` | 密码为空 |
| `409` | `username already exists` | 用户名已存在 |

### 7.2 用户登录

```http
POST /api/auth/login
```

说明：

用户通过用户名和密码登录，登录成功后返回 token。

请求体：

```json
{
  "username": "alice",
  "password": "123456"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名 |
| `password` | string | 是 | 密码 |

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "8f1a2e4b-1234-4c88-9a10-abcdef123456",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "alice",
      "nickname": "Alice",
      "email": "alice@example.com",
      "status": 1
    }
  }
}
```

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `400` | `username is required` | 用户名为空 |
| `400` | `password is required` | 密码为空 |
| `401` | `invalid username or password` | 用户名或密码错误 |
| `403` | `user is disabled` | 用户被禁用 |

### 7.3 获取当前登录用户信息

```http
GET /api/users/me
```

鉴权：

需要 `Authorization` 请求头。

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "alice",
    "nickname": "Alice",
    "email": "alice@example.com",
    "status": 1,
    "createdAt": "2026-05-18 16:30:00"
  }
}
```

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `401` | `unauthorized` | 未登录或 token 无效 |

## 8. 邮件接口

### 8.1 发送邮件

```http
POST /api/mails
```

鉴权：

需要 `Authorization` 请求头。

说明：

当前登录用户向另一个已存在用户发送站内邮件。第一版只支持一个普通收件人，不支持附件、抄送、密送和群发。

请求体：

```json
{
  "recipientUsername": "bob",
  "subject": "实训会议通知",
  "content": "今晚 20:00 讨论邮件系统 MVP 进度。"
}
```

请求字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `recipientUsername` | string | 是 | 收件人的用户名 |
| `subject` | string | 是 | 邮件主题 |
| `content` | string | 是 | 邮件正文 |

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "mailId": 1001,
    "sender": {
      "id": 1,
      "username": "alice",
      "nickname": "Alice"
    },
    "recipient": {
      "id": 2,
      "username": "bob",
      "nickname": "Bob"
    },
    "subject": "实训会议通知",
    "sendTime": "2026-05-18 20:00:00"
  }
}
```

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `400` | `recipientUsername is required` | 收件人为空 |
| `400` | `subject is required` | 主题为空 |
| `400` | `content is required` | 正文为空 |
| `401` | `unauthorized` | 未登录或 token 无效 |
| `404` | `recipient not found` | 收件人不存在 |

### 8.2 查询收件箱

```http
GET /api/mails/inbox?page=1&size=10
```

鉴权：

需要 `Authorization` 请求头。

说明：

查询当前登录用户收到的邮件。已被当前用户逻辑删除的邮件不出现在普通收件箱列表中。

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | number | 否 | `1` | 页码 |
| `size` | number | 否 | `10` | 每页数量 |

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 1,
    "items": [
      {
        "mailId": 1001,
        "sender": {
          "id": 1,
          "username": "alice",
          "nickname": "Alice"
        },
        "subject": "实训会议通知",
        "sendTime": "2026-05-18 20:00:00",
        "readStatus": 0,
        "readStatusText": "未读"
      }
    ]
  }
}
```

列表字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `mailId` | number | 邮件 ID |
| `sender` | object | 发件人信息 |
| `subject` | string | 邮件主题 |
| `sendTime` | string | 发送时间 |
| `readStatus` | number | 已读状态，`0` 未读，`1` 已读 |
| `readStatusText` | string | 已读状态文本 |

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `401` | `unauthorized` | 未登录或 token 无效 |

### 8.3 查询已发送邮件

```http
GET /api/mails/sent?page=1&size=10
```

鉴权：

需要 `Authorization` 请求头。

说明：

查询当前登录用户发送过的邮件。

查询参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | number | 否 | `1` | 页码 |
| `size` | number | 否 | `10` | 每页数量 |

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 1,
    "items": [
      {
        "mailId": 1001,
        "recipient": {
          "id": 2,
          "username": "bob",
          "nickname": "Bob"
        },
        "subject": "实训会议通知",
        "sendTime": "2026-05-18 20:00:00"
      }
    ]
  }
}
```

列表字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `mailId` | number | 邮件 ID |
| `recipient` | object | 收件人信息 |
| `subject` | string | 邮件主题 |
| `sendTime` | string | 发送时间 |

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `401` | `unauthorized` | 未登录或 token 无效 |

### 8.4 查询邮件详情

```http
GET /api/mails/{mailId}
```

鉴权：

需要 `Authorization` 请求头。

说明：

查询邮件详情。只有发件人和收件人可以查看该邮件。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `mailId` | number | 是 | 邮件 ID |

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "mailId": 1001,
    "subject": "实训会议通知",
    "content": "今晚 20:00 讨论邮件系统 MVP 进度。",
    "sender": {
      "id": 1,
      "username": "alice",
      "nickname": "Alice",
      "email": "alice@example.com"
    },
    "recipients": [
      {
        "id": 2,
        "username": "bob",
        "nickname": "Bob",
        "email": "bob@example.com",
        "recipientType": 1,
        "recipientTypeText": "收件人",
        "readStatus": 0,
        "readStatusText": "未读",
        "readTime": null
      }
    ],
    "sendTime": "2026-05-18 20:00:00",
    "currentUserRole": "recipient"
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `mailId` | number | 邮件 ID |
| `subject` | string | 邮件主题 |
| `content` | string | 邮件正文 |
| `sender` | object | 发件人信息 |
| `recipients` | array | 收件人列表，第一版通常只有一个元素 |
| `recipientType` | number | 收件人类型，`1` 普通收件人，预留 `2` 抄送、`3` 密送 |
| `readStatus` | number | 已读状态，`0` 未读，`1` 已读 |
| `sendTime` | string | 发送时间 |
| `currentUserRole` | string | 当前用户身份，`sender` 或 `recipient` |

行为约定：

- 发件人查看详情时，不修改收件人的已读状态
- 收件人是否自动标记已读由实现决定；第一版推荐使用 `PATCH /api/mails/{mailId}/read` 显式标记已读

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `401` | `unauthorized` | 未登录或 token 无效 |
| `403` | `no permission to view this mail` | 无权限查看 |
| `404` | `mail not found` | 邮件不存在 |

### 8.5 标记邮件为已读

```http
PATCH /api/mails/{mailId}/read
```

鉴权：

需要 `Authorization` 请求头。

说明：

将当前登录用户收到的指定邮件标记为已读。只有收件人可以操作，发件人不能修改收件人的已读状态。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `mailId` | number | 是 | 邮件 ID |

请求体：

无。

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "mailId": 1001,
    "readStatus": 1,
    "readStatusText": "已读",
    "readTime": "2026-05-18 20:05:00"
  }
}
```

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `401` | `unauthorized` | 未登录或 token 无效 |
| `403` | `only recipient can mark mail as read` | 当前用户不是收件人 |
| `404` | `mail not found` | 邮件不存在 |
| `404` | `recipient record not found` | 当前用户没有对应收件记录 |

### 8.6 逻辑删除收件箱邮件

```http
DELETE /api/mails/{mailId}
```

鉴权：

需要 `Authorization` 请求头。

说明：

第一版删除只表示当前收件人从自己的收件箱中删除邮件，不物理删除邮件主体记录，也不影响发件人的已发送列表。

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `mailId` | number | 是 | 邮件 ID |

请求体：

无。

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "mailId": 1001,
    "deleted": 1
  }
}
```

可能错误：

| code | message 示例 | 说明 |
| --- | --- | --- |
| `401` | `unauthorized` | 未登录或 token 无效 |
| `403` | `only recipient can delete inbox mail` | 当前用户不是收件人 |
| `404` | `mail not found` | 邮件不存在 |
| `404` | `recipient record not found` | 当前用户没有对应收件记录 |

## 9. 健康检查接口

### 9.1 服务健康检查

```http
GET /api/health
```

鉴权：

不需要。

说明：

用于确认后端服务是否启动。该接口服务于本地开发和演示，不属于邮件业务核心流程。

成功响应：

```text
mail-system-backend is running
```

后续如果统一所有接口响应格式，可以改为：

```json
{
  "code": 200,
  "message": "success",
  "data": "mail-system-backend is running"
}
```

## 10. 权限规则汇总

| 场景 | 规则 |
| --- | --- |
| 查询收件箱 | 只能查询 `recipientId` 等于当前用户的邮件 |
| 查询已发送 | 只能查询 `senderId` 等于当前用户的邮件 |
| 查询详情 | 发件人或收件人可以查看，其他用户禁止 |
| 标记已读 | 只有收件人可以标记自己的收件记录 |
| 逻辑删除 | 只有收件人可以删除自己的收件记录 |
| 删除影响范围 | 只影响当前收件人的收件箱，不影响邮件主体和发件人已发送列表 |

## 11. 枚举值约定

### 11.1 用户状态

| 值 | 含义 |
| --- | --- |
| `1` | 正常 |
| `0` | 禁用 |

### 11.2 邮件已读状态

| 值 | 含义 |
| --- | --- |
| `0` | 未读 |
| `1` | 已读 |

### 11.3 逻辑删除状态

| 值 | 含义 |
| --- | --- |
| `0` | 未删除 |
| `1` | 已删除 |

### 11.4 收件人类型

| 值 | 含义 | 第一版是否实现 |
| --- | --- | --- |
| `1` | 普通收件人 | 是 |
| `2` | 抄送 CC | 否，预留 |
| `3` | 密送 BCC | 否，预留 |

## 12. 第一版接口清单

| 方法 | 路径 | 鉴权 | 说明 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | 否 | 用户注册 |
| `POST` | `/api/auth/login` | 否 | 用户登录 |
| `GET` | `/api/users/me` | 是 | 获取当前用户信息 |
| `POST` | `/api/mails` | 是 | 发送邮件 |
| `GET` | `/api/mails/inbox` | 是 | 查询收件箱 |
| `GET` | `/api/mails/sent` | 是 | 查询已发送邮件 |
| `GET` | `/api/mails/{mailId}` | 是 | 查询邮件详情 |
| `PATCH` | `/api/mails/{mailId}/read` | 是 | 标记邮件为已读 |
| `DELETE` | `/api/mails/{mailId}` | 是 | 逻辑删除收件箱邮件 |
| `GET` | `/api/health` | 否 | 服务健康检查 |

## 13. Apifox 测试建议流程

建议按以下顺序测试第一版 MVP：

1. 注册用户 `alice`
2. 注册用户 `bob`
3. 使用 `alice` 登录并保存 token
4. 使用 `bob` 登录并保存 token
5. 使用 `alice` token 向 `bob` 发送邮件
6. 使用 `bob` token 查询收件箱，确认能看到邮件
7. 使用 `alice` token 查询已发送，确认能看到邮件
8. 使用 `bob` token 查询邮件详情，确认能看到完整正文
9. 使用第三个用户 token 查询该邮件详情，应返回 `403`
10. 使用 `bob` token 标记邮件为已读
11. 使用 `bob` token 再次查询收件箱，确认状态为已读
12. 使用 `bob` token 删除该邮件
13. 使用 `bob` token 再次查询收件箱，确认该邮件不再出现
14. 使用 `alice` token 查询已发送，确认该邮件仍然存在
