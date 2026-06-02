# 接口文档（最终确认版）

版本：API Final
用途：前后端联调、Apifox 建模、后端接口实现、测试验收
状态：已按当前确认结果修订

---

## 1. 文档说明

本文档用于智能站内邮件系统前后端联调，覆盖认证、用户信息、用户设置、邮件发送、邮件列表、邮件详情、邮件状态、垃圾邮箱、搜索过滤、AI 分析结果等接口。

当前系统是**站内邮件系统**，不接入真实外部 SMTP、POP3、IMAP 协议。邮件收发本质上是系统内部用户之间的业务数据流转。

### 1.1 接口优先级

| 优先级 | 含义 |
|---|---|
| P0 | 第一阶段必须实现，保证基础邮件闭环可演示 |
| P1 | 当前版本推荐实现，支撑前端页面完整展示 |
| P2 | 可选增强功能，时间充足再做 |

说明：P2 接口会保留在本文档中，但在接口说明中标注“可选实现”。

---

## 2. 通用约定

### 2.1 基础路径

所有接口统一以 `/api` 作为前缀。

示例：

```http
POST /api/auth/login
GET /api/mails/inbox
```

### 2.2 请求格式

除特别说明外，所有接口默认使用 JSON：

```http
Content-Type: application/json
```

当前版本不提供附件上传接口，不涉及 `multipart/form-data`。

### 2.3 登录态传递

登录成功后，后端返回 `token`。前端后续请求统一在请求头中携带：

```http
Authorization: Bearer <token>
```

示例：

```http
Authorization: Bearer eyJhbGciOi...
```

未携带 token、token 无效、token 过期时，后端返回未登录错误，前端清空登录态并跳转登录页。

### 2.4 统一响应格式

所有接口统一返回：

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

### 2.5 分页响应格式

列表接口统一使用分页结构：

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

分页参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | integer | 否 | 页码，从 1 开始，默认 1 |
| size | integer | 否 | 每页数量，默认 10 |

说明：`totalPages` 会随着 `size` 的变化而变化，不是固定值。

### 2.6 时间格式

后端统一返回 ISO 8601 字符串：

```text
2026-05-25T16:04:00
```

前端负责格式化为：

```text
31 分钟前
大约 2 小时前
1 天前
2026年05月25日 16:04
```

---

## 3. 常用枚举

### 3.1 readStatus

| 值 | 说明 |
|---|---|
| ALL | 全部 |
| READ | 已读 |
| UNREAD | 未读 |

### 3.2 userMailRole

| 值 | 说明 |
|---|---|
| SENDER | 当前用户是发件人 |
| RECIPIENT | 当前用户是收件人 |

### 3.3 priority

| 值 | 说明 |
|---|---|
| LOW | 低优先级 |
| MEDIUM | 中优先级 |
| HIGH | 高优先级 |

### 3.4 spamLevel

| 值 | 说明 |
|---|---|
| NONE | 非垃圾邮件 |
| LOW | 低垃圾风险 |
| MEDIUM | 中垃圾风险 |
| HIGH | 高垃圾风险 |

### 3.5 riskLevel

| 值 | 说明 |
|---|---|
| SAFE | 安全 |
| LOW | 低风险 |
| MEDIUM | 中风险 |
| HIGH | 高风险 |

### 3.6 analysisStatus

| 值 | 说明 |
|---|---|
| NOT_STARTED | 未分析 |
| PENDING | 分析中 |
| SUCCESS | 分析成功 |
| FAILED | 分析失败 |
| DISABLED | 用户关闭 AI 功能 |

### 3.7 provider

当前版本只适配 OpenAI Compatible 格式的大模型服务。

| 值 | 服务商 | 默认 Base URL | 模型名示例 |
|---|---|---|---|
| qwen | 通义千问 Qwen | https://dashscope.aliyuncs.com/compatible-mode/v1 | qwen-flash、qwen-plus |
| openai | OpenAI / GPT | https://api.openai.com/v1 | gpt-4o-mini、gpt-4.1-mini |
| deepseek | DeepSeek | https://api.deepseek.com | deepseek-chat、deepseek-reasoner |
| kimi | Kimi / Moonshot | https://api.moonshot.ai/v1 | moonshot-v1-8k、kimi-latest |
| glm | 智谱 GLM | https://open.bigmodel.cn/api/paas/v4/ | glm-4-plus、glm-4-air |
| siliconflow | 硅基流动 | https://api.siliconflow.cn/v1 | deepseek-ai/DeepSeek-V3、Qwen/Qwen3-32B |
| custom | 自定义 OpenAI 兼容服务 | 用户填写 | 用户填写 |

---

## 4. 错误码约定

| code | HTTP 状态码 | 说明 |
|---|---|---|
| 0 | 200 | 成功 |
| 40000 | 400 | 请求参数错误 |
| 40001 | 401 | 用户名或密码错误 |
| 40002 | 401 | 未登录或 token 无效 |
| 40003 | 403 | 无权限访问该资源 |
| 40004 | 404 | 资源不存在 |
| 40005 | 409 | 用户名已存在 |
| 40006 | 409 | 收件人不存在 |
| 40007 | 400 | 邮件主题不能为空 |
| 40008 | 400 | 邮件正文不能为空 |
| 50000 | 500 | 系统内部错误 |
| 50001 | 500 | AI 分析失败，但不影响邮件主流程 |

---

## 5. 通用数据结构

### 5.1 UserBrief

用于邮件接口中的发件人、收件人展示。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名 |
| nickname | string | 是 | 昵称 |

示例：

```json
{
  "username": "zhangsan",
  "nickname": "张三"
}
```

说明：当前接口响应严格不返回 `userId`。

### 5.2 CurrentUserVO

用于当前登录用户信息。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名 |
| nickname | string | 是 | 昵称 |
| emailAddress | string | 是 | 系统内邮箱地址 |
| avatarText | string | 是 | 前端头像占位文字，例如“管”“张”“李” |

### 5.3 RichTextNode

邮件正文 `content` 统一使用**富文本数组**：

```ts
content: RichTextNode[]
```

