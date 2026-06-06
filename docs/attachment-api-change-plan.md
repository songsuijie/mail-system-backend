# 附件能力接口修改方案

## 1. 修改背景

原接口文档中，通用请求格式默认是 JSON，并明确写了“当前版本不提供附件上传接口，不涉及 `multipart/form-data`”；同时“当前版本不做的接口和能力”里也把“附件上传”列为不纳入范围。

现在为了支持邮件附件，需要在原接口文档基础上做一次**最小改造**：新增通用上传/下载文件接口，并让发送邮件接口和邮件详情接口支持一个附件。

---

## 2. 总体方案

本次只做**单附件最小版**：

```text
一封邮件最多支持 1 个附件
先上传文件，得到 fileId
发送邮件时携带 attachmentFileId
查看邮件详情时返回 attachment
下载附件时校验当前用户是否为该邮件发件人或收件人
```

不改动正文 `content` 的设计。邮件正文仍然是 `RichTextNode[]` 富文本数组，附件作为独立字段处理。

---

## 3. 新增接口

### 3.1 上传文件

```http
POST /api/files
```

请求格式：

```http
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

请求参数：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | file | 是 | 上传文件 |

支持文件类型：

```text
.png / .jpg / .jpeg / .pdf / .docx / .zip
```

暂不支持：

```text
.7z
```

建议限制：

```text
单个文件最大 10MB
```

成功响应采用最小结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "fileId": "file_20260603_001"
  }
}
```

失败情况：

| 场景 | code | message |
|---|---:|---|
| 未登录 | 40002 | 请先登录 |
| 文件为空 | 40000 | 文件不能为空 |
| 文件类型不支持 | 40000 | 文件类型不支持 |
| 文件过大 | 40000 | 文件大小不能超过 10MB |
| 上传失败 | 50000 | 文件上传失败 |

---

### 3.2 下载文件

```http
GET /api/files/{fileId}/download
```

请求头：

```http
Authorization: Bearer <token>
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| fileId | string | 是 | 文件 ID |

成功时返回文件流，不走统一 JSON：

```http
Content-Type: application/pdf
Content-Disposition: attachment; filename*=UTF-8''实验报告.pdf
```

错误时仍然返回统一 JSON：

```json
{
  "code": 40003,
  "message": "无权限下载该文件",
  "data": null
}
```

下载权限规则：

```text
1. 未登录不能下载。
2. 文件不存在，返回 40004 文件不存在。
3. 文件未绑定邮件时，只允许上传者本人访问。
4. 文件绑定邮件后，只有该邮件的发件人和收件人可以下载。
5. 不允许通过真实文件路径直接下载，只能通过 fileId 下载。
```

---

## 4. 修改发送邮件接口

原来的 `POST /api/mails` 请求体只有：

```text
recipientUsername
subject
content
```

现在新增一个非必填字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| attachmentFileId | string | 否 | 附件文件 ID；当前版本一封邮件最多一个附件 |

修改后请求示例：

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
    }
  ],
  "attachmentFileId": "file_20260603_001"
}
```

发送邮件时新增校验：

```text
1. attachmentFileId 不传：正常发送无附件邮件。
2. attachmentFileId 传了：文件必须存在。
3. 文件必须由当前登录用户上传。
4. 文件不能已经绑定到其他邮件。
5. 校验通过后，将该 fileId 绑定到当前邮件。
```

新增失败情况：

| 场景 | code | message |
|---|---:|---|
| 文件不存在 | 40004 | 文件不存在 |
| 使用了别人的文件 | 40003 | 无权限使用该文件 |
| 文件已绑定其他邮件 | 40000 | 文件已被使用 |

---

## 5. 修改邮件详情接口

在 `GET /api/mails/{mailId}` 的详情响应中新增：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| attachment | object/null | 是 | 当前邮件附件；无附件时为 null |

有附件时示例：

```json
{
  "attachment": {
    "fileId": "file_20260603_001",
    "originalFilename": "实验报告.pdf",
    "contentType": "application/pdf",
    "fileSize": 204800,
    "downloadUrl": "/api/files/file_20260603_001/download"
  }
}
```

无附件时：

```json
{
  "attachment": null
}
```

说明：上传接口可以只返回 `fileId`，但详情接口建议返回文件名、类型、大小和下载地址，方便前端展示附件卡片。

---

## 6. 数据库最小修改

本次建议新增一张文件表，并给邮件表增加一个附件字段。

### 6.1 新增文件资源表

```sql
CREATE TABLE file_resource (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_id VARCHAR(64) NOT NULL UNIQUE COMMENT '对外暴露的文件ID',
  uploader_id BIGINT NOT NULL COMMENT '上传用户ID',
  mail_id BIGINT NULL COMMENT '绑定的邮件ID，未发送前为空',
  original_filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
  stored_filename VARCHAR(255) NOT NULL COMMENT '服务端保存文件名',
  storage_path VARCHAR(500) NOT NULL COMMENT '服务端存储路径',
  content_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
  file_ext VARCHAR(20) NOT NULL COMMENT '文件扩展名',
  file_size BIGINT NOT NULL COMMENT '文件大小，单位byte',
  status VARCHAR(20) NOT NULL DEFAULT 'TEMP' COMMENT 'TEMP/BOUND',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 6.2 修改邮件主体表

```sql
ALTER TABLE mail_message
ADD COLUMN attachment_file_id VARCHAR(64) NULL COMMENT '附件文件ID，当前版本一封邮件最多一个附件';
```

如果后端实际表名是 `mail` 而不是 `mail_message`，就把表名替换成实际表名。

---

## 7. 当前仍然不做的内容

这次只是补最小附件能力，不做复杂文件系统：

```text
不做多附件
不做附件在线预览
不做附件删除
不做断点续传
不做文件夹管理
不做病毒查杀
不做 .7z
不做公开文件访问
```

原文档“当前版本不做的接口和能力”中应把“附件上传”改成：

```text
复杂附件能力：当前版本不做。
当前只支持单附件上传、单附件下载和邮件附件展示。
```

---

## 8. 最终接口清单变化

新增：

| 模块 | 方法 | 接口 | 优先级 | 说明 |
|---|---|---|---|---|
| 文件 | POST | /api/files | P1 | 上传文件，返回 fileId |
| 文件 | GET | /api/files/{fileId}/download | P1 | 下载文件，需校验权限 |

修改：

| 接口 | 修改内容 |
|---|---|
| POST /api/mails | 请求体新增 `attachmentFileId`，非必填 |
| GET /api/mails/{mailId} | 响应体新增 `attachment`，无附件时为 null |
| 当前版本不做的接口和能力 | 删除“附件上传完全不做”的表述，改为“不做复杂附件能力” |

---

## 9. 推荐结论

本次最小修改方案就是：

```text
新增上传文件接口；
新增下载文件接口；
发送邮件时可携带 attachmentFileId；
邮件详情返回 attachment；
当前版本一封邮件最多一个附件；
下载时必须校验当前用户是邮件发件人或收件人。
```

这个方案改动小、前后端容易理解，也不会破坏现有邮件正文 `content` 和 P0 邮件主流程。
