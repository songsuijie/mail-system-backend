# AI 真实模型调用开发说明

## 1. 文档目的

本文档给负责 AI 模块的同学使用，说明如何在当前邮件系统后端中接入真实大模型调用。

当前系统已经有：

- 用户 AI 设置接口：`GET /api/users/settings`、`PUT /api/users/settings`
- 邮件分析表：`mail_analysis`
- 规则分析能力：优先级、垃圾等级、风险等级、摘要、回复建议
- 重新分析接口：`POST /api/mails/{mailId}/analysis/retry`

后续要做的是：在不改变前端接口的前提下，把当前规则分析升级为“优先调用真实模型，失败后回退规则分析”。

## 2. 核心原则

1. 不新增前端 AI 接口。
2. 不改变现有邮件接口响应结构。
3. AI 调用失败不能影响发邮件成功。
4. AI 没配置、关闭、超时、返回格式错误时，都回退到规则分析。
5. API Key 不能明文返回给前端。

也就是说，前端仍然只调用：

```http
POST /api/emails/send
POST /api/emails/reply
POST /api/mails/{mailId}/analysis/retry
GET /api/threads
GET /api/threads/{threadId}
```

AI 是后端内部增强能力。

## 3. 推荐实现结构

建议新增或拆分以下类：

```text
service/ai/AiAnalysisService.java
service/ai/impl/AiAnalysisServiceImpl.java
service/ai/RuleAnalysisService.java
service/ai/impl/RuleAnalysisServiceImpl.java
utils/ApiKeyCryptoUtils.java
```

职责建议：

| 类 | 职责 |
| --- | --- |
| `AiAnalysisService` | 对外提供“分析邮件”的统一入口 |
| `AiAnalysisServiceImpl` | 判断是否启用 AI、调用真实模型、失败回退 |
| `RuleAnalysisService` | 保存当前关键词规则分析逻辑 |
| `ApiKeyCryptoUtils` | API Key 加密、解密、脱敏 |

当前 `MailServiceImpl` 里的 `buildMailAnalysis(...)` 可以逐步迁移到 `RuleAnalysisService`。

## 4. 调用流程

发送邮件或重新分析时，流程如下：

```text
1. 用户发送邮件或触发重新分析
2. 后端保存邮件主体和收件人关系
3. 查询当前用户 user_settings
4. 判断 aiEnabled 和 modelConfigured
5. 如果未开启或未配置，直接使用规则分析
6. 如果已开启并配置完整，调用真实模型
7. 模型返回合法 JSON，则保存 AI 分析结果
8. 模型失败、超时、401、返回非 JSON，则保存规则分析结果
9. 无论 AI 是否成功，发邮件接口都返回成功
```

## 5. OpenAI Compatible 调用方式

优先按 OpenAI Compatible 格式实现，这样可以兼容 DeepSeek、通义千问、Kimi、GLM、SiliconFlow、自定义地址等。

请求：

```http
POST {baseUrl}/chat/completions
Authorization: Bearer {apiKey}
Content-Type: application/json
```

请求体示例：

```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "system",
      "content": "你是邮件分析助手。你只能输出 JSON，不要输出 Markdown。"
    },
    {
      "role": "user",
      "content": "请分析以下邮件，返回优先级、垃圾等级、风险等级、摘要和回复建议。"
    }
  ],
  "temperature": 0.2,
  "max_tokens": 800
}
```

返回内容通常在：

```text
choices[0].message.content
```

## 6. 模型输出格式

提示词应要求模型只返回如下 JSON：

```json
{
  "priority": "HIGH",
  "priorityReason": "邮件包含截止时间和提交要求",
  "spam": false,
  "spamLevel": "NONE",
  "spamReason": "未发现垃圾邮件特征",
  "riskLevel": "SAFE",
  "riskReason": "",
  "summary": "提醒收件人按时提交实验报告。",
  "replySuggestions": [
    "收到，我会按时提交。",
    "好的，我会尽快处理。"
  ]
}
```

后端只接受以下枚举：

```text
priority: LOW / MEDIUM / HIGH
spamLevel: NONE / LOW / MEDIUM / HIGH
riskLevel: SAFE / LOW / MEDIUM / HIGH
```