当前最终约定：`content` 不再使用普通字符串，也不额外包 `doc` 根对象；最外层直接是节点数组。

允许节点类型：

| 节点类型 | type | 必须/建议 | 说明 |
|---|---|---|---|
| 段落 | paragraph | 必须支持 | 普通文本块 |
| 文本 | text | 必须支持 | 文本内容 |
| 链接 | link | 必须支持 | 超链接 |
| 图片 | image | 必须支持 | 正文图片 |
| 无序列表 | ul | 建议支持 | 项目列表 |
| 有序列表 | ol | 建议支持 | 编号列表 |
| 列表项 | li | 建议支持 | 列表内容 |

文本节点可选样式字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| bold | boolean | 否 | 是否加粗 |
| italic | boolean | 否 | 是否斜体 |
| underline | boolean | 否 | 是否下划线 |

富文本正文示例：

```json
[
  {
    "type": "paragraph",
    "children": [
      {
        "type": "text",
        "text": "普通文本"
      },
      {
        "type": "text",
        "text": "加粗文本",
        "bold": true
      },
      {
        "type": "text",
        "text": "斜体文本",
        "italic": true
      },
      {
        "type": "text",
        "text": "下划线文本",
        "underline": true
      }
    ]
  },
  {
    "type": "paragraph",
    "children": [
      {
        "type": "link",
        "text": "查看详情",
        "href": "https://example.com"
      }
    ]
  },
  {
    "type": "image",
    "resourceId": "res_123"
  },
  {
    "type": "ul",
    "children": [
      {
        "type": "li",
        "children": [
          {
            "type": "text",
            "text": "无序列表项1"
          }
        ]
      }
    ]
  },
  {
    "type": "ol",
    "children": [
      {
        "type": "li",
        "children": [
          {
            "type": "text",
            "text": "有序列表项1"
          }
        ]
      }
    ]
  }
]
```

### 5.4 MailAnalysisVO

邮件详情页返回完整分析信息。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| analysisStatus | string | 是 | 分析状态：NOT_STARTED / PENDING / SUCCESS / FAILED / DISABLED |
| summary | string | 是 | 邮件摘要 |
| spamLevel | string | 是 | 垃圾等级：NONE / LOW / MEDIUM / HIGH |
| spamLevelLabel | string | 是 | 垃圾等级中文标签 |
| spamReason | string | 是 | 垃圾识别原因 |
| riskLevel | string | 是 | 风险等级：SAFE / LOW / MEDIUM / HIGH |
| riskLabel | string | 是 | 风险中文标签 |
| priority | string | 是 | 优先级：LOW / MEDIUM / HIGH |
| priorityLabel | string | 是 | 优先级中文标签 |
| priorityReason | string | 是 | 优先级判断原因 |
| riskReason | string/null | 是 | 风险原因，可为 null |
| replySuggestions | string[] | 是 | 回复建议列表 |

说明：当前最终版不纳入以下建议补充字段：`aiProvider`、`modelName`、`priorityScore`、`spamScore`、`riskScore`。

示例：

```json
{
  "analysisStatus": "SUCCESS",
  "summary": "实验报告需在本周五前提交，内容包括实验目的、步骤、结果分析和结论。",
  "spamLevel": "NONE",
  "spamLevelLabel": "非垃圾邮件",
  "spamReason": "未发现中奖、广告、诱导点击或异常链接等垃圾邮件特征。",
  "riskLevel": "SAFE",
  "riskLabel": "安全",
  "priority": "HIGH",
  "priorityLabel": "高优先级",
  "priorityReason": "包含截止日期提醒",
  "riskReason": null,
  "replySuggestions": [
    "好的，我会按时提交。",
    "收到，有问题会联系您。"
  ]
}
```

### 5.5 MailListItemVO

收件箱、已发送、已删除、垃圾邮箱四个列表接口统一使用同一个 `MailListItemVO`。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mailId | integer | 是 | 邮件 ID |
| subject | string | 是 | 邮件主题 |
| snippet | string | 是 | 正文摘要或截断内容 |
| sender | UserBrief | 是 | 发件人信息 |
| recipient | UserBrief | 是 | 收件人信息 |
| sentAt | string | 是 | 发送时间，ISO 8601 |
| read | boolean/null | 是 | 当前用户是否已读；已发送列表中可为 null |
| priority | string | 是 | 优先级：LOW / MEDIUM / HIGH |
| priorityLabel | string | 是 | 优先级中文标签 |
| spam | boolean | 是 | 是否垃圾邮件 |
| spamLevel | string | 是 | 垃圾等级：NONE / LOW / MEDIUM / HIGH |
| riskLevel | string | 是 | 风险等级：SAFE / LOW / MEDIUM / HIGH |
| riskLabel | string | 是 | 风险中文标签 |
| analysisStatus | string | 是 | AI 分析状态 |
| riskReason | string/null | 否 | 风险原因，列表页可为空 |
| deletedAt | string | 否 | 删除时间，只在已删除列表常见 |
| spamLevelLabel | string | 否 | 垃圾等级中文标签，只在垃圾邮箱列表常见 |

说明：

- 四个列表使用同一个 VO。
- `read` 字段保留，允许 `boolean` 或 `null`。
- `deletedAt` 是非必填字段，主要用于已删除列表。
- `spamLevelLabel` 是非必填字段，主要用于垃圾邮箱列表。
- `riskReason` 是非必填字段，可为 `null`。

---

## 6. 认证模块

### 6.1 用户注册 P0

```http
POST /api/auth/register
```

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名，全局唯一 |
| password | string | 是 | 密码，后端必须加密存储 |
| nickname | string | 否 | 用户昵称，不传则默认等于 username |

请求示例：

```json
{
  "username": "zhangsan",
  "password": "123456",
  "nickname": "张三"
}
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "username": "zhangsan",
    "nickname": "张三"
  }
}
```

#### 失败情况

| 场景 | code | message |
|---|---|---|
| 用户名为空 | 40000 | 用户名不能为空 |
| 密码为空 | 40000 | 密码不能为空 |
| 用户名已存在 | 40005 | 用户名已存在 |

---

### 6.2 用户登录 P0

