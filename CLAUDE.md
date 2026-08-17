# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

[中文](#中文) | [English](#english)

<a id="中文"></a>
# 中文

## 项目概览

HYPBlog 是一个前后端分离的个人博客系统，由三个可独立运行的应用组成：

- `blog-api`：运行在 `8090` 端口的 Java 8 / Spring Boot 2.2.7 REST API，使用 MyBatis XML Mapper 访问 MySQL，以 Redis 提供缓存和计数器，使用 Spring Security + JWT 保护管理端，并集成 Quartz、邮件、Telegram 和文件上传。
- `blog-cms`：Vue 2 + Element UI 后台管理单页应用。开发服务器默认端口为 `8079`，请求受认证保护的 `/admin` API。
- `blog-view`：Vue 2 前台单页应用，面向博客访客，调用公共 API。

## 常用命令

除非另有说明，以下命令在对应子项目目录中执行。

```bash
# 后端：编译并打包，跳过测试
cd blog-api && mvn package -DskipTests

# 后端：使用 application.properties 中启用的 Spring Profile 启动
cd blog-api && mvn spring-boot:run

# 后端：运行全部测试（当前主要为 Spring 上下文冒烟测试）
cd blog-api && mvn test

# 后端：运行单个 JUnit 类或方法
cd blog-api && mvn -Dtest=BlogApiApplicationTests test
cd blog-api && mvn -Dtest=BlogApiApplicationTests#contextLoads test

# 后台或前台：按锁文件安装依赖
cd blog-cms && npm ci
cd blog-view && npm ci

# 后台或前台：启动开发服务器
cd blog-cms && npm run serve
cd blog-view && npm run serve

# 后台或前台：构建生产包
cd blog-cms && npm run build
cd blog-view && npm run build
```

两个前端项目均未定义独立的测试或 lint 命令。`blog-cms` 在开发环境启用了 Vue CLI 的 `lintOnSave`；使用 `npm run build` 验证各前端的生产构建。

## 本地开发依赖

默认启用 `blog-api/src/main/resources/application.properties` 中指定的 `dev` Spring Profile。API 依赖：

- 按数据源配置可访问的 MySQL；使用 `blog-api/nblog.sql` 初始化。表结构和种子数据要求数据库使用 `utf8mb4`。
- 按配置地址运行的 Redis。应用启动时会初始化文章浏览量，Redis 也承担公共页面缓存和访客跟踪。
- `upload.file.path` 指定的本地上传目录；`WebConfig` 通过 `upload.file.access-path` 将其暴露为静态资源。

开发环境 API 地址由 `blog-view/.env.development` 和 `blog-cms/.env.development` 的 `VUE_APP_API_BASE_URL` 配置。CMS 的地址必须以 `/admin` 结尾；`blog-cms/src/util/request.js` 会从本地存储附加 JWT。前台的 `blog-view/src/plugins/axios.js` 会保存服务端返回的访客 `identification` 响应头。

`application-dev.properties` 包含本机路径和第三方集成配置，不应视为可移植的默认值。生产环境的 URL 和部署基础路径应在对应前端 `.env.production` 及 Vue 配置中调整。

## 架构

### 后端请求与持久化流程

`top.hyp.controller` 中的控制器提供前台公共 API；`top.hyp.controller.admin` 中的控制器提供以 `/admin` 为根路径的 CMS API。控制器统一使用 `model.vo.Result` 返回 `{ code, msg, data }` 响应体。

业务代码遵循 `controller -> service -> mapper -> MyBatis XML`：

- Mapper Java 接口位于 `top.hyp.mapper`。
- SQL、结果映射和关联查询位于 `src/main/resources/mapper/*.xml`；新增持久化操作时必须同时更新接口和 XML。
- Entity 对应持久化模型，`model.dto` 表示请求和写入对象，`model.vo` 表示对外响应对象。
- 控制器分页使用 PageHelper。

博客是核心跨模块领域。已发布文章使用 `blog.status = 'FINISHED'`；公共 Mapper 查询必须筛选已发布且已完成的文章。草稿与文章共用 `blog` 表，使用 `status = 'DRAFT'`，并通过独立的 `/admin/drafts` 和 `/admin/draft` 接口管理；草稿不得出现在公共查询中。文章与标签的关系存储于 `blog_tag`，新增、更新、删除文章或草稿时必须保持关联一致。

### 缓存与定时数据

Service 实现层负责缓存失效，`RedisKeyConstants` 定义共享键名。`BlogServiceImpl` 缓存首页文章、归档、推荐文章，并通过 Redis Hash 保存浏览量。任何影响公共文章内容的写操作都必须清理相应缓存；直接修改数据库可能导致 Redis 缓存过期前数据不一致。

`schedule_job` 表驱动 Quartz 任务。初始任务会将 Redis 文章浏览量同步至 MySQL，并将访客数据写入持久化记录。任务类和调度工具位于 `top.hyp.task` 与 `top.hyp.util.quartz`。

### 安全、日志与访客标识

`SecurityConfig` 使用 JWT 保护 `/admin/**`：GET 请求允许 `ROLE_admin` 与 `ROLE_visitor`，其他管理端请求要求 `ROLE_admin`。`JwtLoginFilter` 处理 `/admin/login`，`JwtFilter` 处理后续请求。CMS 路由也会在渲染前检查本地存储的 Token。

公共接口无需登录。`VisitLogger` 和 `OperationLogger` 注解会触发 AOP 切面，分别写入访问和操作记录。`WebConfig` 全局注册访问限流拦截器。调整需要纳入统计或审计的接口时，应保留这些注解。

### 前端

两个前端均使用 Vue Router 的 history 模式及 Vuex 状态管理。API 模块集中在各应用的 `src/api` 下，视图应调用这些模块，不应在组件内临时创建 axios 实例。

- `blog-view` 实现首页、文章详情、归档、分类/标签列表、动态、友链、关于页、评论、搜索、受密码保护文章访问和博主登录。文章 Markdown 在服务端转换为 HTML，客户端展示前会进行清理。
- `blog-cms` 提供仪表盘、文章与草稿编辑、分类、标签、动态、评论、站点/关于/友链设置、图床、账户、Quartz 任务、操作/登录/异常/访问日志和访客统计。

## 项目约定

- `blog-api/nblog.sql` 是基础表结构与种子数据。若需升级已部署实例，应在 `docs/sql/` 添加正向和回滚迁移脚本，而不是只修改初始化 SQL。
- 多处写入路径假定为单作者博客：新文章和草稿会关联 `user.id = 1`。
- Markdown 通过 `MarkdownUtils` 在服务端转换；公共博客 Service 在返回文章正文或摘要前执行转换。
- 当前工作区已有未提交功能修改和开发日志。保留无关改动，提交时不要包含生成的 `*.out.log` 或 `*.err.log` 文件。

[返回顶部](#claudemd) | [English](#english)

<a id="english"></a>
# English

## Project Overview

HYPBlog is a separated personal blog system composed of three independently run applications:

- `blog-api`: Java 8 / Spring Boot 2.2.7 REST API on port `8090`. It uses MyBatis XML mappers with MySQL, Redis for caches and counters, Spring Security with JWT for administration, Quartz for persisted jobs, and mail/Telegram/upload integrations.
- `blog-cms`: Vue 2 + Element UI administration SPA. Its development server defaults to port `8079` and sends authenticated API requests to `/admin`.
- `blog-view`: Vue 2 public-facing SPA that calls the public API.

## Commands

Run commands from the named application directory unless stated otherwise.

```bash
# Backend: compile/package without running tests
cd blog-api && mvn package -DskipTests

# Backend: run the application with the active Spring profile from application.properties
cd blog-api && mvn spring-boot:run

# Backend: run all tests (currently a Spring context smoke test)
cd blog-api && mvn test

# Backend: run one JUnit class or one test method
cd blog-api && mvn -Dtest=BlogApiApplicationTests test
cd blog-api && mvn -Dtest=BlogApiApplicationTests#contextLoads test

# CMS or public site: install locked dependencies
cd blog-cms && npm ci
cd blog-view && npm ci

# CMS or public site: start development server
cd blog-cms && npm run serve
cd blog-view && npm run serve

# CMS or public site: production build
cd blog-cms && npm run build
cd blog-view && npm run build
```

Neither frontend package defines a standalone test or lint script. Vue CLI performs development-time linting in `blog-cms` because `lintOnSave` is enabled there; use `npm run build` to verify each frontend production bundle.

## Local Development Dependencies

The default `dev` Spring profile is selected in `blog-api/src/main/resources/application.properties`. The API expects:

- MySQL reachable at the configured datasource; initialize it from `blog-api/nblog.sql`. The schema and seed data require `utf8mb4`.
- Redis at the configured host and port. The API initializes article-view counters in Redis at startup and uses it broadly for public-page caches and visitor tracking.
- Local upload storage at the configured `upload.file.path`; `WebConfig` exposes it through `upload.file.access-path`.

The development frontend endpoints are configured with `VUE_APP_API_BASE_URL` in `blog-view/.env.development` and `blog-cms/.env.development`. Keep the CMS base URL ending in `/admin`; `blog-cms/src/util/request.js` attaches the JWT from local storage. The public client in `blog-view/src/plugins/axios.js` persists the server-issued visitor `identification` header.

`application-dev.properties` is environment-specific and contains integration credentials and local paths. Do not treat its values as portable defaults. Production URL and base-path changes belong in the corresponding frontend `.env.production` files and Vue configuration.

## Architecture

### Backend Request and Persistence Flow

Public controllers in `top.hyp.controller` provide the public site API, while controllers under `top.hyp.controller.admin` are the CMS API and are rooted at `/admin`. Controllers return the common `{ code, msg, data }` envelope from `model.vo.Result`.

Business code follows `controller -> service -> mapper -> MyBatis XML`:

- Java mapper interfaces are in `top.hyp.mapper`.
- SQL, result maps, and joins live in `src/main/resources/mapper/*.xml`; update both the interface and XML when adding persistence operations.
- Entities model persisted rows; `model.dto` is used for request/write shapes and `model.vo` for public response shapes.
- PageHelper is used for controller-driven pagination.

The blog domain is the central cross-cutting feature. Published articles use `blog.status = 'FINISHED'`; public mapper queries must filter to published, finished entries. Drafts share the `blog` table with articles, use `status = 'DRAFT'`, and have separate `/admin/drafts` and `/admin/draft` endpoints; drafts must not be returned by public queries. Article/tag relations are maintained in `blog_tag`; create, update, delete, and draft operations must keep that relation consistent.

### Caching and Scheduled Data

Service implementations own cache invalidation. `RedisKeyConstants` names the shared keys. `BlogServiceImpl` caches home lists, archives, recommended articles, and maintains view counts in a Redis hash. Writes that affect public blog content must invalidate the relevant caches; direct database changes can leave Redis stale until its keys are cleared or the application restarts.

The `schedule_job` database table drives Quartz jobs. The seeded jobs synchronize Redis article views to MySQL and roll visitor data into persistent records. Job classes and scheduling utilities are under `top.hyp.task` and `top.hyp.util.quartz`.

### Security, Logging, and Public Visitor Identity

`SecurityConfig` makes `/admin/**` JWT-protected: GET permits `ROLE_admin` and `ROLE_visitor`, while non-GET administration endpoints require `ROLE_admin`. `JwtLoginFilter` handles `/admin/login`, and `JwtFilter` processes subsequent requests. CMS routes independently require a local-storage token before rendering.

Public endpoints are open. `VisitLogger` and `OperationLogger` annotations trigger AOP aspects that write visit and operation records. The access-limit interceptor is registered globally by `WebConfig`. Preserve these annotations when altering endpoints that should remain represented in analytics or administration audit logs.

### Frontends

Both frontend apps use Vue Router in history mode and Vuex for shared state. API modules are grouped under each app's `src/api`; views should call those modules rather than creating axios instances ad hoc.

- `blog-view` renders the public homepage, articles, archives, category/tag listings, moments, friends, about page, comments, search, password-protected article access, and author login. Public article content arrives as server-rendered Markdown HTML and is sanitized before display.
- `blog-cms` is the operational UI for dashboard data, article and draft editing, categories, tags, moments, comments, site/about/friend settings, image hosting, user account, Quartz jobs, operational/login/exception/visit logs, and visitor statistics.

## Repository-Specific Notes

- `blog-api/nblog.sql` is the base schema plus seed data. Add forward and rollback migration scripts under `docs/sql/` for changes intended to update existing installations; do not rely only on editing the seed SQL.
- The application is a single-author blog in several write paths: new articles and drafts associate `user.id = 1`.
- Markdown conversion happens server-side through `MarkdownUtils`; public blog services convert article content or descriptions before returning them.
- The repository currently has uncommitted feature work and generated development logs. Preserve unrelated working-tree changes and do not add generated `*.out.log` or `*.err.log` files to commits.

[Back to top](#claudemd) | [中文](#中文)
