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
│   ├── mysql/
│   │   └── init.sql  # 数据库初始化
│   └── nginx/
│       └── nginx.conf # Nginx 配置
├── .github/
│   └── workflows/
│       └── deploy.yml # GitHub Actions 部署
├── docker-compose.yml
├── deploy.sh         # 手动部署脚本
├── quick-deploy.sh   # 快速部署脚本
├── DEPLOYMENT.md     # 详细部署文档
└── .env              # 环境变量
```

## 部署指南

### 方式一：GitHub Actions 自动部署（推荐）

1. **配置 GitHub Secrets**

在 GitHub 仓库的 **Settings** -> **Secrets and variables** -> **Actions** 添加：

| Secret 名称 | 说明 |
|------------|------|
| `SSH_PRIVATE_KEY` | 服务器 SSH 私钥 |
| `DEPLOY_HOST` | 服务器 IP 或域名 |
| `DEPLOY_USER` | SSH 用户名 |

2. **获取 SSH 私钥**

```bash
ssh-keygen -t rsa -b 4096 -C "github-deploy" -f ~/.ssh/github_deploy_key
ssh-copy-id -i ~/.ssh/github_deploy_key.pub root@your-server-ip
```

3. **推送代码触发部署**

```bash
git add .
git commit -m "部署更新"
git push origin main
```

或手动触发：GitHub 仓库 -> Actions -> Deploy Blog System -> Run workflow

### 方式二：手动部署

```bash
# 1. 构建镜像
docker-compose build

# 2. 启动服务
docker-compose up -d

# 3. 查看日志
docker-compose logs -f server
```

详细部署指南请参考 [DEPLOYMENT.md](DEPLOYMENT.md)

## AI 配置

在 `blog-server/src/main/resources/application.yml` 中配置 AI 提供商：

```yaml
app:
  ai:
    provider: deepseek  # 可选: deepseek, qwen, wenxin
```

支持的 AI 提供商：
- DeepSeek
- 通义千问
- 文心一言

## API 文档

启动后端服务后访问：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API 文档: `http://localhost:8080/v3/api-docs`