```http
POST /api/auth/login
```

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

请求示例：

```json
{
  "username": "admin",
  "password": "123456"
}
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "mock-token-admin-123456",
    "user": {
      "username": "admin",
      "nickname": "管理员",
      "emailAddress": "admin@mail.com"
    }
  }
}
```

#### 失败情况

| 场景 | code | message |
|---|---|---|
| 用户不存在 | 40001 | 用户名或密码错误 |
| 密码错误 | 40001 | 用户名或密码错误 |

说明：第一阶段可以使用简单 token 方案；如果后续使用 JWT 或 Redis token，不影响前端接口格式。

---

### 6.3 退出登录 P0，可选后端实现

```http
POST /api/auth/logout
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": true
}
```

说明：当前版本如果使用简单 token 或 JWT，退出登录也可以由前端删除本地 token 完成；该接口保留为可选后端实现。

---

## 7. 用户模块

### 7.1 获取当前登录用户信息 P0

```http
GET /api/users/me
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "username": "admin",
    "nickname": "管理员",
    "emailAddress": "admin@mail.com",
    "avatarText": "管"
  }
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名 |
| nickname | string | 是 | 昵称 |
| emailAddress | string | 是 | 系统内邮箱地址 |
| avatarText | string | 是 | 前端头像占位文字，例如“管”“张”“李” |

---

### 7.2 修改密码 P2，可选实现

```http
PUT /api/users/password
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码 |
| confirmPassword | string | 是 | 确认新密码 |

请求示例：

```json
{
  "oldPassword": "123456",
  "newPassword": "new123456",
  "confirmPassword": "new123456"
}
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": true
}
```

#### 失败情况

| 场景 | code | message |
|---|---|---|
| 原密码为空 | 40000 | 原密码不能为空 |
| 原密码错误 | 40000 | 原密码错误 |
| 新密码为空 | 40000 | 新密码不能为空 |
| 确认密码为空 | 40000 | 确认密码不能为空 |
| 两次密码不一致 | 40000 | 两次输入的新密码不一致 |
| 新密码长度不合法 | 40000 | 新密码长度不合法 |
| 新密码与原密码相同 | 40000 | 新密码不能与原密码相同 |

说明：用户菜单可预留“修改密码”入口；该接口为 P2 可选实现。

---

## 8. 用户设置模块

说明：

- AI 配置统一归入用户设置模块，不单独新增 `/api/users/ai-config`。
- 当前版本只适配 OpenAI Compatible 格式的大模型服务。
- AI 能力只作为增强能力。
- 当 API Key 未配置、模型不可用、额度不足、网络异常或调用超时时，系统应自动降级为本地机器学习和规则检测，不能影响邮件发送、接收、查看详情和删除等主流程。

### 8.1 获取用户设置 P1

```http
GET /api/users/settings
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "aiEnabled": true,
    "autoReplyEnabled": true,
    "prioritySortEnabled": true,
    "modelConfigured": true,
    "provider": "deepseek",
    "modelName": "deepseek-chat",
    "baseUrl": "https://api.deepseek.com",
    "apiKeyConfigured": true,
    "maskedApiKey": "sk-****abcd",
    "timeoutMs": 10000,
    "maxTokens": 800,
    "temperature": 0.2
  }
}
```

未配置模型时推荐返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "aiEnabled": false,
    "autoReplyEnabled": false,
    "prioritySortEnabled": true,
    "modelConfigured": false,
    "provider": null,
    "modelName": null,
    "baseUrl": null,
    "apiKeyConfigured": false,
    "maskedApiKey": null,
    "timeoutMs": 10000,
    "maxTokens": 800,
    "temperature": 0.2
  }
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| aiEnabled | boolean | 是 | 是否启用 AI 分析 |
| autoReplyEnabled | boolean | 是 | 是否启用自动回复建议 |
| prioritySortEnabled | boolean | 是 | 是否启用邮件优先级排序 |
| modelConfigured | boolean | 是 | 后端是否已完成模型配置 |
| provider | string/null | 是 | 当前模型服务商 |
| modelName | string/null | 是 | 当前模型名称 |
| baseUrl | string/null | 是 | 当前模型接口地址 |
| apiKeyConfigured | boolean | 是 | 是否已保存 API Key |
| maskedApiKey | string/null | 是 | 脱敏后的 API Key |
| timeoutMs | integer | 是 | 单次 AI 请求超时时间，默认 10000，单位毫秒 |
| maxTokens | integer | 是 | 模型最大输出长度，默认 800 |
| temperature | number | 是 | 模型温度，默认 0.2 |

重要说明：

- 后端不得在 GET `/api/users/settings` 中返回完整 API Key。
- 前端只能展示脱敏结果，例如 `sk-****abcd`。
- `modelConfigured = true` 表示 `provider`、`baseUrl`、`modelName` 均已配置且 `apiKeyConfigured = true`。

---

### 8.2 更新用户设置 P1

