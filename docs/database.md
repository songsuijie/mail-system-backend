# 数据库设计说明

## 1. 文档定位

本文档根据 `docs/api.md` 定义第一版邮件系统后端数据库结构，是编写 `sql/schema.sql`、Entity、Mapper、Service 和接口实现的依据。

接口契约以 `docs/api.md` 为最高优先级。若字段命名、响应结构或业务规则存在冲突，以 `docs/api.md` 为准。

## 2. 设计目标

数据库需要支持以下接口能力：

- 用户注册、登录、当前用户信息。
- 用户设置和 AI 配置状态。
- 发送站内邮件。
- 收件箱、已发送、已删除、垃圾邮箱列表。
- 邮件详情、自动已读、显式标记已读。
- 收件人侧逻辑删除。
- 邮箱统计数量。
- 搜索、过滤、优先级、垃圾等级、风险等级。
- 邮件分析结果展示和后续重新分析。

当前版本不接入真实 SMTP、POP3、IMAP，不设计附件表、多收件人详情表、草稿表或复杂文件夹表。

## 3. 表结构总览

`docs/api.md` 第 17 节使用 `user` 和 `mail` 作为实体名称。为了避免和 MySQL 系统用户概念混淆，并延续当前项目命名，实际建表使用以下表名：

| API 实体 | 实际表名 | 说明 |
| --- | --- | --- |
| user | `sys_user` | 用户账号和基础资料 |
| user_settings | `user_settings` | 用户设置和 AI 模型配置 |
| mail | `mail_message` | 邮件主体内容 |
| mail_recipient | `mail_recipient` | 收件人个人状态 |
| mail_analysis | `mail_analysis` | 规则、机器学习或 AI 分析结果 |

## 4. 表关系

```text
sys_user 1 ---- 1 user_settings

sys_user 1 ---- N mail_message

mail_message 1 ---- N mail_recipient

sys_user 1 ---- N mail_recipient

mail_recipient 1 ---- 0/1 mail_analysis
```

第一版只发送给一个普通收件人，但仍保留 `mail_recipient` 关系表，方便后续扩展多收件人、CC 和 BCC。

## 5. 核心表设计

### 5.1 `sys_user`

用途：

保存用户登录账号和基础展示信息。

核心字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 用户 ID，作为内部关联字段，当前接口响应不直接返回 |
| `username` | `VARCHAR(32)` | 用户名，全局唯一 |
| `password_hash` | `VARCHAR(255)` | 密码哈希，不保存明文密码 |
| `nickname` | `VARCHAR(64)` | 昵称，不传时默认等于 username |
| `email_address` | `VARCHAR(128)` | 系统内邮箱地址或展示邮箱 |
| `status` | `TINYINT` | 用户状态，`1` 正常，`0` 禁用 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |
| `deleted` | `TINYINT` | 用户逻辑删除状态 |

业务规则：

- 注册时校验 `username` 唯一。
- 登录时只允许 `status = 1` 且 `deleted = 0` 的用户登录。
- API 返回字段使用 `username`、`nickname`、`emailAddress`、`avatarText`，当前最终版不返回 `userId`。

### 5.2 `user_settings`

用途：

保存用户级设置和 AI 模型配置。

核心字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `user_id` | `BIGINT` | 用户 ID |
| `ai_enabled` | `TINYINT` | 是否启用 AI 分析 |
| `auto_reply_enabled` | `TINYINT` | 是否启用回复建议 |
| `priority_sort_enabled` | `TINYINT` | 是否启用优先级排序 |
| `provider` | `VARCHAR(32)` | 模型服务商 |
| `base_url` | `VARCHAR(255)` | OpenAI Compatible 接口地址 |
| `model_name` | `VARCHAR(128)` | 模型名称 |
| `api_key_encrypted` | `VARCHAR(1024)` | 加密后的 API Key |
| `api_key_mask` | `VARCHAR(64)` | 脱敏后的 API Key |
| `timeout_ms` | `INT` | 单次请求超时时间 |
| `max_tokens` | `INT` | 最大输出长度 |
| `temperature` | `DECIMAL(3,2)` | 模型温度 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

业务规则：

