# Git 协作规范

## 1. 目标

本规范用于约束邮件系统后端项目的分支、提交和合并流程，避免多人协作时出现代码覆盖、提交混乱、文档不同步等问题。

当前项目是课程实训项目，流程应保持简单清晰，不引入过重的工程管理负担。

## 2. 分支模型

项目使用以下分支：

| 分支 | 用途 | 说明 |
| --- | --- | --- |
| `main` | 稳定版本分支 | 用于最终提交、答辩演示和阶段稳定版本 |
| `dev` | 开发集成分支 | 所有功能分支先合并到 `dev` |
| `feature/*` | 功能开发分支 | 每个功能或文档任务独立创建一个分支 |

不要直接在 `main` 分支上开发。

## 3. 分支命名

功能分支命名格式：

```text
feature/<short-name>
```

推荐示例：

```text
feature/user-login
feature/mail-send
feature/inbox-api
feature/mail-detail
feature/database-init
feature/api-docs
feature/git-workflow-doc
```

命名要求：

- 使用英文小写
- 单词之间使用短横线
- 名称应能表达本次任务内容
- 不使用 `test`、`temp`、`final` 等含义不清的名称

## 4. 开发流程

推荐流程：

1. 切换到 `dev`

```bash
git switch dev
```

2. 拉取最新代码

```bash
git pull origin dev
```

3. 创建功能分支

```bash
git switch -c feature/mail-send
```

4. 完成功能开发或文档修改

5. 本地检查变更

```bash
git status
git diff
```

6. 提交代码

```bash
git add <files>
git commit -m "feat(mail): implement send mail api"
```

7. 推送功能分支

```bash
git push origin feature/mail-send
```

8. 合并到 `dev`

功能自测通过后，再合并到 `dev`。如果使用 GitHub，建议通过 Pull Request 合并。

9. 合并到 `main`

只有当 `dev` 上的功能稳定、可以演示或需要阶段交付时，才合并到 `main`。

## 5. Commit Message 规范

提交信息格式：

```text
type(scope): message
```

常用 `type`：

| 类型 | 用途 |
| --- | --- |
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `docs` | 文档修改 |
| `refactor` | 重构，不改变外部行为 |
| `test` | 测试相关 |
| `chore` | 构建、配置、依赖、杂项 |
| `style` | 代码格式调整，不改变逻辑 |

常用 `scope`：

| 范围 | 含义 |
| --- | --- |
| `auth` | 注册、登录、鉴权 |
| `user` | 用户信息 |
| `mail` | 邮件发送、查询、详情、状态 |
| `database` | 数据库设计或 SQL |
| `api` | 接口文档或接口契约 |
| `docs` | 项目文档 |
| `config` | 项目配置 |

示例：

```text
feat(auth): implement login api
feat(mail): implement send mail api
feat(mail): implement inbox query
fix(mail): fix mail detail permission check
docs(api): update mail api document
docs(database): update mail table design
chore(config): update application config
```

避免以下提交信息：

```text
update
test
aaa
final
fix bug
随便改一下
```

## 6. 提交粒度

提交应保持相对独立，便于回看和回滚。

推荐做法：

- 一个提交只解决一个明确问题
- 功能代码和大规模文档修改尽量分开提交
- 数据库结构变化应同时提交 `docs/database.md` 和 `sql/schema.sql`
- API 变化应同时提交 `docs/api.md`
- 不把格式化整个项目和业务逻辑修改混在一个提交里

## 7. 合并前检查

合并到 `dev` 前，应至少完成：

1. `git status` 确认没有误提交文件
2. `git diff` 检查改动范围符合任务目标
3. 项目可以正常编译
4. 相关接口可以通过 Apifox 或类似工具测试
5. API 变化已更新 `docs/api.md`
6. 数据库变化已更新 `docs/database.md` 和 `sql/schema.sql`
7. 没有提交 `target/`、日志、私有配置、临时文件

## 8. 文档同步规则

以下变更必须同步更新文档：

| 变更内容 | 必须更新 |
| --- | --- |
| 新增或修改接口路径 | `docs/api.md` |
| 修改请求参数或响应字段 | `docs/api.md` |
| 修改错误码或鉴权规则 | `docs/api.md` |
| 新增、删除、修改数据库字段 | `docs/database.md`、`sql/schema.sql` |
| 修改 MVP 功能范围 | `docs/mvp.md` |
| 修改启动方式或环境变量 | `README.md` |
| 修改协作流程 | `docs/git-workflow.md` |

不要在代码中静默改变接口或数据库行为。

## 9. 不应提交的内容

不要提交：

- `target/`
- `.idea/` 个人配置
- `*.iml`
- `*.log`
- 本地临时文件
- 本地数据库导出文件
- 个人笔记
- 压缩包
- 私有密码
- 私有 token
- 私有 API key

提交前可以使用以下命令检查：

```bash
git status --short
```

## 10. 冲突处理

出现冲突时：

1. 先确认冲突文件属于谁负责的模块
2. 不直接删除他人代码来解决冲突
3. 对接口、数据库、公共类冲突要和组员确认
4. 解决冲突后重新运行编译和必要测试
5. 如果冲突涉及文档和实现不一致，应同步修正文档

## 11. main 分支规则

`main` 分支只保存稳定版本。

可以合并到 `main` 的情况：

- 阶段 MVP 已完整通过测试
- 答辩或演示前准备稳定版本
- 需要提交课程阶段成果

不应合并到 `main` 的情况：

- 功能尚未完成
- 接口尚未联调
- 数据库脚本和文档不一致
- 代码无法编译
- 只是个人临时实验

## 12. 推荐工作习惯

每次开始任务前：

```bash
git switch dev
git pull origin dev
git switch -c feature/<task-name>
```

每次提交前：

```bash
git status
git diff
```

每次合并前：

```bash
mvn test
```

如果当前阶段还没有完整测试用例，至少应保证项目可以编译，并通过 Apifox 验证本次涉及的核心接口。