```http
PUT /api/users/settings
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 请求体字段

该接口使用一个统一请求对象，所有字段均为非必填。配置模型时部分字段有条件必填规则。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| aiEnabled | boolean | 否 | 是否启用 AI 功能 |
| autoReplyEnabled | boolean | 否 | 是否启用回复建议 |
| prioritySortEnabled | boolean | 否 | 是否启用优先级排序 |
| provider | string | 配置模型时必填 | 模型服务商 |
| apiKey | string | 首次配置模型时必填 | 新的 API Key；不传表示不修改旧 Key |
| baseUrl | string | 配置模型时必填 | OpenAI 兼容接口地址 |
| modelName | string | 配置模型时必填 | 模型名称 |
| timeoutMs | integer | 否 | 超时时间，默认 10000 |
| maxTokens | integer | 否 | 最大输出长度，默认 800 |
| temperature | number | 否 | 温度参数，默认 0.2 |

只更新开关示例：

```json
{
  "aiEnabled": true,
  "autoReplyEnabled": true,
  "prioritySortEnabled": true
}
```

配置 OpenAI 兼容模型示例：

```json
{
  "aiEnabled": true,
  "autoReplyEnabled": true,
  "prioritySortEnabled": true,
  "provider": "deepseek",
  "apiKey": "sk-xxxxxxxxxxxxxxxx",
  "baseUrl": "https://api.deepseek.com",
  "modelName": "deepseek-chat",
  "timeoutMs": 10000,
  "maxTokens": 800,
  "temperature": 0.2
}
```

#### apiKey 更新规则

| 请求情况 | 后端处理 |
|---|---|
| apiKey 不传 | 不修改已保存 API Key |
| apiKey 为非空字符串 | 加密保存新的 API Key，并更新 maskedApiKey |
| apiKey 为空字符串 `""` | 清空已保存 API Key，modelConfigured 变为 false |

说明：前端未修改 API Key 时不要提交 `apiKey` 字段；只有用户主动清空密钥时才提交 `apiKey = ""`。

#### 校验规则

| 字段 | 校验规则 |
|---|---|
| provider | 必须是 qwen、openai、deepseek、kimi、glm、siliconflow、custom 之一 |
| baseUrl | 必须以 http:// 或 https:// 开头；正式环境建议只允许 https:// |
| modelName | 不能为空 |
| timeoutMs | 建议范围 3000 至 30000 |
| maxTokens | 建议范围 100 至 4000 |
| temperature | 建议范围 0 至 1 |

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "aiEnabled": true,
    "autoReplyEnabled": true,
    "prioritySortEnabled": true,
    "modelConfigured": true,
    "provider": "deepseek",
    "modelName": "deepseek-chat",
    "baseUrl": "https://api.deepseek.com",
    "apiKeyConfigured": true,
    "maskedApiKey": "sk-****abcd",
    "timeoutMs": 10000,
    "maxTokens": 800,
    "temperature": 0.2
  }
}
```

#### 失败情况

| 场景 | code | message |
|---|---|---|
| provider 不支持 | 40000 | 不支持的 AI 服务商 |
| baseUrl 为空 | 40000 | Base URL 不能为空 |
| baseUrl 格式不合法 | 40000 | Base URL 格式不合法 |
| modelName 为空 | 40000 | 模型名称不能为空 |
| 首次配置时 apiKey 为空 | 40000 | API Key 不能为空 |
| temperature 超出范围 | 40000 | temperature 参数不合法 |
| maxTokens 超出范围 | 40000 | maxTokens 参数不合法 |
| timeoutMs 超出范围 | 40000 | timeoutMs 参数不合法 |

---

## 9. 邮件发送模块

### 9.1 发送邮件 P0

```http
POST /api/mails
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| recipientUsername | string | 是 | 收件人用户名 |
| subject | string | 是 | 邮件主题 |
| content | RichTextNode[] | 是 | 邮件正文，富文本数组 |

请求示例：

```json
{
  "recipientUsername": "zhangsan",
  "subject": "实验报告提交提醒",
  "content": [
    {
      "type": "paragraph",
      "children": [
        {
          "type": "text",
          "text": "您好，请注意本周五之前提交实验报告。"
        }
      ]
    },
    {
      "type": "paragraph",
      "children": [
        {
          "type": "text",
          "text": "报告内容需要包含实验目的、实验步骤、实验结果分析以及结论。"
        }
      ]
    }
  ]
}
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "mailId": 1001,
    "subject": "实验报告提交提醒",
    "sender": {
      "username": "admin",
      "nickname": "管理员"
    },
    "recipient": {
      "username": "zhangsan",
      "nickname": "张三"
    },
    "sentAt": "2026-05-25T16:04:00",
    "analysisStatus": "PENDING"
  }
}
```

#### 失败情况

| 场景 | code | message |
|---|---|---|
| 未登录 | 40002 | 请先登录 |
| 收件人不存在 | 40006 | 收件人不存在 |
| 主题为空 | 40007 | 邮件主题不能为空 |
| 正文为空 | 40008 | 邮件正文不能为空 |

说明：

- 发送成功后，后端至少需要创建邮件主体记录和收件关系记录。
- AI 分析可以同步执行，也可以异步执行。
- AI 失败不影响邮件发送成功。

---

## 10. 邮件列表模块

四个列表接口统一返回分页结构，`records` 使用同一个 `MailListItemVO`。

---

### 10.1 查询收件箱列表 P0 / P1

```http
GET /api/mails/inbox
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 查询参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | integer | 否 | 页码，默认 1 |
| size | integer | 否 | 每页数量，默认 10 |
| keyword | string | 否 | 搜索主题、正文、发件人用户名或昵称 |
| readStatus | string | 否 | ALL / READ / UNREAD |
| senderUsername | string | 否 | 发件人用户名 |
| priority | string | 否 | LOW / MEDIUM / HIGH |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |

示例：

```http
GET /api/mails/inbox?keyword=实验&readStatus=UNREAD&priority=HIGH&page=1&size=10
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 4,
    "totalPages": 1,
    "records": [
      {
        "mailId": 1001,
        "subject": "实验报告提交提醒",
        "snippet": "实验报告需在本周五前提交，包含目的、步骤、结果分析和结论",
        "sender": {
          "username": "zhangsan",
          "nickname": "张三"
        },
        "recipient": {
          "username": "admin",
          "nickname": "管理员"
        },
        "sentAt": "2026-05-25T16:04:00",
        "read": false,
        "priority": "HIGH",
        "priorityLabel": "高优先级",
        "spam": false,
        "spamLevel": "NONE",
        "riskLevel": "SAFE",
        "riskLabel": "安全",
        "riskReason": null,
        "analysisStatus": "SUCCESS"
      }
    ]
  }
}
```

#### 业务规则

- 只查询当前登录用户收到的邮件。
- 不返回当前用户已逻辑删除的邮件。
- 不返回已进入垃圾邮箱的邮件。
- 默认按发送时间倒序排序。
- 如果用户开启优先级排序，可以优先按优先级排序，再按发送时间排序。

---

### 10.2 查询已发送列表 P0 / P1

