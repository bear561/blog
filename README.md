# Blog System

基于 Vue3 + Spring Boot 的全栈博客系统，集成 AI 助手。

## 技术栈

- **前端**: Vue 3 + Element Plus + Vite
- **后端**: Spring Boot 3 + MyBatis-Plus + MySQL + Redis
- **管理端**: Vue 3 + Element Plus
- **AI**: 支持 DeepSeek / 通义千问 / 文心一言
- **部署**: Docker + docker-compose

## 快速开始

### 1. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 文件，填入 AI API Key 等配置
```

### 2. 一键启动

```bash
docker-compose up -d
```

### 3. 访问

- 博客首页: http://localhost
- 管理后台: http://localhost/admin
- 默认管理员: admin / admin123

## 本地开发

### 后端

```bash
cd blog-server
mvn spring-boot:run
```

### 前端

```bash
cd blog-frontend
npm install
npm run dev
```

### 管理端

```bash
cd blog-admin
npm install
npm run dev
```

## 项目结构

```
blog/
├── blog-frontend/    # 用户端 Vue3 项目
├── blog-admin/       # 管理端 Vue3 项目
├── blog-server/      # Spring Boot 后端
├── docker/           # Docker 配置
├── docker-compose.yml
└── .env              # 环境变量
```
