# Mail System Backend 文档索引

## 1. 文档定位

本目录保存邮件系统后端第一版的核心项目文档。后续开发、联调、测试和答辩准备应优先阅读本目录文档。

当前接口契约以 `docs/默认模块最终版.openapi.json` 为最高优先级，该文件是可导入 Apifox 的最终确认版 OpenAPI。`docs/api.md` 是早期 Markdown 接口说明，仅作为历史参考；若与最终 OpenAPI 冲突，以最终 OpenAPI 为准。

## 2. 推荐阅读顺序

1. `AGENTS.md`
   - 项目整体约束、Codex 协作规则和开发边界。
2. `docs/00-index.md`
   - 文档入口和阅读顺序。
3. `docs/默认模块最终版.openapi.json`
   - 当前最终接口契约，前后端联调、Apifox 导入和后端实现均以此为准。
4. `docs/api.md`
   - 早期 Markdown 接口说明，可辅助理解历史设计；不作为当前最终契约。
5. `docs/mvp.md`
   - 按 API 的 P0/P1/P2 定义第一版功能范围和验收边界。
6. `docs/prd.md`
   - 面向前端的页面、流程和交互说明。
7. `docs/database.md`
   - 数据库表结构、字段含义和 API 字段映射。
8. `sql/schema.sql`
   - MySQL 建表和开发环境重建脚本。
9. `sql/final-contract-supplement.sql`
   - 最终契约补充迁移脚本，补齐线程、回复和附件文件能力。
10. `sql/init-data.sql`
   - 初始测试账号和默认用户设置。
11. `docs/git-workflow.md`
   - 分支、提交和合并规则。
12. `README.md`
   - 项目启动、配置和演示入口。

## 3. 文档职责

| 文件 | 职责 | 维护时机 |
| --- | --- | --- |
| `docs/默认模块最终版.openapi.json` | 最高优先级接口契约，Apifox 可导入文件 | 接口路径、字段、响应、错误码变化时 |
| `docs/api.md` | 早期 Markdown 接口说明 | 仅在需要保留历史说明或人工阅读时同步 |
| `docs/mvp.md` | MVP 功能范围、优先级和验收标准 | 功能范围或优先级变化时 |
| `docs/prd.md` | 前端页面、流程和交互说明 | 页面流程或交互变化时 |
| `docs/database.md` | 数据库设计和 API 字段映射 | 表结构或字段变化时 |
| `sql/schema.sql` | MySQL 基础建表脚本 | 基础数据库结构变化时 |
| `sql/final-contract-supplement.sql` | 最终契约补充迁移脚本 | 线程、回复、附件等补充字段变化时 |
| `sql/init-data.sql` | 初始测试数据脚本 | 测试账号或初始化设置变化时 |
| `docs/git-workflow.md` | 团队协作和提交规范 | 协作流程变化时 |
| `README.md` | 启动方式和项目展示入口 | 启动、配置或核心范围变化时 |

## 4. 当前版本目标

当前版本按最终 OpenAPI 分为：

| 优先级 | 目标 |
| --- | --- |
| P0 | 注册、登录、当前用户、发送邮件、回复邮件、收件箱线程、线程详情、已发送、已读、逻辑删除 |
| P1 | 用户设置、上传文件、下载文件、已删除列表、垃圾邮箱、统计、搜索过滤、分析字段展示 |
| P2 | 修改密码、恢复邮件、重新分析邮件 |

开发应先跑通 P0，再补齐 P1。P2 可后置。

## 5. 当前数据模型

当前数据库以 `docs/api.md` 的实体建议为基础，并使用以下实际表名：

- `sys_user`
- `user_settings`
- `mail_message`
- `mail_recipient`
- `mail_analysis`
- `file_resource`

基础建表脚本在 `sql/schema.sql`，最终契约补充脚本在 `sql/final-contract-supplement.sql`，初始测试数据在 `sql/init-data.sql`。当前 `schema.sql` 用于开发环境重建表结构，执行前应确认本地数据可以清空。

## 6. 变更原则

1. 不随机修改 API 路径、请求字段或响应字段；接口变更先同步最终 OpenAPI。
2. API、数据库或页面交互变更时，同步更新最终 OpenAPI、数据库文档、SQL 和 README。
3. 基础邮件流程不能依赖 AI 调用成功。
4. 不在第一阶段引入 Redis、WebSocket、完整 Spring Security 或复杂角色权限。
5. 不提交 `target/`、日志、私有配置、私有 token、API Key 或无关 IDE 文件。

