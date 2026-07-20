# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack blog system with AI assistant. Monorepo with 3 modules: user-facing blog frontend, admin panel, and Spring Boot backend.

## Tech Stack

- **Frontend**: Vue 3 + Element Plus + Vite + Pinia + Vue Router
- **Admin**: Vue 3 + Element Plus + Vite + Pinia + Vue Router
- **Backend**: Spring Boot 3 + MyBatis-Plus + MySQL 8 + Redis
- **AI**: DeepSeek / 通义千问 / 文心一言 (pluggable adapter pattern)
- **Deploy**: Docker + docker-compose + Nginx

## Commands

### Backend (blog-server/)
```bash
# Run (requires MySQL & Redis on localhost)
cd blog-server && mvn spring-boot:run

# Build
mvn clean package -DskipTests

# Tests
mvn test

# Single test
mvn test -Dtest=ClassName
```

### Frontend (blog-frontend/)
```bash
cd blog-frontend && npm install && npm run dev      # dev server on :5173
cd blog-frontend && npm run build                     # production build
```

### Admin (blog-admin/)
```bash
cd blog-admin && npm install && npm run dev           # dev server on :5174
cd blog-admin && npm run build                        # production build
```

### Docker
```bash
docker-compose up -d              # start all services
docker-compose down               # stop all
docker-compose logs -f server     # follow backend logs
docker-compose restart server     # restart backend
```

## Architecture

### Backend Layers
```
controller/       REST controllers (public + admin/ sub-package)
service/          Business logic (impl/ for implementations, ai/ for AI adapters)
mapper/           MyBatis-Plus BaseMapper interfaces with @Select custom queries
entity/           DB entity classes mapped to t_* tables
dto/              Request body objects (with @NotBlank validation)
vo/               Response view objects
common/           Result<T>, PageResult<T>, BusinessException, AppConfig
config/           SecurityConfig (JWT filter), RedisConfig, CorsConfig, MyBatisPlusConfig
```

### API Convention
- All responses wrapped in `Result<T>`: `{ code: 200, message: "success", data: ... }`
- Public endpoints under `/api/` (no auth)
- Admin endpoints under `/api/admin/` (requires JWT Bearer token)
- JWT token obtained via `POST /api/auth/login`
- Paginated endpoints accept `page` (default 1) and `size` (default 10)

### Database
- All tables prefixed with `t_` (t_article, t_category, t_tag, t_comment, etc.)
- MyBatis-Plus handles single-table CRUD via `BaseMapper<T>`
- Complex queries use `@Select` annotations or XML in `resources/mapper/`
- Cache invalidation: article changes clear `article:*` Redis keys; category/tag changes clear respective caches

### AI Assistant Flow
1. `AIChatController` receives POST `/api/ai/chat` → returns SSE stream
2. `ArticleContextBuilder` searches articles via MySQL LIKE for relevant context
3. `AIService.chatStream()` calls LLM API with context-augmented system prompt
4. SSE events: `message` (text chunks), `error`, `done` (with sessionId)
5. Provider configured via `app.ai.provider` in application.yml; `@ConditionalOnProperty` selects implementation

### Redis Cache Keys
| Pattern | TTL | Invalidated by |
|---|---|---|
| `article:list:*` | 10min | Article create/update/delete |
| `article:detail:{id}` | 30min | Article update |
| `article:hot` | 1h | Scheduled refresh |
| `category:all` | 1h | Category CRUD |
| `tag:all` | 1h | Tag CRUD |
| `friend:links` | 30min | Link CRUD |
| `site:config` | 30min | Config update |
| `rate:comment:{ip}` | 1min window | Automatic expiry |

### Frontend State Management
- `stores/app.js` — site config (name, description)
- `stores/auth.js` (admin only) — login state, JWT token
- `stores/ai.js` — chat messages, session ID

## Key Conventions
- Use MyBatis-Plus `LambdaQueryWrapper` for type-safe queries, not string-based conditions
- Use `@Cacheable`/`@CacheEvict` annotations for Redis caching
- Admin controllers go in `controller/admin/` package
- All DTOs use `jakarta.validation` annotations (`@NotBlank`, `@NotNull`)
- Date format: `yyyy-MM-dd HH:mm` for display, ISO for API
- Vue uses `<script setup>` syntax, Element Plus components prefixed with `el-`
