# 数据库设计说明

## 1. 文档定位

本文档定义邮件系统后端第一版 MVP 的数据库设计，是后续编写 `sql/schema.sql`、Entity、Mapper、Service 和接口实现的依据。

第一版数据库只服务于基础站内邮件闭环：

1. 用户注册和登录
2. 当前用户信息查询
3. 发送站内邮件
4. 查询收件箱
5. 查询已发送邮件
6. 查询邮件详情
7. 标记邮件已读
8. 收件人侧逻辑删除

第一版暂不实现真实外部 SMTP、POP3、IMAP 邮件协议互通，也不设计附件、复杂文件夹、复杂垃圾箱、AI 摘要等扩展表。

## 2. 设计原则

### 2.1 核心原则

数据库设计遵守以下原则：

1. 先支撑 MVP 闭环，不提前引入过多表
2. 邮件主体和收件人状态分离
3. 已读、删除等个人状态放在收件关系表中
4. 所有核心业务表保留创建时间、更新时间和逻辑删除字段
5. 字段命名使用数据库常见的 `snake_case`
6. Java 代码中可映射为 `camelCase`
7. 表结构需要方便后续扩展多收件人、抄送、密送、附件、搜索和 AI 功能

### 2.2 为什么拆分邮件主体和收件关系

不要把收件人的已读状态、删除状态直接放在 `mail_message` 表。

原因：

一个邮件未来可能有多个收件人，每个收件人都有自己的状态：

- A 用户可能已读
- B 用户可能未读
- A 用户可能已删除
- B 用户仍然保留在收件箱

因此第一版即使只支持一个普通收件人，也应使用：

- `mail_message` 保存邮件公共内容
- `mail_recipient` 保存收件人个人状态

## 3. 核心表概览

第一版 MVP 使用三张核心表：

| 表名 | 说明 | 主要职责 |
| --- | --- | --- |
| `sys_user` | 用户表 | 保存用户账号、密码、昵称、邮箱和状态 |
| `mail_message` | 邮件主体表 | 保存发件人、主题、正文和发送时间 |
| `mail_recipient` | 邮件收件关系表 | 保存收件人、收件类型、已读状态、读信时间和删除状态 |

第一版不单独设计 token 表。登录 token 可先使用简单内存方案实现；如果后续需要服务重启后保留登录态，再新增 `sys_user_token` 或引入 JWT、Redis。

## 4. 表关系

### 4.1 用户与邮件主体

一个用户可以发送多封邮件。

```text
sys_user 1 ---- N mail_message
```

关系字段：

```text
mail_message.sender_id -> sys_user.id
```

### 4.2 邮件主体与收件关系

一封邮件可以对应多条收件关系。

第一版只创建一条普通收件人记录，后续可扩展为多个收件人、抄送和密送。

```text
mail_message 1 ---- N mail_recipient
```

关系字段：

```text
mail_recipient.mail_id -> mail_message.id
```

### 4.3 用户与收件关系

一个用户可以收到多封邮件。

```text
sys_user 1 ---- N mail_recipient
```

关系字段：

```text
mail_recipient.recipient_id -> sys_user.id
```

### 4.4 整体关系

```text
sys_user
   | 1
   | sends
   | N
mail_message
   | 1
   | has recipients
   | N
mail_recipient
   | N
   | received by
   | 1
sys_user
```

## 5. 表结构设计

### 5.1 `sys_user`

用途：

保存用户账号信息，用于注册、登录、当前用户查询、发件人和收件人展示。

字段设计：

| 字段名 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 是 | 自增 | 主键 |
| `username` | `VARCHAR(32)` | 是 | 无 | 用户名，唯一，登录和站内收件使用 |
| `password` | `VARCHAR(255)` | 是 | 无 | 密码摘要，不保存明文密码 |
| `nickname` | `VARCHAR(64)` | 是 | 无 | 昵称，不传时可使用 `username` |
| `email` | `VARCHAR(128)` | 否 | `NULL` | 邮箱地址，第一版仅作为资料展示 |
| `status` | `TINYINT` | 是 | `1` | 用户状态，`1` 正常，`0` 禁用 |
| `created_at` | `DATETIME` | 是 | 当前时间 | 创建时间 |
| `updated_at` | `DATETIME` | 是 | 当前时间 | 更新时间 |
| `deleted` | `TINYINT` | 是 | `0` | 逻辑删除，`0` 未删除，`1` 已删除 |

