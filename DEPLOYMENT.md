# 博客系统部署指南

## 目录
- [环境要求](#环境要求)
- [服务器准备](#服务器准备)
- [本地构建](#本地构建)
- [手动部署](#手动部署)
- [自动部署 (GitHub Actions)](#自动部署-github-actions)
- [验证部署](#验证部署)

---

## 环境要求

### 服务器要求
- **操作系统**: Ubuntu 20.04+ / CentOS 7+
- **CPU**: 2核+
- **内存**: 2GB+
- **硬盘**: 20GB+
- **网络**: 公网IP，80/443端口开放

### 必需软件
- Docker 20.10+
- Docker Compose 1.29+
- Git
- Nginx (可选，也可以用Docker)

---

## 服务器准备

### 1. 安装 Docker 和 Docker Compose

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker
```

### 2. 配置防火墙

```bash
# Ubuntu/Debian
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 3. 克隆项目

```bash
# 创建部署目录
sudo mkdir -p /var/www
cd /var/www

# 克隆项目（确保使用 SSH 方式）
sudo git clone git@github.com:your-username/your-repo.git blog
sudo chown -R $USER:$USER blog
cd blog
```

---

## 本地构建

### 1. 安装 Node.js

```bash
# Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# CentOS/RHEL
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
sudo yum install -y nodejs
```

### 2. 安装依赖

```bash
# 安装依赖
cd blog-frontend && npm install
cd ../blog-admin && npm install
cd ../blog-server && mvn clean package -DskipTests
```

### 3. 构建前端和后台

```bash
cd blog-frontend && npm run build
cd ../blog-admin && npm run build
```

---

## 手动部署

### 1. 构建镜像

```bash
# 使用 Docker Compose 构建
docker-compose build
```

### 2. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f server
```

### 3. 初始化数据库

首次部署需要初始化数据库：

```bash
# 进入 MySQL 容器
docker exec -it blog-mysql bash

# 执行初始化脚本
mysql -uroot -proot123 < /docker-entrypoint-initdb.d/init.sql

# 退出容器
exit

# 重启 MySQL 容器确保配置生效
docker-compose restart mysql
```

### 4. 配置 Nginx（可选）

如果需要在服务器上直接使用 Nginx，可以配置反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 后台管理
    location /admin {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 自动部署 (GitHub Actions)

### 1. 配置 GitHub Secrets

在 GitHub 仓库的 **Settings** -> **Secrets and variables** -> **Actions** 中添加以下 Secrets：

| Secret 名称 | 说明 | 示例值 |
|------------|------|--------|
| `SSH_PRIVATE_KEY` | 服务器 SSH 私钥 | `-----BEGIN RSA PRIVATE KEY-----...` |
| `SERVER_HOST` | 服务器 IP 或域名 | `123.45.67.89` |
| `SERVER_USER` | SSH 用户名 | `root` 或 `ubuntu` |

### 2. 获取 SSH 私钥

```bash
# 在本地生成 SSH 密钥对
ssh-keygen -t rsa -b 4096 -C "github-deploy" -f ~/.ssh/github_actions

# 复制公钥到服务器
ssh-copy-id -i ~/.ssh/github_actions.pub root@your-server-ip

# 复制私钥内容到 GitHub Secrets
cat ~/.ssh/github_actions
```

### 3. 配置 GitHub Workflow

编辑 `.github/workflows/deploy.yml` 文件：

```yaml
name: Deploy Blog System

on:
  push:
    branches:
      - main  # 推送到 main 分支时自动部署
  workflow_dispatch:  # 手动触发部署

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup SSH key
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.SSH_PRIVATE_KEY }}" > ~/.ssh/deploy_key
          chmod 600 ~/.ssh/deploy_key
          ssh-keyscan -H ${{ secrets.SERVER_HOST }} >> ~/.ssh/known_hosts

      - name: Deploy to server
        env:
          SERVER_USER: ${{ secrets.SERVER_USER }}
          SERVER_HOST: ${{ secrets.SERVER_HOST }}
          DEPLOY_DIR: /var/www/blog
          SSH_PRIVATE_KEY: ${{ secrets.SSH_PRIVATE_KEY }}
        run: |
          echo "开始部署到服务器..."

          # 拉取最新代码
          ssh -i ~/.ssh/deploy_key -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_HOST} \
            "cd ${DEPLOY_DIR} && git pull origin main"

          # 构建镜像
          ssh -i ~/.ssh/deploy_key ${SERVER_USER}@${SERVER_HOST} \
            "cd ${DEPLOY_DIR} && docker-compose build"

          # 停止旧容器
          ssh -i ~/.ssh/deploy_key ${SERVER_USER}@${SERVER_HOST} \
            "cd ${DEPLOY_DIR} && docker-compose down"

          # 启动新容器
          ssh -i ~/.ssh/deploy_key ${SERVER_USER}@${SERVER_HOST} \
            "cd ${DEPLOY_DIR} && docker-compose up -d"

          echo "部署完成！"

      - name: Verify deployment
        run: |
          echo "验证部署状态..."
          sleep 10
          curl -f ${{ secrets.SERVER_HOST }} || exit 1
          echo "验证成功！"
```

### 4. 触发部署

#### 自动部署
- 推送代码到 `main` 分支，GitHub Actions 会自动触发部署

#### 手动部署
- 进入 GitHub 仓库的 **Actions** 标签
- 选择 "Deploy Blog System" workflow
- 点击 **Run workflow** 按钮手动触发

---

## 验证部署

### 1. 检查服务状态

```bash
# 查看所有容器状态
docker-compose ps

# 检查日志
docker-compose logs -f server
```

### 2. 访问应用

- **前端**: http://your-server-ip
- **后台管理**: http://your-server-ip/admin
- **API**: http://your-server-ip/api

### 3. 测试关键功能

- [ ] 访问前端页面
- [ ] 注册/登录后台
- [ ] 发布文章
- [ ] 检查数据库连接
- [ ] 检查 Redis 连接
- [ ] 查看服务器日志

---

## 常见问题

### 1. 端口被占用

```bash
# 查看端口占用
sudo netstat -tulpn | grep :8080

# 停止占用端口的进程
sudo kill -9 <PID>
```

### 2. Docker 容器无法启动

```bash
# 查看容器日志
docker-compose logs <service-name>

# 重启服务
docker-compose restart <service-name>

# 重新构建并启动
docker-compose up -d --build
```

### 3. 数据库连接失败

```bash
# 检查 MySQL 容器状态
docker-compose ps mysql

# 进入 MySQL 容器
docker exec -it blog-mysql bash
mysql -uroot -proot123

# 检查用户权限
SHOW GRANTS FOR 'blog'@'%';
```

### 4. 权限问题

```bash
# 修改文件权限
sudo chown -R $USER:$USER /var/www/blog
sudo chmod -R 755 /var/www/blog
```

---

## 备份与恢复

### 数据库备份

```bash
# 备份数据库
docker exec blog-mysql mysqldump -uroot -proot123 blog > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker exec -i blog-mysql mysql -uroot -proot123 blog < backup_20240101.sql
```

### 容器备份

```bash
# 导出容器镜像
docker save blog-frontend:latest blog-admin:latest blog-server:latest > blog-images.tar

# 导入镜像
docker load < blog-images.tar
```

---

## 更新日志

### v1.0.0 (2024-01-01)
- ✅ 基础功能部署
- ✅ 前端 + 后台 + API
- ✅ MySQL + Redis
- ✅ GitHub Actions 自动部署

---

## 支持

如有问题，请提交 Issue 或联系维护者。
