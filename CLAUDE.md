# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack blog system with an AI assistant. Monorepo with 3 modules:
- **blog-frontend/** — user-facing blog (Vue 3 + Element Plus + Vite + Pinia + Vue Router)
- **blog-admin/** — admin panel (same stack, served under `/admin`, served from its own `dist`)
- **blog-server/** — Spring Boot 3 backend (Java 17, MyBatis-Plus, MySQL 8, Redis, langchain4j for AI)

Deploy: Docker Compose to a Tencent Cloud host (blog.xiongjie.icu) via GitHub Actions on push to `main`.

## Commands

### Backend (blog-server/, target Java 17, local JDK 21 works)
```bash
# Zero-dependency dev: H2 file DB + no Redis (recommended)
cd blog-server && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Full-stack local dev: needs MySQL + Redis from docker-compose
docker compose up -d mysql redis
cd blog-server && mvn spring-boot:run

# Build / tests
mvn clean package -DskipTests        # bound build (used by Dockerfile)
mvn test
mvn test -Dtest=ClassName            # single test
```
Profiles (`application.yml` is the base, active profile picks up the rest):
- `local` — H2 embedded, cache off, Redis excluded, `spring.sql.init.mode=always` (runs `schema.sql`). AI keys are hardcoded in `application-local.yml` for dev convenience.
- `prod` — every secret comes from env vars (`DB_URL`, `REDIS_PASSWORD`, `JWT_SECRET`, `AI_API_KEY`, ...), logging silent, `sql.init.mode=never`.
- Running without `-Dspring-boot.run.profiles=local` boots H2 in-memory demo data; MySQL/Redis-backed prod runs are driven by environment/docker-compose, not profiles.

### Frontends
```bash
cd blog-frontend && npm install && npm run dev     # :5173  (user site)
cd blog-frontend && npm run build                   # output to dist/ (mounted by nginx)

cd blog-admin && npm install && npm run dev         # :5174  (admin panel)
cd blog-admin && npm run build
```

### Docker / deploy
```bash
docker compose up -d                 # mysql + redis + server + nginx (compose v2; v1 is dead)
docker compose up -d --build server  # rebuild backend image only
docker compose logs -f server
docker compose ps
```
Secrets come from the repo-root `.env` (gitignored; compose interpolates it). Pushing to `main` triggers `.github/workflows/deploy.yml` → rsync/syncs to `/var/www/blog` on the server and rebuilds. Workflow dispatch supports `skip_build` / `skip_git_sync` overrides.

## Architecture

### Backend layers (com.blog.*)
```
controller/       REST controllers (public + admin/ sub-package)
service/          Business logic
  AiChatService.java          SSE chat orchestration (langchain4j)
  ai/BlogAiAssistant.java     langchain4j @SystemMessage assistant interface
  ai/ArticleContextBuilder.java  MySQL LIKE RAG context builder
  ai/BlogTools.java           AI-callable tools (@Tool methods)
mapper/           MyBatis-Plus BaseMapper + @Select custom queries
entity/           DB entities mapped to t_* tables
dto/              Request bodies (jakarta.validation annotations)
vo/               Response view objects
common/           Result<T>, PageResult<T>, BusinessException, AppConfig
config/           SecurityConfig (JWT filter), AiConfig, RedisConfig, CorsConfig, MyBatisPlusConfig, JwtAuthFilter
```

### Frontend dual layout
`App.vue` picks layout via `useDeviceMode()` (`composables/useDeviceMode.js`, module singleton, max-width 900px / coarse-pointer). Desktop views in `views/desktop/`, mobile views in `views/mobile/`, mobile-only widgets in `components/mobile/`. **Any new page must ship both variants** (route tables differ per layout).

### API convention
- All responses wrapped in `Result<T>`: `{ code: 200, message: "success", data: ... }`
- Public under `/api/`; admin under `/api/admin/` (JWT Bearer from `POST /api/auth/login`)
- Pagination: `page` (default 1), `size` (default 10)
- `ArticleListVO` deliberately omits `contentHtml` (keeps list payloads light). If a list card needs a derived field (read minutes etc.), compute it **server-side in `toArticleListVO`/`toArticleVO`** and ship it as a VO field — don't add contentHtml to the list VO.

### Databases & schema — TWO mirrored schema files
| File | Dialect | When it runs |
|---|---|---|
| `blog-server/src/main/resources/schema.sql` | H2 (`MERGE INTO ... KEY(id)`, `CLOB`) | `local` profile, `sql.init.mode=always` |
| `docker/mysql/init.sql` | MySQL (`INSERT`, `TEXT`) | **only** first-ever init of an empty MySQL volume |

**Any table/column/seed change must be mirrored in BOTH files** (same seed rows: admin user, 2 categories, site config KV). `init.sql` does NOT re-run on existing volumes — migrate live MySQL by hand.
Important: MySQL `TIMESTAMP` columns are timezone-converting; H2 `TIMESTAMP` is not.

### Timezone invariant
All layers must agree on **Asia/Shanghai** (no DST in China):
- JVM: Dockerfile `ENV TZ=Asia/Shanghai` + `-Duser.timezone=Asia/Shanghai`
- MySQL: container `TZ=Asia/Shanghai` + `--default-time-zone=+08:00`
- JDBC URL: `serverTimezone=Asia/Shanghai` — must match MySQL's session zone, or `LocalDateTime` wall times drift by 8h.
Entities store **naive local wall time** (`LocalDateTime` + `@JsonFormat("yyyy-MM-dd HH:mm:ss")`); frontend renders as-is. LocalDateTime serialization has no zone, so `spring.jackson.time-zone` is irrelevant.

### Site config = key-value store
`t_site_config` holds arbitrary `config_key → config_value` rows; frontend store reads them (`site_name`, `site_description`, `about_content`, `site_avatar`, `icp_number`, ...). To add a new site-level string: add the key to both schema files' seeds, read it in `stores/app.js`, render conditionally. Admin `SiteConfig.vue` edits rows dynamically and evicts the `site:config` cache via `@CacheEvict`. `site_avatar` is special-cased (avatar card) — don't add other "config" that needs special editing UI here.

## AI Assistant Flow (langchain4j, SSE)
1. `POST /api/ai/chat` → `AiChatService.chat()` returns `SseEmitter` (300s timeout)
2. `ArticleContextBuilder` does a naive keyword split + MySQL `LIKE` on title/summary, prepends up to 3 matched articles (≤2000 chars each) to the user message
3. `BlogAiAssistant.chat()` streams via langchain4j `TokenStream` with `MessageWindowChatMemory` (10 msgs) and `BlogTools` as tools
4. SSE events: `message` (text chunks), `done` (JSON w/ `sessionId`), `error`
5. Conversation persisted to `t_ai_conversation` on completion (tokens from `TokenStream` response)
6. Provider = `ai.api-key` / `ai.model` / `ai.base-url` in `application.yml` (any OpenAI-compatible endpoint: DeepSeek default, Qwen/Wenxin via custom base-url). Model wired in `AiConfig`.

## Redis Cache Keys
> 普通缓存 TTL 统一 30min（`RedisConfig.cacheManager` entryTtl），仅 rate 限流键用不同 TTL。
| Pattern | TTL | Invalidated by |
|---|---|---|
| `article:list:{query}` | 30min | Admin article create/update/delete/publish |
| `article:detail:{id}` | 30min | Admin article update/delete/publish |
| `article:hot:{limit}` | 30min | Admin article create/update/delete/publish |
| `article:archive` | 30min | Admin article create/update/delete/publish |
| `category:all` | 30min | Category CRUD |
| `tag:all` | 30min | Tag CRUD |
| `friend:links` | 30min | Link CRUD |
| `site:config` | 30min | Config update |
| `rate:comment:{ip}` | 1min window | Automatic expiry |

Note: after a backend deploy that changes VO shape, stale cached JSON (missing new fields) persists up to 30min — trigger a cache evict by editing + re-publishing an article.

## Frontend State Management
- `stores/app.js` — site config KV (site name/desc/about/avatar/icp), loaded once in `App.vue`
- `stores/ai.js` — chat messages + session id (blog-frontend)
- `stores/auth.js` — admin only: JWT token, login state; admin router guards via `meta.requiresAuth` in `blog-admin/src/router/index.js`
- Reading-time: server ships `readMinutes` on detail & list VOs; `utils/readingTime.js` `readingMinutesFrom()` prefers it, falls back to local estimate. Keep both in sync with `ArticleService.estimateReadMinutes` (strip tags → chars/350 → min 1).

## Key Conventions
- MyBatis-Plus `LambdaQueryWrapper` for type-safe queries, not string conditions
- `@Cacheable`/`@CacheEvict` annotations for Redis caching (see keys above)
- Admin controllers in `controller/admin/` package
- DTOs use `jakarta.validation` (`@NotBlank`, `@NotNull`)
- Date display format `yyyy-MM-dd HH:mm`, API uses ISO via `@JsonFormat`
- Vue `<script setup>`, Element Plus components `el-*`
- Markdown rendering: flexmark with TablesExtension (`ArticleService` static parser), `content_html` persisted on write/publish