约束和索引：

| 类型 | 字段 | 说明 |
| --- | --- | --- |
| 主键 | `id` | 用户唯一标识 |
| 唯一索引 | `username` | 保证用户名唯一 |
| 普通索引 | `status` | 便于筛选正常用户 |
| 普通索引 | `deleted` | 便于过滤逻辑删除用户 |

业务规则：

- `username` 是第一版登录名，也是发送邮件时的收件人标识
- 注册时必须校验 `username` 唯一
- 登录时只允许 `status = 1` 且 `deleted = 0` 的用户登录
- `password` 字段保存加密或哈希后的值，不保存明文密码
- 第一版不实现复杂角色权限，所有注册用户视为普通用户

### 5.2 `mail_message`

用途：

保存邮件主体信息，包括发件人、主题、正文和发送时间。

字段设计：

| 字段名 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 是 | 自增 | 主键，邮件 ID |
| `sender_id` | `BIGINT` | 是 | 无 | 发件人用户 ID |
| `subject` | `VARCHAR(200)` | 是 | 无 | 邮件主题 |
| `content` | `TEXT` | 是 | 无 | 邮件正文 |
| `send_time` | `DATETIME` | 是 | 当前时间 | 实际发送时间 |
| `status` | `TINYINT` | 是 | `1` | 邮件状态，第一版使用 `1` 表示已发送 |
| `created_at` | `DATETIME` | 是 | 当前时间 | 创建时间 |
| `updated_at` | `DATETIME` | 是 | 当前时间 | 更新时间 |
| `deleted` | `TINYINT` | 是 | `0` | 发件人侧逻辑删除预留字段，第一版可不开放接口 |

约束和索引：

| 类型 | 字段 | 说明 |
| --- | --- | --- |
| 主键 | `id` | 邮件唯一标识 |
| 普通索引 | `sender_id` | 支持查询已发送邮件 |
| 普通索引 | `send_time` | 支持按发送时间排序 |
| 普通索引 | `deleted` | 支持过滤发件人侧已删除邮件 |

业务规则：

- `sender_id` 对应 `sys_user.id`
- `subject` 不能为空，建议后端限制长度不超过 200
- `content` 不能为空
- 发送邮件时先写入 `mail_message`，再写入 `mail_recipient`
- 查询已发送邮件时按 `sender_id` 查询，并按 `send_time` 倒序排序
- 第一版 `deleted` 不影响收件人收件箱，只作为发件人侧删除预留

### 5.3 `mail_recipient`

用途：

保存邮件和收件人的关系，以及收件人个人状态。

字段设计：

| 字段名 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 是 | 自增 | 主键 |
| `mail_id` | `BIGINT` | 是 | 无 | 邮件 ID |
| `recipient_id` | `BIGINT` | 是 | 无 | 收件人用户 ID |
| `recipient_type` | `TINYINT` | 是 | `1` | 收件人类型，`1` 普通收件人，`2` 抄送，`3` 密送 |
| `read_status` | `TINYINT` | 是 | `0` | 已读状态，`0` 未读，`1` 已读 |
| `read_time` | `DATETIME` | 否 | `NULL` | 首次标记为已读的时间 |
| `deleted` | `TINYINT` | 是 | `0` | 收件人侧逻辑删除，`0` 未删除，`1` 已删除 |
| `created_at` | `DATETIME` | 是 | 当前时间 | 创建时间 |
| `updated_at` | `DATETIME` | 是 | 当前时间 | 更新时间 |

约束和索引：

