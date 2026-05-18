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

The first MVP should complete the basic mail system workflow.

Required MVP features:

1. User registration
2. User login
3. Get current logged-in user information
4. Send mail
5. Query inbox
6. Query sent mails
7. Query mail detail
8. Mark mail as read
9. Delete mail logically

The MVP should prove this workflow:

A user can register and log in.
A logged-in user can send a mail to another existing user.
The recipient can see the mail in the inbox.
The sender can see the mail in the sent list.
The recipient or sender can view mail detail.
The recipient can mark the mail as read.
The recipient can delete the mail from the inbox logically.

## 6. Features Not Required in the First MVP

Do not implement the following features in the first MVP unless explicitly requested:

1. AI mail summary
2. AI auto reply
3. AI mail classification
4. Semantic mail search
5. WebSocket real-time notification
6. Complex admin management
7. Complex role-based permission system
8. Full Spring Security integration
9. Attachment upload
10. Mail recall
11. Mail group sending
12. Complex trash recovery
13. Complex folder system

These features can be reserved as future extension points, but should not block the first MVP.

## 7. Optional Features

The following features are optional and should only be implemented after the required MVP is stable:

1. Mail search by keyword
2. Draft mail
3. Multiple recipients
4. CC
5. BCC
6. Attachments
7. Starred mails
8. Trash box
9. Mail labels
10. AI-related features

If time is limited, prioritize required MVP features over optional features.

## 8. AI Extension Principle

The team wants to reserve space for AI features because AI may help improve the final score.

However, AI should not be forced into the first backend MVP.

The first version should reserve clean extension points.

Possible future AI features:

1. AI mail summary
2. AI reply suggestion
3. AI mail classification
4. AI spam detection
5. Semantic mail search
6. RAG-based mail knowledge search

Do not write AI logic directly into the basic mail sending process.

If AI is added later, prefer a separate module or service layer, for example:

- ai
- ai.service
- mail.ai
- summary
- semantic.search

The basic mail module should still work even if AI features are disabled.

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
2. mail_message
3. mail_recipient

Table responsibilities:

sys_user:
Stores user account information.

mail_message:
Stores the main body of the mail, including sender, subject, content, and send time.

mail_recipient:
Stores recipient-specific information, including recipient id, read status, read time, recipient-side deleted status, and recipient type.

Important rule:

Do not put recipient-specific read status directly into mail_message.

Reason:

One mail may have multiple recipients in the future. Each recipient may have a different read/unread status and deleted status.

Therefore:

mail_message stores the shared mail body.
mail_recipient stores each recipient's personal mail status.

## 12. Recommended Core Tables

The first MVP should use these tables as the base design.

### sys_user

Purpose:

Store user information.

Recommended fields:

- id
- username
- password
- nickname
- email
- status
- created_at
- updated_at
- deleted

Rules:

- username should be unique.
- password should not be stored in plain text in the final version.
- deleted should be used for logical deletion.
- status can represent normal or disabled users.

### mail_message

Purpose:

Store mail body and sender information.

Recommended fields:

- id
- sender_id
- subject
- content
- send_time
- status
- created_at
- updated_at
- deleted

Rules:

- sender_id references sys_user.id.
- subject should not be empty.
- content should not be empty.
- send_time records the actual sending time.
- deleted can be used for sender-side logical deletion if needed.

### mail_recipient

Purpose:

Store the relation between mail and recipient.

Recommended fields:

- id
- mail_id
- recipient_id
- recipient_type
- read_status
- read_time
- deleted
- created_at
- updated_at

Rules:

- mail_id references mail_message.id.
- recipient_id references sys_user.id.
- read_status: 0 means unread, 1 means read.
- deleted: 0 means not deleted, 1 means deleted by recipient.
- recipient_type can reserve space for normal recipient, CC, and BCC.

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
GET    /api/users/me
POST   /api/mails
GET    /api/mails/inbox
GET    /api/mails/sent
GET    /api/mails/{mailId}
PATCH  /api/mails/{mailId}/read
DELETE /api/mails/{mailId}

Follow docs/api.md as the final API source of truth.

Do not randomly change request paths, request fields, or response fields.

If an API changes, update docs/api.md.

## 15. Unified Response Format

All backend APIs should return a unified response format.

Recommended format:

{
  "code": 200,
  "message": "success",
  "data": {}
}

Recommended status codes:

200: success
400: bad request or invalid parameter
401: not logged in or invalid token
403: no permission
404: resource not found
500: internal server error

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
- Deleted recipient records should not appear in the normal inbox.

Sent mail query:

- A user can only query mails where mail_message.sender_id equals current user id.

Mark as read:

- Only the recipient can mark a received mail as read.
- The sender should not mark recipient read status.

Delete mail:

- First MVP should use logical deletion.
- Recipient delete should update mail_recipient.deleted.
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

1. Complete docs/mvp.md
2. Complete docs/database.md
3. Complete sql/schema.sql
4. Complete docs/api.md
5. Complete README.md
6. Complete docs/git-workflow.md
7. Implement user registration and login
8. Implement mail sending
9. Implement inbox query
10. Implement sent mail query
11. Implement mail detail
12. Implement read status
13. Implement logical delete
14. Use Apifox to test all core APIs

Do not jump to AI features before the basic mail workflow works.

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