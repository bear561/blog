#!/bin/bash
set -e

echo "========================================="
echo "开始部署博客系统"
echo "========================================="

# 配置
SERVER_USER="root"
SERVER_HOST="your-server-ip"
DEPLOY_DIR="/var/www/blog"
SSH_KEY="${HOME}/.ssh/github_actions"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 1. 构建项目
log_info "正在构建前端和后台..."
docker-compose build

# 2. 检查 SSH 连接
log_info "检查服务器连接..."
ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_HOST} "echo '连接成功'"

# 3. 拉取最新代码
log_info "拉取最新代码..."
ssh -i "$SSH_KEY" ${SERVER_USER}@${SERVER_HOST} "cd $DEPLOY_DIR && git pull origin main"

# 4. 构建镜像
log_info "构建 Docker 镜像..."
ssh -i "$SSH_KEY" ${SERVER_USER}@${SERVER_HOST} "cd $DEPLOY_DIR && docker-compose build"

# 5. 停止旧容器
log_info "停止旧容器..."
ssh -i "$SSH_KEY" ${SERVER_USER}@${SERVER_HOST} "cd $DEPLOY_DIR && docker-compose down"

# 6. 启动新容器
log_info "启动新容器..."
ssh -i "$SSH_KEY" ${SERVER_USER}@${SERVER_HOST} "cd $DEPLOY_DIR && docker-compose up -d"

# 7. 清理旧镜像
log_info "清理旧镜像..."
ssh -i "$SSH_KEY" ${SERVER_USER}@${SERVER_HOST} "cd $DEPLOY_DIR && docker image prune -f"

# 8. 重启服务
log_info "重启服务..."
ssh -i "$SSH_KEY" ${SERVER_USER}@${SERVER_HOST} "cd $DEPLOY_DIR && docker-compose restart server"

# 9. 显示状态
log_info "服务状态："
ssh -i "$SSH_KEY" ${SERVER_USER}@${SERVER_HOST} "docker-compose ps"

log_info "========================================="
log_info "部署完成！"
log_info "========================================="