如果模型返回了其他值，应修正为默认值或回退规则分析。

## 7. Prompt 建议

System Prompt：

```text
你是邮件分析助手。请根据邮件主题和正文分析邮件。
你只能输出 JSON，不要输出 Markdown，不要解释。
priority 只能是 LOW、MEDIUM、HIGH。
spamLevel 只能是 NONE、LOW、MEDIUM、HIGH。
riskLevel 只能是 SAFE、LOW、MEDIUM、HIGH。
replySuggestions 返回 2 条中文短回复。
```

User Prompt：

```text
邮件主题：
{subject}

邮件正文：
{plainText}

请输出 JSON：
{
  "priority": "",
  "priorityReason": "",
  "spam": false,
  "spamLevel": "",
  "spamReason": "",
  "riskLevel": "",
  "riskReason": "",
  "summary": "",
  "replySuggestions": []
}
```

## 8. 和 user_settings 的关系

AI 调用需要读取 `user_settings`：

| 字段 | 用途 |
| --- | --- |
| `ai_enabled` | 是否启用 AI 分析 |
| `provider` | 模型服务商 |
| `base_url` | OpenAI Compatible 地址 |
| `model_name` | 模型名称 |
| `api_key_encrypted` | 加密后的 API Key |
| `timeout_ms` | 请求超时时间 |
| `max_tokens` | 最大输出长度 |
| `temperature` | 模型温度 |

注意：

- 当前代码需要能解密 `api_key_encrypted`。
- 建议把 API Key 的编码、解码、脱敏逻辑从 `UserSettingsServiceImpl` 抽到工具类。
- 如果最终 OpenAPI 使用 `openai-compatible` 作为 provider，需要代码支持该值。

## 9. 失败降级规则

以下情况必须回退规则分析：

- 用户关闭 AI
- 模型未配置完整
- API Key 为空
- HTTP 401 / 403
- HTTP 429
- HTTP 500
- 请求超时
- 模型返回空内容
- 模型返回非 JSON
- JSON 缺少必要字段
- 枚举值不合法

回退后仍然要保存 `mail_analysis`，建议：

```text
analysis_status = SUCCESS
ai_error_message = 记录简短错误原因
ai_provider = provider
model_name = modelName
```

不要因为 AI 失败让发邮件接口返回失败。

## 10. 测试建议

### 10.1 单元测试

建议覆盖：

1. 未开启 AI，直接使用规则分析。
2. AI 配置不完整，直接使用规则分析。
3. AI 返回合法 JSON，保存模型分析结果。
4. AI 超时，回退规则分析。
5. AI 返回非 JSON，回退规则分析。
6. AI 返回非法枚举，回退规则分析或修正默认值。

### 10.2 Apifox 联调

1. 登录获取 token。
2. 调用 `PUT /api/users/settings` 配置模型。
3. 发送一封普通邮件。
4. 查询 `/api/threads` 看分析字段。
5. 查询 `/api/threads/{threadId}` 看详情分析字段。
6. 调用 `POST /api/mails/{mailId}/analysis/retry` 测试重新分析。

### 10.3 推荐测试邮件

高优先级：

```text
请在今天下班前提交实验报告，逾期将无法补交。
```

风险邮件：

```text
你的账号异常，请立即点击链接修改密码并填写银行卡信息。
```

垃圾邮件：

```text
恭喜你中奖，点击领取免费礼品卡和限时优惠。
```

## 11. 最小开发顺序

建议按以下顺序做：

1. 抽出当前规则分析为 `RuleAnalysisService`。
2. 抽出 API Key 工具类，支持解密。
3. 新增 `AiAnalysisService`，先写接口和降级框架。
4. 用 Mock 测试 AI 成功和失败。
5. 接入 OpenAI Compatible HTTP 调用。
6. 接到发邮件和重新分析流程。
7. 用真实模型做一次 Apifox 联调。

## 12. 验收标准

AI 模块完成后应满足：

- 不改前端接口。
- 发邮件在 AI 失败时仍然成功。
- 模型配置完整时可以调用真实模型。
- 模型结果能写入 `mail_analysis`。
- 列表和详情能展示 AI 分析结果。
- 重新分析接口能触发真实模型调用。
- API Key 不会明文返回给前端。