```http
GET /api/mails/sent
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 查询参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | integer | 否 | 页码，默认 1 |
| size | integer | 否 | 每页数量，默认 10 |
| keyword | string | 否 | 搜索主题、正文、收件人用户名或昵称 |
| recipientUsername | string | 否 | 收件人用户名 |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |

示例：

```http
GET /api/mails/sent?keyword=项目&recipientUsername=lisi&page=1&size=10
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 1,
    "totalPages": 1,
    "records": [
      {
        "mailId": 1002,
        "subject": "项目进度更新",
        "snippet": "项目进度更新：前端80%，后端60%，预计下周完成集成测试",
        "sender": {
          "username": "admin",
          "nickname": "管理员"
        },
        "recipient": {
          "username": "lisi",
          "nickname": "李四"
        },
        "sentAt": "2026-05-25T14:20:00",
        "read": null,
        "priority": "MEDIUM",
        "priorityLabel": "中优先级",
        "spam": false,
        "spamLevel": "NONE",
        "riskLevel": "SAFE",
        "riskLabel": "安全",
        "riskReason": null,
        "analysisStatus": "SUCCESS"
      }
    ]
  }
}
```

#### 业务规则

- 只查询当前登录用户发送过的邮件。
- 已发送列表不受收件人删除状态影响。
- 当前版本只支持单收件人，因此每封邮件只有一个 recipient。
- 已发送列表默认按发送时间倒序排序。
- `read` 对发件人通常没有意义，可以返回 `null`。

---

### 10.3 查询已删除列表 P1

```http
GET /api/mails/trash
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 查询参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | integer | 否 | 页码，默认 1 |
| size | integer | 否 | 每页数量，默认 10 |
| keyword | string | 否 | 搜索主题、正文、发件人用户名或昵称 |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |

示例：

```http
GET /api/mails/trash?keyword=系统&page=1&size=10
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 1,
    "totalPages": 1,
    "records": [
      {
        "mailId": 1003,
        "subject": "系统维护通知",
        "snippet": "系统将于本周六凌晨2:00-6:00进行维护升级，届时服务将暂时不可用",
        "sender": {
          "username": "lisi",
          "nickname": "李四"
        },
        "recipient": {
          "username": "admin",
          "nickname": "管理员"
        },
        "sentAt": "2026-05-23T09:00:00",
        "deletedAt": "2026-05-24T10:00:00",
        "read": true,
        "priority": "LOW",
        "priorityLabel": "低优先级",
        "spam": false,
        "spamLevel": "NONE",
        "riskLevel": "SAFE",
        "riskLabel": "安全",
        "riskReason": null,
        "analysisStatus": "SUCCESS"
      }
    ]
  }
}
```

#### 业务规则

- 当前版本的删除是收件人侧逻辑删除。
- 已删除列表只展示当前登录用户删除过的收件邮件。
- 当前版本不强制实现恢复邮件和彻底删除。

---

### 10.4 查询垃圾邮箱列表 P1

```http
GET /api/mails/spam
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 查询参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | integer | 否 | 页码，默认 1 |
| size | integer | 否 | 每页数量，默认 10 |
| keyword | string | 否 | 搜索主题、正文、发件人用户名或昵称 |
| spamLevel | string | 否 | NONE / LOW / MEDIUM / HIGH |
| riskLevel | string | 否 | SAFE / LOW / MEDIUM / HIGH |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |

说明：为保持枚举统一，垃圾邮箱查询参数 `spamLevel` 允许 `NONE`，`riskLevel` 允许 `SAFE`。业务上通常主要使用 `LOW / MEDIUM / HIGH`。

示例：

```http
GET /api/mails/spam?keyword=中奖&riskLevel=HIGH&page=1&size=10
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "page": 1,
    "size": 10,
    "total": 2,
    "totalPages": 1,
    "records": [
      {
        "mailId": 1004,
        "subject": "恭喜您中奖100万！点击领取",
        "snippet": "检测到诱导性中奖信息和可疑链接",
        "sender": {
          "username": "wangwu",
          "nickname": "王五"
        },
        "recipient": {
          "username": "admin",
          "nickname": "管理员"
        },
        "sentAt": "2026-05-25T11:34:00",
        "read": true,
        "priority": "LOW",
        "priorityLabel": "低优先级",
        "spam": true,
        "spamLevel": "HIGH",
        "spamLevelLabel": "高危垃圾",
        "riskLevel": "HIGH",
        "riskLabel": "高风险",
        "riskReason": "检测到诱导性中奖信息和可疑链接",
        "analysisStatus": "SUCCESS"
      }
    ]
  }
}
```

#### 业务规则

- 只查询当前用户收到的垃圾或高风险邮件。
- 高风险邮件不出现在普通收件箱中。
- 垃圾邮箱必须展示风险等级、垃圾等级和风险原因。
- 当前版本不强制支持“移出垃圾邮箱”。

---

## 11. 邮件详情模块

### 11.1 查询邮件详情 P0 / P1

```http
GET /api/mails/{mailId}
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mailId | integer | 是 | 邮件 ID |

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "mailId": 1001,
    "subject": "实验报告提交提醒",
    "content": [
      {
        "type": "paragraph",
        "children": [
          {
            "type": "text",
            "text": "普通文本"
          },
          {
            "type": "text",
            "text": "加粗文本",
            "bold": true
          },
          {
            "type": "text",
            "text": "斜体文本",
            "italic": true
          },
          {
            "type": "text",
            "text": "下划线文本",
            "underline": true
          }
        ]
      },
      {
        "type": "paragraph",
        "children": [
          {
            "type": "link",
            "text": "查看详情",
            "href": "https://example.com"
          }
        ]
      },
      {
        "type": "image",
        "resourceId": "res_123"
      }
    ],
    "sender": {
      "username": "zhangsan",
      "nickname": "张三"
    },
    "recipient": {
      "username": "admin",
      "nickname": "管理员"
    },
    "sentAt": "2026-05-25T16:04:00",
    "currentUserRole": "RECIPIENT",
    "read": true,
    "deleted": false,
    "spam": false,
    "analysis": {
      "analysisStatus": "SUCCESS",
      "summary": "实验报告需在本周五前提交，内容包括实验目的、步骤、结果分析和结论。",
      "spamLevel": "NONE",
      "spamLevelLabel": "非垃圾邮件",
      "riskLevel": "SAFE",
      "riskLabel": "安全",
      "priority": "HIGH",
      "priorityLabel": "高优先级",
      "priorityReason": "包含截止日期提醒",
      "spamReason": "未发现中奖、广告、诱导点击或异常链接等垃圾邮件特征。",
      "riskReason": null,
      "replySuggestions": [
        "好的，我会按时提交。",
        "收到，有问题会联系您。"
      ]
    }
  }
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mailId | integer | 是 | 邮件 ID |
| subject | string | 是 | 邮件主题 |
| content | RichTextNode[] | 是 | 邮件正文，富文本数组 |
| sender | UserBrief | 是 | 发件人 |
| recipient | UserBrief | 是 | 收件人 |
| sentAt | string | 是 | 发送时间 |
| currentUserRole | string | 是 | 当前用户角色：SENDER / RECIPIENT |
| read | boolean | 是 | 当前用户是否已读 |
| deleted | boolean | 是 | 当前用户侧是否已删除 |
| spam | boolean | 是 | 是否垃圾邮件 |
| analysis | MailAnalysisVO | 是 | 邮件智能分析结果 |

