#!/bin/bash

# 博客系统快速部署脚本
# 用法: ./quick-deploy.sh

set -e

echo "========================================="
echo "博客系统快速部署"
echo "========================================="

# 配置
SERVER_USER="${SERVER_USER:-root}"
SERVER_HOST="${SERVER_HOST:-your-server-ip}"
DEPLOY_DIR="/var/www/blog"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_step() {
    echo -e "${BLUE}[$1]${NC} $2"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

log_info() {
    echo -e "${GREEN}[ℹ]${NC} $1"
}

# 检查 SSH 密钥
log_step "检查 SSH 密钥..."
if [ ! -f ~/.ssh/github_actions ]; then
    log_error "SSH 密钥不存在！请先生成 SSH 密钥对："
    echo "  ssh-keygen -t rsa -b 4096 -C 'github-deploy' -f ~/.ssh/github_actions"
    exit 1
fi

# 检查 SSH 连接
log_step "检查服务器连接..."
if ! ssh -i ~/.ssh/github_actions -o StrictHostKeyChecking=no -o ConnectTimeout=5 ${SERVER_USER}@${SERVER_HOST} "echo '连接成功'" 2>/dev/null; then
    log_error "无法连接到服务器！请检查："
    echo "  1. SSH 密钥是否正确配置"
    echo "  2. 服务器 IP 地址是否正确"
    echo "  3. SSH 服务是否正常运行"
    echo "  4. 防火墙是否允许 SSH 连接"
    exit 1
fi

log_success "服务器连接成功"

# 创建必要目录
log_step "创建必要目录..."
ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} \
  "mkdir -p ${DEPLOY_DIR} && \
   mkdir -p ${DEPLOY_DIR}/blog-frontend && \
   mkdir -p ${DEPLOY_DIR}/blog-admin && \
   mkdir -p ${DEPLOY_DIR}/blog-server && \
   mkdir -p ${DEPLOY_DIR}/docker/mysql && \
   mkdir -p ${DEPLOY_DIR}/docker/nginx && \
   mkdir -p ${DEPLOY_DIR}/.github/workflows && \
   mkdir -p /var/lib/blog && \
   mkdir -p /var/lib/blog/mysql && \
   mkdir -p /var/lib/blog/redis && \
   mkdir -p /var/lib/blog/uploads && \
   mkdir -p /var/log/blog"
log_success "目录创建完成"

# 拉取最新代码
log_step "拉取最新代码..."
ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} "cd ${DEPLOY_DIR} && git pull origin main"
log_success "代码更新成功"

# 检查 Docker
log_step "检查 Docker..."
if ! ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} "docker --version" >/dev/null 2>&1; then
    log_error "服务器上未安装 Docker！请先安装 Docker。"
    exit 1
fi
log_success "Docker 已安装"

# 构建镜像
log_step "构建 Docker 镜像..."
ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} "cd ${DEPLOY_DIR} && docker-compose build"
log_success "镜像构建成功"

# 停止旧容器
log_step "停止旧容器..."
ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} "cd ${DEPLOY_DIR} && docker-compose down"
log_success "旧容器已停止"

# 启动新容器
log_step "启动新容器..."
ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} "cd ${DEPLOY_DIR} && docker-compose up -d"
log_success "新容器已启动"

# 清理旧镜像
log_step "清理旧镜像..."
ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} "cd ${DEPLOY_DIR} && docker image prune -f"
log_success "旧镜像已清理"

# 重启服务
log_step "重启服务..."
ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} "cd ${DEPLOY_DIR} && docker-compose restart server"
log_success "服务已重启"

# 显示状态
log_step "服务状态："
ssh -i ~/.ssh/github_actions ${SERVER_USER}@${SERVER_HOST} "docker-compose ps"

# 验证
log_step "验证部署..."
sleep 5

if curl -sf http://${SERVER_HOST} >/dev/null 2>&1; then
    log_success "部署验证成功！"
else
    log_warn "前端访问失败，请检查容器状态："
    echo "  docker-compose logs nginx"
fi

echo ""
log_info "========================================="
log_info "部署完成！"
log_info "前端地址: http://${SERVER_HOST}"
log_info "后台地址: http://${SERVER_HOST}/admin"
log_info "========================================="