- `GET /api/users/settings` 不能返回完整 API Key。
- `apiKey` 不传表示不修改；传空字符串表示清空。
- `modelConfigured` 可由 `provider`、`base_url`、`model_name` 和 `api_key_encrypted` 是否完整推导。

### 5.3 `mail_message`

用途：

保存邮件主体内容。

核心字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 邮件 ID，对应 API 的 `mailId` |
| `sender_id` | `BIGINT` | 发件人用户 ID |
| `subject` | `VARCHAR(200)` | 邮件主题 |
| `content` | `MEDIUMTEXT` | 邮件正文，保存 `RichTextNode[]` 富文本数组的 JSON 字符串 |
| `sent_at` | `DATETIME` | 发送时间，对应 API 的 `sentAt` |
| `status` | `TINYINT` | 邮件状态，当前 `1` 表示已发送 |
| `sender_deleted` | `TINYINT` | 发件人侧删除预留字段 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

业务规则：

- 发送邮件时写入 `mail_message` 后，再写入 `mail_recipient`。
- 已发送列表按 `sender_id` 查询，并按 `sent_at` 倒序。
- 当前 API 不开放发件人删除，`sender_deleted` 仅预留。

### 5.4 `mail_recipient`

用途：

保存收件人与邮件之间的关系，以及收件人个人状态。

核心字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `mail_id` | `BIGINT` | 邮件 ID |
| `recipient_id` | `BIGINT` | 收件人用户 ID |
| `recipient_type` | `TINYINT` | 收件人类型，`1` 普通收件人，`2` CC，`3` BCC |
| `read_flag` | `TINYINT` | 是否已读，`0` 未读，`1` 已读 |
| `read_at` | `DATETIME` | 已读时间 |
| `deleted_flag` | `TINYINT` | 是否被收件人逻辑删除 |
| `deleted_at` | `DATETIME` | 删除时间 |
| `spam_flag` | `TINYINT` | 是否进入垃圾邮箱 |
| `spam_level` | `VARCHAR(16)` | 垃圾等级 |
| `risk_level` | `VARCHAR(16)` | 风险等级 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

业务规则：

- 新邮件默认 `read_flag = 0`。
- 收件人查看详情时可自动更新 `read_flag = 1` 和 `read_at`。
- 显式标记已读接口也更新 `read_flag` 和 `read_at`。
- 收件人删除时更新 `deleted_flag = 1` 和 `deleted_at`。
- 普通收件箱过滤 `deleted_flag = 0` 且 `spam_flag = 0`。
- 已删除列表过滤 `deleted_flag = 1`。
- 垃圾邮箱过滤 `spam_flag = 1` 或高风险等级。

### 5.5 `mail_analysis`

用途：

保存规则、机器学习或 AI 分析结果。该表服务于列表和详情中的分析字段。

核心字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 |
| `mail_id` | `BIGINT` | 邮件 ID |
| `recipient_id` | `BIGINT` | 收件人 ID |
| `analysis_status` | `VARCHAR(20)` | 分析状态 |
| `priority` | `VARCHAR(16)` | 优先级 |
| `priority_score` | `INT` | 优先级评分 |
| `priority_reason` | `VARCHAR(500)` | 优先级原因 |
| `spam_flag` | `TINYINT` | 是否垃圾邮件 |
| `spam_score` | `INT` | 垃圾邮件评分 |
| `spam_level` | `VARCHAR(16)` | 垃圾等级 |
| `spam_reason` | `VARCHAR(500)` | 垃圾判断原因 |
| `risk_level` | `VARCHAR(16)` | 风险等级 |
| `risk_score` | `INT` | 风险评分 |
| `risk_reason` | `VARCHAR(500)` | 风险原因 |
| `summary` | `TEXT` | 邮件摘要 |
| `reply_suggestions` | `JSON` | 回复建议数组 |
| `ai_provider` | `VARCHAR(64)` | 使用的模型服务商 |
| `model_name` | `VARCHAR(128)` | 使用的模型名称 |
| `ai_error_message` | `VARCHAR(500)` | AI 调用错误信息 |
| `created_at` | `DATETIME` | 创建时间 |
| `updated_at` | `DATETIME` | 更新时间 |