| 类型 | 字段 | 说明 |
| --- | --- | --- |
| 主键 | `id` | 收件关系唯一标识 |
| 普通索引 | `mail_id` | 支持根据邮件查询收件人 |
| 普通索引 | `recipient_id` | 支持查询收件箱 |
| 组合索引 | `recipient_id, deleted, read_status` | 支持收件箱过滤和未读状态查询 |
| 组合索引 | `mail_id, recipient_id` | 支持权限校验和标记已读 |

业务规则：

- `mail_id` 对应 `mail_message.id`
- `recipient_id` 对应 `sys_user.id`
- 第一版发送邮件时只写入一条 `recipient_type = 1` 的记录
- 新邮件默认 `read_status = 0`
- 标记已读时更新 `read_status = 1` 和 `read_time`
- 逻辑删除时只更新当前收件人的 `deleted = 1`
- 收件箱查询必须过滤 `deleted = 0`
- 发件人不能修改收件人的 `read_status` 和 `deleted`

## 6. 枚举值约定

### 6.1 用户状态 `sys_user.status`

| 值 | 含义 |
| --- | --- |
| `1` | 正常 |
| `0` | 禁用 |

### 6.2 邮件状态 `mail_message.status`

| 值 | 含义 | 第一版是否使用 |
| --- | --- | --- |
| `1` | 已发送 | 是 |
| `0` | 草稿 | 否，预留 |

### 6.3 收件人类型 `mail_recipient.recipient_type`

| 值 | 含义 | 第一版是否使用 |
| --- | --- | --- |
| `1` | 普通收件人 | 是 |
| `2` | 抄送 CC | 否，预留 |
| `3` | 密送 BCC | 否，预留 |

### 6.4 已读状态 `mail_recipient.read_status`

| 值 | 含义 |
| --- | --- |
| `0` | 未读 |
| `1` | 已读 |

### 6.5 逻辑删除状态 `deleted`

| 值 | 含义 |
| --- | --- |
| `0` | 未删除 |
| `1` | 已删除 |

## 7. 典型查询支持

### 7.1 注册时校验用户名唯一

查询 `sys_user`：

```text
username = ?
deleted = 0
```

如果存在记录，则返回用户名已存在。

### 7.2 登录校验

查询 `sys_user`：

```text
username = ?
status = 1
deleted = 0
```

然后比对密码摘要。

### 7.3 发送邮件

发送邮件需要写入两张表：

1. 写入 `mail_message`
   - `sender_id`
   - `subject`
   - `content`
   - `send_time`

2. 写入 `mail_recipient`
   - `mail_id`
   - `recipient_id`
   - `recipient_type = 1`
   - `read_status = 0`
   - `deleted = 0`

这两个写入应放在同一个事务中。

### 7.4 查询收件箱

从 `mail_recipient` 出发关联 `mail_message` 和 `sys_user`：

```text
mail_recipient.recipient_id = current_user_id
mail_recipient.deleted = 0
mail_message.deleted = 0
```

按 `mail_message.send_time` 倒序分页。

返回字段对应 `docs/api.md` 中收件箱列表：

- `mailId`
- `sender`
- `subject`
- `sendTime`
- `readStatus`

### 7.5 查询已发送邮件

从 `mail_message` 出发关联 `mail_recipient` 和 `sys_user`：

```text
mail_message.sender_id = current_user_id
mail_message.deleted = 0
```

按 `mail_message.send_time` 倒序分页。

返回字段对应 `docs/api.md` 中已发送列表：

- `mailId`
- `recipient`
- `subject`
- `sendTime`

### 7.6 查询邮件详情

查询 `mail_message`，并关联：

- 发件人 `sys_user`
- 收件关系 `mail_recipient`
- 收件人 `sys_user`

权限判断：

```text
mail_message.sender_id = current_user_id
OR exists mail_recipient where recipient_id = current_user_id
```

无权限时返回 `403`。

### 7.7 标记已读

更新 `mail_recipient`：

```text
mail_id = ?
recipient_id = current_user_id
deleted = 0
```

更新字段：

```text
read_status = 1
read_time = current_time
updated_at = current_time
```

只有收件人可以标记已读。

### 7.8 逻辑删除收件箱邮件

更新 `mail_recipient`：

```text
mail_id = ?
recipient_id = current_user_id
```

