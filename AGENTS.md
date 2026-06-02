# AGENTS.md

## 1. Project Overview

This project is a Spring Boot backend project for a course assignment.

Project name:

Mail System Backend

Project root:

D:\project\mail-system-backend

Current goal:

Build the first MVP version of a mail system backend. The project should first complete a clear and runnable basic mail workflow, then reserve extension points for future AI features.

This is not a large enterprise project. Keep the implementation suitable for a course project, but the structure should still be clean enough for teamwork, demonstration, and future extension.

## 2. Current Project Stage

The project is currently in the early backend setup stage.

The backend skeleton and GitHub repository have already been created.

Before writing large amounts of code, the project should first complete the following documents:

1. docs/mvp.md
2. docs/database.md
3. docs/api.md
4. docs/git-workflow.md
5. README.md

These documents are the source of truth for the first version.

Before modifying code, read these files first.

## 3. Read These Files First

Before implementing or modifying backend code, read the following files in order:

1. docs/00-index.md
2. docs/mvp.md
3. docs/database.md
4. docs/api.md
5. docs/git-workflow.md
6. README.md

If these files do not exist yet, help create them according to the project goal.

The current MVP scope is defined by:

docs/mvp.md

The current database design is defined by:

docs/database.md
sql/schema.sql

The current API contract is defined by:

docs/api.md

Do not guess requirements randomly. Follow the existing documents first.

## 4. Recommended Project Structure

The project should roughly follow this structure:

D:\project\mail-system-backend
│
├─ AGENTS.md
├─ README.md
├─ pom.xml
│
├─ docs
│  ├─ 00-index.md
│  ├─ mvp.md
│  ├─ database.md
│  ├─ api.md
│  └─ git-workflow.md
│
├─ sql
│  ├─ schema.sql
│  └─ init-data.sql
│
└─ src
   └─ main
      ├─ java
      └─ resources

Directory responsibilities:

- src: backend source code
- docs: project documents
- sql: database schema and initialization scripts
- README.md: project startup guide
- AGENTS.md: rules and context for Codex

Do not put requirement documents inside src.

Do not put SQL design only inside README.

Do not scatter important project files randomly in the project root.

## 5. MVP Scope

The current API contract is defined by docs/api.md and has the highest priority.

The first version should follow the P0 / P1 / P2 priority model in docs/api.md.

P0 required features:

1. User registration
2. User login
3. Get current logged-in user information
4. Send mail
5. Query inbox
6. Query sent mails
7. Query mail detail
8. Mark mail as read
9. Delete mail logically

P1 recommended features after P0 is stable:

1. User settings and AI configuration status
2. Query deleted mails
3. Query spam mailbox
4. Mailbox statistics
5. Search and filter mail lists
6. Return analysis fields for list and detail pages

P2 optional features:

1. Change password
2. Restore deleted mail
3. Retry mail analysis

The MVP should first prove the P0 workflow, then add P1 features for a fuller frontend demo.

## 6. Features Not Required in P0

Do not implement the following features before the P0 workflow is stable unless explicitly requested:

1. Attachment upload
2. Multiple recipients
3. CC
4. BCC
5. Draft mail
6. Mail recall
7. WebSocket real-time notification
8. Complex admin management
9. Complex role-based permission system
10. Full Spring Security integration
11. Redis token management
12. Semantic search
13. Streaming AI output
14. AI thinking display
15. Native Claude protocol support
16. Multiple model configurations

User settings, spam mailbox, deleted mailbox, mailbox statistics, search filters, and analysis result fields are P1 according to docs/api.md.

## 7. Optional Features

The following features are optional and should only be implemented after P0 and required P1 work is stable:

1. Draft mail
2. Multiple recipients
3. CC
4. BCC
5. Attachments
6. Starred mails
7. Mail labels
8. Restore deleted mail
9. Retry mail analysis

If time is limited, prioritize docs/api.md P0 features first.

## 8. AI and Analysis Principle

docs/api.md already includes user settings, analysis status, priority, spam level, risk level, summary, and reply suggestions.

These features should be treated as enhancement features. The basic mail workflow must still work when AI is disabled, not configured, unavailable, or failed.

Recommended first-version approach:

1. Save mail body and recipient status first.
2. Create a default or rule-based analysis result.
3. If user settings enable AI and the model is configured, call the OpenAI Compatible model.
4. If model calling fails, store the failure or degraded result without affecting mail sending.

Do not provide AI test connection APIs, provider list APIs, streaming output, AI thinking display, native Claude protocol support, or multiple model configuration in the current version unless explicitly requested.

## 9. Recommended Backend Tech Stack

Main tech stack:

- Java
- Spring Boot
- Maven
- MySQL
- MyBatis or MyBatis Plus
- Apifox for API testing