#### 无权限响应

```json
{
  "code": 40003,
  "message": "无权限查看该邮件",
  "data": null
}
```

#### 权限规则

| 当前用户身份 | 是否可查看 |
|---|---|
| 发件人 | 可以查看 |
| 收件人 | 可以查看 |
| 其他用户 | 不允许查看 |

#### 自动已读规则

如果当前用户是收件人，且该邮件原本未读，则调用详情接口后自动标记为已读。

也就是说：

```http
GET /api/mails/{mailId}
```

对收件人具有“查看详情 + 自动已读”的效果。

---

## 12. 邮件状态模块

### 12.1 标记邮件为已读 P0

```http
PATCH /api/mails/{mailId}/read
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mailId | integer | 是 | 邮件 ID |

#### 请求体

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| read | boolean | 是 | 固定传 true；该接口只用于标记已读 |

请求示例：

```json
{
  "read": true
}
```

说明：当前接口只允许 `read = true`，不提供标记未读接口。

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "mailId": 1001,
    "read": true
  }
}
```

#### 业务规则

- 只有收件人可以修改自己的已读状态。
- 发件人不能修改收件人的已读状态。
- 当前版本不提供 `/api/mails/{mailId}/unread`。

---

### 12.2 逻辑删除邮件 P0

```http
DELETE /api/mails/{mailId}
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mailId | integer | 是 | 邮件 ID |

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "mailId": 1001,
    "deleted": true,
    "deletedAt": "2026-05-26T10:30:00"
  }
}
```

#### 业务规则

- 当前版本删除只针对收件人侧状态。
- 删除后，该邮件不再出现在普通收件箱或垃圾邮箱中。
- 删除后，该邮件出现在已删除列表。
- 不物理删除邮件主体记录。
- 发件人删除已发送邮件不是当前版本必做能力。

---

### 12.3 恢复已删除邮件 P2，可选实现

```http
PATCH /api/mails/{mailId}/restore
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mailId | integer | 是 | 邮件 ID |

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "mailId": 1001,
    "deleted": false
  }
}
```

说明：当前版本不强制实现恢复邮件和彻底删除；该接口为 P2 可选实现。

---

## 13. 侧边栏统计模块

### 13.1 获取邮箱统计数量 P1

```http
GET /api/mails/statistics
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "inboxTotal": 4,
    "inboxUnread": 2,
    "sentTotal": 0,
    "trashTotal": 1,
    "spamTotal": 2
  }
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| inboxTotal | integer | 是 | 收件箱正常邮件总数 |
| inboxUnread | integer | 是 | 收件箱未读数量 |
| sentTotal | integer | 是 | 已发送邮件数量 |
| trashTotal | integer | 是 | 已删除邮件数量 |
| spamTotal | integer | 是 | 垃圾邮箱邮件数量 |

说明：如果不做该接口，前端也可以在进入每个列表页时分别取 total，但侧边栏数量会不方便统一刷新。因此建议实现。

---

## 14. AI 分析模块

### 14.1 当前版本推荐策略

当前版本不强制提供独立 AI 接口。推荐做法是：

1. 邮件发送成功后，后端触发 AI 分析。
2. 分析结果保存到 `mail_analysis` 或类似表中。
3. 邮件列表接口返回部分分析字段。
4. 邮件详情接口返回完整分析字段。

AI 能力只作为增强能力。基础垃圾邮件识别仍采用“规则检测 + 机器学习分类 + 可选大模型增强分析”。如果用户关闭 AI、模型未配置、模型调用失败或返回解析失败，系统必须降级为规则和机器学习结果，不能影响邮件发送、收件、查看详情和删除等主流程。

### 14.2 AI 分析触发流程

1. 用户发送邮件：`POST /api/mails`
2. 后端保存邮件主体 `mail`
3. 后端保存收件关系 `mail_recipient`
4. 后端执行规则检测
5. 后端执行机器学习垃圾邮件分类
6. 后端读取用户设置 `user_settings`
7. 如果 `aiEnabled = true` 且 `modelConfigured = true`，则调用 OpenAI 兼容模型
8. 后端融合规则、机器学习、大模型结果
9. 后端写入 `mail_analysis`
10. 列表页和详情页返回 AI 分析字段

### 14.3 列表页返回的 AI 字段

适用接口：

```http
GET /api/mails/inbox
GET /api/mails/sent
GET /api/mails/trash
GET /api/mails/spam
```

列表页返回用于展示标签、排序和过滤的字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| priority | string | 是 | 邮件优先级：LOW / MEDIUM / HIGH |
| priorityLabel | string | 是 | 优先级展示文本 |
| spam | boolean | 是 | 是否为垃圾邮件 |
| spamLevel | string | 是 | 垃圾风险等级：NONE / LOW / MEDIUM / HIGH |
| riskLevel | string | 是 | 安全风险等级：SAFE / LOW / MEDIUM / HIGH |
| riskLabel | string | 是 | 安全风险展示文本 |
| riskReason | string/null | 否 | 风险原因，列表页可为空 |
| analysisStatus | string | 是 | 分析状态：NOT_STARTED / PENDING / SUCCESS / FAILED / DISABLED |

### 14.4 详情页返回的 AI 字段

适用接口：

```http
GET /api/mails/{mailId}
```

详情页返回完整 `analysis` 信息，结构见 `MailAnalysisVO`。

说明：当前最终版不纳入以下建议补充字段：

```text
aiProvider
modelName
priorityScore
spamScore
riskScore
```

### 14.5 重新分析邮件 P2，可选实现

```http
POST /api/mails/{mailId}/analysis/retry
```

#### 请求头

```http
Authorization: Bearer <token>
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mailId | integer | 是 | 邮件 ID |

#### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "mailId": 1001,
    "analysisStatus": "PENDING"
  }
}
```

#### 业务规则

1. 当前用户必须有权限查看该邮件。
2. 重新分析不修改邮件正文和收件关系，只更新 `mail_analysis`。
3. 如果 AI 不可用，应降级为规则和机器学习分析结果。
4. 当前阶段不建议优先做该接口，时间不足时只做发送邮件后的自动分析。

### 14.6 OpenAI 兼容模型调用格式

后端调用用户自定义模型时，统一按 OpenAI Chat Completions 格式组织请求。

请求地址：

```http
POST {baseUrl}/chat/completions
```

后端拼接请求地址时应统一去除 `baseUrl` 末尾的 `/`，再追加 `/chat/completions`，避免出现双斜杠路径。

请求体示例：

```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "system",
      "content": "你是一个邮件安全分析助手，需要判断邮件是否为垃圾邮件、是否存在安全风险，并输出结构化 JSON。"
    },
    {
      "role": "user",
      "content": "请分析以下邮件：主题：实验报告提交提醒。正文：您好，请注意本周五之前提交实验报告。"
    }
  ],
  "temperature": 0.2,
  "max_tokens": 800
}
```

推荐模型返回 JSON：

```json
{
  "spam": false,
  "spamLevel": "NONE",
  "spamReason": "未发现明显垃圾邮件特征。",
  "riskLevel": "SAFE",
  "riskReason": "未发现可疑链接或诱导行为。",
  "priority": "HIGH",
  "priorityReason": "包含明确截止日期和任务要求。",
  "summary": "该邮件提醒用户在本周五前提交实验报告。",
  "replySuggestions": [
    "收到，我会按时提交。",
    "好的，我会尽快完成。"
  ]
}
```

说明：当前最终版接口层不返回 `spamScore`、`riskScore`、`priorityScore`。

### 14.7 分析结果融合策略

垃圾邮件识别建议保留三层结构：

```text
规则检测 + 机器学习分类 + 大模型增强分析
```

推荐权重：

```text
finalSpamScore = ruleScore * 0.4 + mlScore * 0.4 + aiScore * 0.2
```

如果用户关闭 AI 或模型不可用：

```text
finalSpamScore = ruleScore * 0.5 + mlScore * 0.5
```

也可以采用更简单的课程项目规则：

- 命中高危钓鱼链接、伪造登录链接、中奖诱导等规则时直接提高风险等级。
- 机器学习模型给出垃圾邮件概率。
- 大模型只负责复杂语义判断、原因解释、摘要和回复建议。
- 大模型失败时只保存规则和机器学习结果。

---

## 15. 搜索与过滤规则

### 15.1 搜索原则

当前版本不单独提供全局搜索接口。

搜索和过滤统一挂在各列表接口上：

```http
GET /api/mails/inbox
GET /api/mails/sent
GET /api/mails/trash
GET /api/mails/spam
```

### 15.2 各页面支持的查询条件

| 页面 | 支持参数 |
|---|---|
| 收件箱 | keyword、readStatus、senderUsername、priority、startTime、endTime、page、size |
| 已发送 | keyword、recipientUsername、startTime、endTime、page、size |
| 已删除 | keyword、startTime、endTime、page、size |
| 垃圾邮箱 | keyword、spamLevel、riskLevel、startTime、endTime、page、size |

### 15.3 keyword 搜索范围

| 页面 | keyword 搜索范围 |
|---|---|
| 收件箱 | 主题、正文、发件人用户名、发件人昵称 |
| 已发送 | 主题、正文、收件人用户名、收件人昵称 |
| 已删除 | 主题、正文、发件人用户名、发件人昵称 |
| 垃圾邮箱 | 主题、正文、发件人用户名、发件人昵称、风险原因 |

### 15.4 时间范围规则

`startTime` 和 `endTime` 按发送时间过滤。

示例：

```http
GET /api/mails/inbox?startTime=2026-05-01T00:00:00&endTime=2026-05-31T23:59:59
```

### 15.5 URL 状态同步

前端应将搜索与过滤条件同步到 URL 查询参数，例如：

```text
/mails/inbox?keyword=实验&readStatus=UNREAD&priority=HIGH&page=1&size=10
```

后端只负责接收查询参数并返回结果。

---

## 16. 页面与接口对应关系

| 前端页面 | 路由建议 | 需要调用的接口 |
|---|---|---|
| 登录 / 注册页 | /auth | POST /api/auth/login、POST /api/auth/register |
| 主布局侧边栏 | / | GET /api/users/me、GET /api/mails/statistics |
| 收件箱页 | /mails/inbox | GET /api/mails/inbox |
| 已发送页 | /mails/sent | GET /api/mails/sent |
| 已删除页 | /mails/trash | GET /api/mails/trash |
| 垃圾邮箱页 | /mails/spam | GET /api/mails/spam |
| 写邮件页 | /mails/compose | POST /api/mails |
| 邮件详情页 | /mails/:mailId | GET /api/mails/{mailId}、DELETE /api/mails/{mailId} |
| 设置页 | /settings | GET /api/users/settings、PUT /api/users/settings |
| 修改密码弹窗 | 无固定路由 | PUT /api/users/password |

说明：当前最终版不纳入 `/api/users/search`，写邮件时直接输入 `recipientUsername`。

---

## 17. 推荐数据库实体对应关系

接口层不直接暴露数据库表结构，但后端设计建议至少包含以下实体。

### 17.1 用户表 user

核心字段：

```text
id
username
password_hash
nickname
email_address
created_at
updated_at
```

说明：数据库可以有 `id`，但当前接口响应严格不返回 `userId`。

### 17.2 用户设置表 user_settings

核心字段：

```text
id
user_id
ai_enabled
auto_reply_enabled
priority_sort_enabled
provider
base_url
model_name
api_key_encrypted
api_key_mask
timeout_ms
max_tokens
temperature
created_at
updated_at
```

说明：

- `api_key_encrypted` 必须加密保存。
- GET `/api/users/settings` 只能返回 `api_key_mask` 对应的 `maskedApiKey`。
- 不得返回完整 API Key。

### 17.3 邮件主体表 mail

核心字段：

```text
id
sender_id
subject
content
sent_at
created_at
updated_at
```

说明：`content` 存储富文本数组 JSON。

### 17.4 邮件收件关系表 mail_recipient

核心字段：

```text
id
mail_id
recipient_id
read_flag
read_at
deleted_flag
deleted_at
spam_flag
spam_level
risk_level
created_at
updated_at
```

### 17.5 邮件分析结果表 mail_analysis

核心字段：

```text
id
mail_id
recipient_id
analysis_status
priority
priority_reason
spam_flag
spam_level
spam_reason
risk_level
risk_reason
summary
reply_suggestions
ai_error_message
created_at
updated_at
```

说明：

- `reply_suggestions` 可以用 JSON 字符串保存，也可以单独拆表。
- 当前接口层返回 `replySuggestions: string[]`。
- 当前最终版接口层不返回 `aiProvider`、`modelName`、`priorityScore`、`spamScore`、`riskScore`。

---

## 18. Apifox 推荐测试流程

### 18.1 第一轮：P0 闭环测试

1. 注册用户 A：`POST /api/auth/register`
2. 注册用户 B：`POST /api/auth/register`
3. 用户 A 登录：`POST /api/auth/login`
4. 用户 A 发送邮件给用户 B：`POST /api/mails`
5. 用户 A 查看已发送：`GET /api/mails/sent`
6. 用户 B 登录：`POST /api/auth/login`
7. 用户 B 查看收件箱：`GET /api/mails/inbox`
8. 用户 B 查看邮件详情：`GET /api/mails/{mailId}`
9. 用户 B 删除邮件：`DELETE /api/mails/{mailId}`
10. 用户 B 查看已删除：`GET /api/mails/trash`

### 18.2 第二轮：P1 功能测试

1. 测试收件箱关键词搜索。
2. 测试收件箱未读过滤。
3. 测试已发送按收件人搜索。
4. 测试垃圾邮箱风险等级过滤。
5. 测试设置页 AI 开关更新。
6. 测试侧边栏统计数量。

---

## 19. 当前版本不做的接口和能力

以下接口或能力当前版本不纳入：

| 功能 | 原因 |
|---|---|
| SMTP / POP3 / IMAP 接入 | 超出站内邮件系统范围 |
| 附件上传 | 增加文件存储和预览复杂度 |
| 多收件人 / CC / BCC | 当前版本只支持单收件人 |
| 草稿箱 | 页面设计未体现，非主流程 |
| 邮件撤回 | 权限和状态复杂 |
| 邮件群发 | 非课程项目核心 |
| WebSocket 实时通知 | 演示收益不高，增加联调复杂度 |
| 全文搜索引擎 | 当前 SQL LIKE 足够 |
| 语义搜索 | 非当前版本核心 |
| 管理员后台 | 当前版本不引入管理员角色 |
| 用户搜索接口 `/api/users/search` | 当前最终版不纳入，写邮件直接输入 recipientUsername |
| 测试 AI 连接接口 | 当前版本保存配置后在分析阶段调用模型，不单独提供 POST /api/users/settings/ai-test |
| AI 服务商列表接口 | 前端可写死 provider 预设，不单独提供 GET /api/ai/providers |
| 流式输出 | 当前版本只返回结构化分析结果 |
| AI thinking 展示 | 当前版本不展示模型推理过程 |
| Claude 原生协议适配 | 当前版本只适配 OpenAI Compatible 格式 |
| 多模型配置 | 当前版本每个用户只保留一套模型配置 |
| 标记未读接口 `/api/mails/{mailId}/unread` | 当前版本不提供，read 接口只标记已读 |

---

## 20. 最终接口清单

| 模块 | 方法 | 接口 | 优先级 | 说明 |
|---|---|---|---|---|
| 认证 | POST | /api/auth/register | P0 | 注册 |
| 认证 | POST | /api/auth/login | P0 | 登录 |
| 认证 | POST | /api/auth/logout | P0 | 退出登录，可选后端实现 |
| 用户 | GET | /api/users/me | P0 | 当前用户信息 |
| 用户 | PUT | /api/users/password | P2 | 修改密码，可选实现 |
| 设置 | GET | /api/users/settings | P1 | 获取用户设置和 AI 配置状态 |
| 设置 | PUT | /api/users/settings | P1 | 更新用户设置和 AI 配置 |
| 邮件 | POST | /api/mails | P0 | 发送邮件 |
| 邮件 | GET | /api/mails/inbox | P0/P1 | 收件箱列表 |
| 邮件 | GET | /api/mails/sent | P0/P1 | 已发送列表 |
| 邮件 | GET | /api/mails/trash | P1 | 已删除列表 |
| 邮件 | GET | /api/mails/spam | P1 | 垃圾邮箱列表 |
| 邮件 | GET | /api/mails/{mailId} | P0/P1 | 邮件详情 |
| 邮件状态 | PATCH | /api/mails/{mailId}/read | P0 | 标记已读 |
| 邮件状态 | DELETE | /api/mails/{mailId} | P0 | 逻辑删除 |
| 邮件状态 | PATCH | /api/mails/{mailId}/restore | P2 | 恢复邮件，可选实现 |
| 统计 | GET | /api/mails/statistics | P1 | 侧边栏统计 |
| AI | POST | /api/mails/{mailId}/analysis/retry | P2 | 重新分析邮件，可选实现 |