业务规则：

- 发送邮件后可以同步或异步写入分析结果。
- AI 失败不能影响邮件发送成功。
- 列表页只返回展示和筛选所需的分析字段。
- 详情页返回完整 `analysis` 对象。
- `priority_score`、`spam_score`、`risk_score`、`ai_provider`、`model_name` 为内部计算、排序或排错字段，当前最终版接口层不返回。

## 6. 枚举约定

### 6.1 `analysis_status`

| 值 | 说明 |
| --- | --- |
| `NOT_STARTED` | 未分析 |
| `PENDING` | 分析中 |
| `SUCCESS` | 分析成功 |
| `FAILED` | 分析失败 |
| `DISABLED` | 用户关闭 AI 功能 |

### 6.2 `priority`

| 值 | 说明 |
| --- | --- |
| `LOW` | 低优先级 |
| `MEDIUM` | 中优先级 |
| `HIGH` | 高优先级 |

### 6.3 `spam_level`

| 值 | 说明 |
| --- | --- |
| `NONE` | 非垃圾邮件 |
| `LOW` | 低垃圾风险 |
| `MEDIUM` | 中垃圾风险 |
| `HIGH` | 高垃圾风险 |

### 6.4 `risk_level`

| 值 | 说明 |
| --- | --- |
| `SAFE` | 安全 |
| `LOW` | 低风险 |
| `MEDIUM` | 中风险 |
| `HIGH` | 高风险 |

## 7. 典型查询规则

### 7.1 收件箱

```text
mail_recipient.recipient_id = current_user_id
mail_recipient.deleted_flag = 0
mail_recipient.spam_flag = 0
```

按 `mail_message.sent_at` 倒序分页。若开启优先级排序，可先按 `mail_analysis.priority_score` 倒序，再按发送时间倒序。

### 7.2 已发送

```text
mail_message.sender_id = current_user_id
mail_message.sender_deleted = 0
```

已发送列表不受 `mail_recipient.deleted_flag` 影响。

### 7.3 已删除

```text
mail_recipient.recipient_id = current_user_id
mail_recipient.deleted_flag = 1
```

### 7.4 垃圾邮箱

```text
mail_recipient.recipient_id = current_user_id
mail_recipient.deleted_flag = 0
AND (
  mail_recipient.spam_flag = 1
  OR mail_recipient.risk_level IN ('MEDIUM', 'HIGH')
)
```

### 7.5 邮件详情权限

允许查看：

```text
mail_message.sender_id = current_user_id
OR mail_recipient.recipient_id = current_user_id
```

无权限返回 `40003`。

## 8. API 字段映射

| API 字段 | 数据库来源 |
| --- | --- |
| `username` | `sys_user.username` |
| `nickname` | `sys_user.nickname` |
| `emailAddress` | `sys_user.email_address` |
| `mailId` | `mail_message.id` |
| `subject` | `mail_message.subject` |
| `content` | `mail_message.content` |
| `sentAt` | `mail_message.sent_at` |
| `sender` | `mail_message.sender_id -> sys_user` |
| `recipient` | `mail_recipient.recipient_id -> sys_user` |
| `read` | `mail_recipient.read_flag` |
| `deleted` | `mail_recipient.deleted_flag` |
| `deletedAt` | `mail_recipient.deleted_at` |
| `spam` | `mail_recipient.spam_flag` 或 `mail_analysis.spam_flag` |
| `spamLevel` | `mail_analysis.spam_level` |
| `riskLevel` | `mail_analysis.risk_level` |
| `priority` | `mail_analysis.priority` |
| `analysisStatus` | `mail_analysis.analysis_status` |
| `summary` | `mail_analysis.summary` |
| `replySuggestions` | `mail_analysis.reply_suggestions` |

## 9. 建表 SQL

建表脚本维护在：

```text
sql/schema.sql
```

初始测试数据维护在：

```text
sql/init-data.sql
```

当前建表脚本用于开发环境重建表结构，会删除并重建核心表。执行前应确认本地数据可以被清空。建表完成后可按需执行初始数据脚本导入测试账号和默认用户设置。