Possible future extensions:

- Redis
- JWT
- Spring Security
- AI service API
- Vector database

Do not add unnecessary dependencies.

Do not introduce Redis, Spring Security, WebSocket, or AI SDK unless the current task clearly requires it.

## 10. Recommended Package Structure

Use a clean layered structure.

Recommended package structure:

com.xxx.mailsystem
├─ controller
├─ service
│  └─ impl
├─ mapper
├─ entity
├─ dto
├─ vo
├─ common
├─ config
├─ exception
└─ utils

Package responsibilities:

controller:
Receive HTTP requests, validate basic parameters, and return unified responses.

service:
Handle business logic.

service.impl:
Implement service interfaces.

mapper:
Access database.

entity:
Map database tables.

dto:
Receive request parameters from frontend.

vo:
Return response data to frontend.

common:
Common response class, constants, enums, and shared utilities.

config:
Configuration classes.

exception:
Global exception handling and custom exceptions.

utils:
Utility classes.

Do not put business logic directly inside Controller.

Do not let Controller directly operate Mapper unless it is a very small temporary prototype and explicitly requested.

## 11. Database Design Principle

The database should support future extension.

Do not design the mail system with only one simple mail table if it will make later extension difficult.

Recommended core tables:

1. sys_user
2. user_settings
3. mail_message
4. mail_recipient
5. mail_analysis

Table responsibilities:

sys_user:
Stores user account information.

user_settings:
Stores user settings and AI model configuration status.

mail_message:
Stores the main body of the mail, including sender, subject, content, and send time.

mail_recipient:
Stores recipient-specific information, including recipient id, read flag, read time, deleted flag, deleted time, spam status, risk level, and recipient type.

mail_analysis:
Stores rule-based, machine-learning, or AI analysis results, including analysis status, priority, spam level, risk level, summary, and reply suggestions.

Important rule:

Do not put recipient-specific read status directly into mail_message.

Reason:

One mail may have multiple recipients in the future. Each recipient may have a different read/unread status and deleted status.

Therefore:

mail_message stores the shared mail body.
mail_recipient stores each recipient's personal mail status.
mail_analysis stores analysis results for list and detail display.

## 12. Recommended Core Tables

The first MVP should use these tables as the base design.

### sys_user

Purpose:

Store user information.

Recommended fields:

- id
- username
- password_hash
- nickname
- email_address
- status
- created_at
- updated_at
- deleted

Rules:

- username should be unique.
- password_hash should not store plain text passwords.
- deleted should be used for logical deletion.
- status can represent normal or disabled users.

### user_settings

Purpose:

Store user-level settings and AI model configuration status.

Recommended fields:

- id
- user_id
- ai_enabled
- auto_reply_enabled
- priority_sort_enabled
- provider
- base_url
- model_name
- api_key_encrypted
- api_key_mask
- timeout_ms
- max_tokens
- temperature
- created_at
- updated_at

Rules:

- Do not return the complete API key from GET /api/users/settings.
- api_key_encrypted must be stored securely.
- modelConfigured can be derived from provider, base_url, model_name, and api_key_encrypted.

### mail_message

Purpose:

Store mail body and sender information.

Recommended fields:

- id
- sender_id
- subject
- content
- sent_at
- status
- sender_deleted
- created_at
- updated_at

Rules:

- sender_id references sys_user.id.
- subject should not be empty.
- content should not be empty.
- sent_at records the actual sending time and maps to API field sentAt.
- sender_deleted is reserved for sender-side logical deletion if needed.

### mail_recipient

Purpose:

Store the relation between mail and recipient.

Recommended fields:

- id
- mail_id
- recipient_id
- recipient_type
- read_flag
- read_at
- deleted_flag
- deleted_at
- spam_flag
- spam_level
- risk_level
- created_at
- updated_at

Rules:

- mail_id references mail_message.id.
- recipient_id references sys_user.id.
- read_flag: 0 means unread, 1 means read.
- deleted_flag: 0 means not deleted, 1 means deleted by recipient.
- recipient_type can reserve space for normal recipient, CC, and BCC.

### mail_analysis

Purpose:

Store analysis results for mail list and detail pages.

Recommended fields:

- id
- mail_id
- recipient_id
- analysis_status
- priority
- priority_score
- priority_reason
- spam_flag
- spam_score
- spam_level
- spam_reason
- risk_level
- risk_score
- risk_reason
- summary
- reply_suggestions
- ai_provider
- model_name
- ai_error_message
- created_at
- updated_at

Rules:

- AI failures must not affect mail sending.
- List pages return only display and filter fields.
- Detail pages return the complete analysis object.

## 13. SQL File Rules