更新字段：

```text
deleted = 1
updated_at = current_time
```

不删除 `mail_message`，不影响发件人的已发送列表。

## 8. 建表 SQL 建议

后续 `sql/schema.sql` 应按照本文档生成。

推荐 MySQL 建表策略：

- 使用 `BIGINT AUTO_INCREMENT` 作为主键
- 使用 `InnoDB` 引擎
- 使用 `utf8mb4` 字符集
- 使用 `DATETIME` 存储业务时间
- 使用 `TINYINT` 存储枚举和逻辑删除状态
- `created_at` 默认当前时间
- `updated_at` 默认当前时间并在更新时自动刷新

外键策略：

第一版可以只使用逻辑外键，不强制添加数据库级外键约束。

原因：

- MyBatis 或 MyBatis Plus 项目中常通过业务逻辑维护关联
- 课程项目调试和重置测试数据更方便
- 后续如果需要更强数据一致性，可以在 `schema.sql` 中补充外键

即使不添加数据库级外键，也必须在业务逻辑中保证：

- `mail_message.sender_id` 对应存在的用户
- `mail_recipient.mail_id` 对应存在的邮件
- `mail_recipient.recipient_id` 对应存在的用户

## 9. 与 API 的字段映射

| API 字段 | 数据库来源 |
| --- | --- |
| `user.id` | `sys_user.id` |
| `user.username` | `sys_user.username` |
| `user.nickname` | `sys_user.nickname` |
| `user.email` | `sys_user.email` |
| `user.status` | `sys_user.status` |
| `mailId` | `mail_message.id` |
| `sender.id` | `mail_message.sender_id` |
| `sender.username` | `sys_user.username` |
| `recipient.id` | `mail_recipient.recipient_id` |
| `recipient.username` | `sys_user.username` |
| `subject` | `mail_message.subject` |
| `content` | `mail_message.content` |
| `sendTime` | `mail_message.send_time` |
| `readStatus` | `mail_recipient.read_status` |
| `readTime` | `mail_recipient.read_time` |
| `recipientType` | `mail_recipient.recipient_type` |
| `deleted` | `mail_recipient.deleted` |

## 10. 后续扩展预留

### 10.1 多收件人、抄送、密送

当前设计已经支持一封邮件对应多条 `mail_recipient`。

后续扩展时：

- 普通收件人使用 `recipient_type = 1`
- 抄送使用 `recipient_type = 2`
- 密送使用 `recipient_type = 3`

### 10.2 附件

后续可新增表：

```text
mail_attachment
```

可能字段：

- `id`
- `mail_id`
- `original_name`
- `stored_name`
- `file_path`
- `file_size`
- `content_type`
- `created_at`
- `deleted`

附件文件保存到磁盘或对象存储，数据库只保存元数据。

### 10.3 邮件搜索

第一版不实现搜索。

后续可以先基于 `mail_message.subject` 和 `mail_message.content` 做普通 SQL 模糊查询；如果数据量变大，再考虑全文索引或搜索引擎。

### 10.4 AI 功能

后续 AI 摘要、分类、垃圾邮件识别可新增独立表，例如：

```text
mail_ai_result
```

可能字段：

- `id`
- `mail_id`
- `summary`
- `category`
- `spam_score`
- `model_name`
- `created_at`
- `updated_at`

AI 结果不应写死进 `mail_message` 的核心发送流程。

## 11. 第一版验收检查

数据库设计完成后，应能支撑以下场景：

- 注册用户时保存账号信息
- 登录时根据用户名查询用户并校验密码
- 根据 token 识别当前用户后查询用户信息
- 发送邮件时保存邮件主体和收件关系
- 收件人查询收件箱并看到未读状态
- 发件人查询已发送邮件
- 发件人和收件人查询邮件详情
- 其他用户无法查看邮件详情
- 收件人标记邮件为已读
- 收件人逻辑删除邮件后，普通收件箱不再展示该邮件
- 发件人的已发送列表不受收件人删除影响

下一步应根据本文档创建或更新 `sql/schema.sql`。