Database schema should be maintained in:

sql/schema.sql

Initial test data should be maintained in:

sql/init-data.sql

If the database structure changes, update all related files:

1. docs/database.md
2. sql/schema.sql
3. docs/api.md if API behavior changes

Do not modify database fields silently without updating documents.

## 14. API Design Principle

The API should be simple, stable, and easy for frontend integration.

All API paths should start with:

/api

Recommended first-version APIs:

POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/logout
GET    /api/users/me
PUT    /api/users/password
GET    /api/users/settings
PUT    /api/users/settings
POST   /api/mails
GET    /api/mails/inbox
GET    /api/mails/sent
GET    /api/mails/trash
GET    /api/mails/spam
GET    /api/mails/{mailId}
PATCH  /api/mails/{mailId}/read
DELETE /api/mails/{mailId}
PATCH  /api/mails/{mailId}/restore
GET    /api/mails/statistics
POST   /api/mails/{mailId}/analysis/retry

Follow docs/api.md as the final API source of truth.

Do not randomly change request paths, request fields, or response fields.

If an API changes, update docs/api.md.

## 15. Unified Response Format

All backend APIs should return a unified response format.

Recommended format:

{
  "code": 0,
  "message": "success",
  "data": {}
}

Recommended business error codes follow docs/api.md:

0: success
40000: invalid parameter
40001: username or password error
40002: not logged in or invalid token
40003: no permission
40004: resource not found
40005: username already exists
40006: recipient does not exist
40007: mail subject cannot be empty
40008: mail content cannot be empty
50000: internal server error
50001: AI analysis failed but must not affect the main mail workflow

Do not return random response formats from different controllers.

## 16. Authentication Rule

The first MVP can use a simple token-based login mechanism.

Basic flow:

1. User logs in with username and password.
2. Backend returns a token.
3. Frontend stores the token.
4. Frontend sends the token in request headers.
5. Backend identifies the current user by token.

Header format:

Authorization: Bearer <token>

For the first MVP, a simple token solution is acceptable.

JWT, Redis token, or Spring Security can be introduced later if explicitly required.

Do not introduce a complex authentication system before the MVP is stable.

## 17. Mail Permission Rules

Mail detail permission:

- The sender can view mails sent by themselves.
- The recipient can view mails received by themselves.
- Other users cannot view the mail.

Inbox query:

- A user can only query mails where mail_recipient.recipient_id equals current user id.
- Records with mail_recipient.deleted_flag = 1 should not appear in the normal inbox.
- Records with mail_recipient.spam_flag = 1 should not appear in the normal inbox.

Sent mail query:

- A user can only query mails where mail_message.sender_id equals current user id.
- Sent mails are not affected by recipient-side deleted_flag.

Mark as read:

- Only the recipient can mark a received mail as read.
- The sender should not mark recipient read status.
- GET /api/mails/{mailId} should automatically mark the mail as read when the current user is the recipient and the mail is unread.

Delete mail:

- First MVP should use logical deletion.
- Recipient delete should update mail_recipient.deleted_flag and mail_recipient.deleted_at.
- Do not physically delete mail records in the first MVP.

## 18. README Rules

README.md should explain how to run the project.

README.md should include:

1. Project introduction
2. Tech stack
3. Directory structure
4. Environment requirements
5. Database initialization
6. application.yml configuration
7. How to start the backend
8. Where to find API docs
9. Where to find database docs
10. Git branch rules

README.md is for project startup and presentation.

Detailed product scope should stay in docs/mvp.md.

Detailed database design should stay in docs/database.md.

Detailed API design should stay in docs/api.md.

## 19. Git Branch Rules

Use this branch model:

main
dev
feature/*

Branch meanings:

main:
Stable branch. Used for final delivery, demo, or stable versions.

dev:
Development integration branch. Feature branches should merge into dev first.

feature/*:
Feature development branches. Each feature should have its own branch.

Recommended feature branch names:

feature/user-login
feature/mail-send
feature/inbox-api
feature/mail-detail
feature/database-init
feature/api-docs

Do not develop directly on main.

Recommended workflow:

1. Start from dev.
2. Pull latest dev.
3. Create a feature branch.
4. Develop the feature.
5. Commit changes.
6. Push the feature branch.
7. Merge into dev after testing.
8. Merge dev into main only when the version is stable.

## 20. Commit Message Rules

Use this commit message format:

type(scope): message

Common types:

feat: new feature
fix: bug fix
docs: documentation
refactor: code refactoring
test: test-related changes
chore: build, config, or miscellaneous changes
style: code format changes

Examples:

feat(auth): implement login api
feat(mail): implement send mail api
feat(mail): implement inbox query
fix(mail): fix mail detail permission check
docs(api): update mail api document
docs(database): update mail table design
chore: update project config

Avoid meaningless commit messages, such as:

update
test
aaa
final
fix bug

## 21. Coding Rules

Follow these rules:

1. Keep code simple and readable.
2. Do not over-engineer the first MVP.
3. Keep Controller, Service, Mapper, Entity, DTO, and VO separated.
4. Do not put business logic directly inside Controller.
5. Do not randomly rename packages, classes, methods, or fields.
6. Do not change API paths without updating docs/api.md.
7. Do not change database fields without updating docs/database.md and sql/schema.sql.
8. Do not add unnecessary dependencies.
9. Do not hardcode database username, password, token secrets, or private keys.
10. Do not commit target, logs, local temporary files, or unrelated IDE files.
11. Prefer explicit and understandable code over clever code.
12. Add comments only when the logic is not obvious.

## 22. Files That Should Not Be Committed

Do not commit:

- target/
- .idea/ personal configuration
- *.iml if not required by the team
- *.log
- local temporary files
- local database dump files
- personal notes unrelated to the project
- generated compressed packages
- private passwords
- private tokens
- private API keys

Make sure .gitignore is properly configured.

Recommended .gitignore content:

target/
.idea/
*.iml
*.log
.DS_Store

## 23. Development Priority

Current priority order:

1. Keep docs/api.md as the highest-priority API contract.
2. Keep docs/mvp.md, docs/prd.md, docs/database.md, sql/schema.sql, README.md, and AGENTS.md aligned with docs/api.md.
3. Implement P0: user registration and login.
4. Implement P0: current user information.
5. Implement P0: send mail.
6. Implement P0: inbox query.
7. Implement P0: sent mail query.
8. Implement P0: mail detail with automatic read behavior.
9. Implement P0: explicit read status update.
10. Implement P0: recipient-side logical delete.
11. Use Apifox to test all P0 APIs.
12. Implement P1 features after P0 is stable: user settings, deleted list, spam mailbox, statistics, filters, and analysis fields.
13. Implement P2 features only if time allows.

Do not make AI model calls block or break the basic mail workflow.

## 24. Definition of Done

A backend task is done only when:

1. The code compiles successfully.
2. The feature matches docs/mvp.md.
3. The API matches docs/api.md.
4. The database matches docs/database.md and sql/schema.sql.
5. The feature can be tested through Apifox or a similar HTTP tool.
6. Related documents are updated if API or database design changed.
7. No unrelated files are modified.
8. No unnecessary dependencies are added.
9. No private credentials are exposed.
10. The implementation is understandable for a course project team.

## 25. How Codex Should Work

When receiving a task, Codex should:

1. Read AGENTS.md first.
2. Read docs/00-index.md if it exists.
3. Read docs/mvp.md to confirm whether the feature belongs to the first MVP.
4. Read docs/database.md and sql/schema.sql if database changes are involved.
5. Read docs/api.md if API changes are involved.
6. Make the smallest necessary change.
7. Avoid unrelated refactoring.
8. Explain what files were changed.
9. Explain how to test the result.

If a requirement is unclear, prefer the current MVP documents.

If a change would affect API or database design, update the corresponding docs.

## 26. Do Not Do

Do not do these things unless explicitly requested:

1. Do not rewrite the whole project structure.
2. Do not introduce microservices.
3. Do not introduce Spring Security before MVP authentication is stable.
4. Do not introduce Redis unless the task requires it.
5. Do not introduce AI SDKs unless the task is specifically about AI.
6. Do not implement WebSocket notification in the first MVP.
7. Do not change table names randomly.
8. Do not change API paths randomly.
9. Do not remove existing documents.
10. Do not delete SQL files.
11. Do not submit generated files.
12. Do not implement optional features before required MVP features.

## 27. Suggested Prompts for Future Codex Tasks

When implementing a feature, the user may give prompts like:

Please read AGENTS.md, docs/mvp.md, docs/database.md, and docs/api.md first. Then implement the user login API according to the current MVP. Do not modify unrelated modules.

Please implement the mail sending API according to docs/api.md and docs/database.md. Use the existing layered structure. Update documentation only if the API or database design changes.

Please check whether the current database schema matches docs/database.md. If not, fix sql/schema.sql and explain the differences.

Please help me create the basic Controller, Service, Mapper, Entity, DTO, and VO structure for the mail module. Follow the MVP scope and do not implement optional features.

## 28. Project Style

This project should be:

- clear
- maintainable
- suitable for a course assignment
- easy for teammates to understand
- easy for frontend integration
- friendly to Apifox testing
- ready for later AI extension

The first goal is not to make the most complex system.

The first goal is to make a clean, runnable, explainable MVP.
