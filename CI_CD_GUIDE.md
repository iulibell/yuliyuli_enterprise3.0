# YuliYuli CI/CD 部署指南

本文档介绍如何为 YuliYuli 项目配置完整的 CI/CD 流程，实现自动化构建、测试和部署。

## 目录

- [架构概览](#架构概览)
- [环境准备](#环境准备)
- [本地开发](#本地开发)
- [CI/CD 配置](#ci/cd-配置)
- [生产部署](#生产部署)
- [故障排查](#故障排查)

---

##  架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                        GitHub                               │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │   Code      │───▶│  GitHub     │───▶│  GitHub     │     │
│  │   Push      │    │  Actions    │    │  Packages   │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      生产服务器                              │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │   Docker    │    │   Docker    │    │   Docker    │     │
│  │   Compose   │◀───│    Pull     │◀───│    SSH      │     │
│  │             │    │   Images    │    │   Deploy    │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
│         │                                                   │
│         ▼                                                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Nginx ◀──▶ Frontend ◀──▶ Backend ◀──▶ MySQL       │   │
│  │                            │         ◀──▶ Redis     │   │
│  │                            │         ◀──▶ RabbitMQ  │   │
│  │                            └─────────▶ Elasticsearch │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 环境准备

### 1. 服务器要求

| 组件 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2核 | 4核 |
| 内存 | 4GB | 8GB |
| 磁盘 | 50GB | 100GB |
| 系统 | Ubuntu 20.04+ | Ubuntu 22.04 LTS |

### 2. 安装 Docker

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Docker
curl -fsSL https://get.docker.com | sh

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 将当前用户加入 docker 组
sudo usermod -aG docker $USER
newgrp docker
```

### 3. 配置 GitHub Secrets

在 GitHub 仓库的 Settings > Secrets and variables > Actions 中添加以下 secrets：

| Secret Name | 说明 | 示例 |
|------------|------|------|
| `SERVER_HOST` | 服务器 IP 或域名 | `192.168.1.100` |
| `SERVER_USER` | SSH 用户名 | `root` |
| `SERVER_PORT` | SSH 端口 | `22` |
| `SSH_PRIVATE_KEY` | SSH 私钥 | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `SLACK_WEBHOOK` | Slack 通知 webhook（可选） | `https://hooks.slack.com/...` |

#### 生成 SSH 密钥

```bash
# 在本地生成密钥对
ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions

# 将公钥添加到服务器
ssh-copy-id -i ~/.ssh/github_actions.pub root@your-server-ip

# 复制私钥内容，添加到 GitHub Secrets
cat ~/.ssh/github_actions
```

---

## 本地开发

### 使用 Docker Compose 启动

```bash
# 1. 克隆项目
git clone https://github.com/iulibell/yuliyuli_enterprise3.0.git
cd yuliyuli_enterprise3.0

# 2. 创建环境变量文件
cp .env.example .env
# 编辑 .env 文件，设置您的配置

# 3. 启动所有服务
docker-compose up -d

# 4. 查看日志
docker-compose logs -f

# 5. 停止服务
docker-compose down
```

### 服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost | Web 界面 |
| 后端 API | http://localhost:8081 | REST API |
| RabbitMQ | http://localhost:15672 | 消息队列管理 |
| Elasticsearch | http://localhost:9200 | 搜索引擎 |

---

## CI/CD 配置

### 工作流说明

CI/CD 流程定义在 `.github/workflows/ci-cd.yml`，包含以下阶段：

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    Test     │───▶│    Build    │───▶│   Deploy    │───▶│   Notify    │
│   测试阶段   │    │   构建阶段   │    │   部署阶段   │    │   通知阶段   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

#### 1. 测试阶段 (Test)

- **后端测试**: 运行 Maven 单元测试
- **前端测试**: 运行 ESLint 检查和构建测试

#### 2. 构建阶段 (Build)

- 构建 Docker 镜像
- 推送到 GitHub Container Registry (ghcr.io)
- 支持多架构构建 (amd64/arm64)

#### 3. 部署阶段 (Deploy)

- 通过 SSH 连接到服务器
- 拉取最新代码
- 拉取最新 Docker 镜像
- 重启服务
- 执行健康检查

#### 4. 通知阶段 (Notify)

- 部署成功/失败发送 Slack 通知

### 触发条件

| 事件 | 触发行为 |
|------|---------|
| `push` 到 `master/main` | 完整 CI/CD 流程 |
| `push` 到 `develop` | 仅构建镜像 |
| `push` tag `v*` | 构建并部署生产环境 |
| `pull_request` | 仅运行测试 |

---

## 🌐 生产部署

### 方式一：GitHub Actions 自动部署（推荐）

1. 配置好 GitHub Secrets
2. 推送代码到 `master` 分支
3. GitHub Actions 自动执行部署

### 方式二：手动部署脚本

```bash
# 1. 登录服务器
ssh root@your-server-ip

# 2. 克隆项目
git clone https://github.com/iulibell/yuliyuli_enterprise3.0.git /opt/yuliyuli
cd /opt/yuliyuli

# 3. 配置环境变量
cp .env.example .env
nano .env  # 编辑配置

# 4. 运行部署脚本
chmod +x deploy.sh
./deploy.sh

# 或使用 docker-compose 直接启动
docker-compose -f docker-compose.prod.yml up -d
```

### 部署脚本命令

```bash
# 部署应用
./deploy.sh deploy

# 查看日志
./deploy.sh logs          # 查看所有日志
./deploy.sh logs backend  # 查看后端日志

# 健康检查
./deploy.sh health

# 备份数据
./deploy.sh backup

# 重启服务
./deploy.sh restart

# 停止服务
./deploy.sh stop
```

---

## 配置文件说明

### 1. `.env` - 环境变量

```bash
# 数据库
MYSQL_ROOT_PASSWORD=your_secure_password

# Redis
REDIS_PASSWORD=your_redis_password

# RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASS=your_rabbitmq_password

# 其他配置
JWT_SECRET=your_jwt_secret
```

### 2. `docker-compose.yml` - 开发环境

- 使用本地构建
- 包含所有服务
- 适合开发和测试

### 3. `docker-compose.prod.yml` - 生产环境

- 使用预构建镜像
- 资源限制
- 健康检查
- 自动重启

---

## 故障排查

### 常见问题

#### 1. 容器无法启动

```bash
# 查看容器日志
docker-compose logs [service-name]

# 检查容器状态
docker-compose ps

# 重启服务
docker-compose restart [service-name]
```

#### 2. 数据库连接失败

```bash
# 检查 MySQL 状态
docker-compose exec mysql mysqladmin ping

# 查看 MySQL 日志
docker-compose logs mysql

# 重置数据库（谨慎操作）
docker-compose down -v  # 删除数据卷
docker-compose up -d
```

#### 3. 后端服务健康检查失败

```bash
# 检查后端日志
docker-compose logs backend

# 进入容器排查
docker-compose exec backend sh

# 测试数据库连接
docker-compose exec backend curl mysql:3306
```

#### 4. 前端无法访问后端 API

```bash
# 检查 Nginx 配置
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf

# 测试后端连通性
docker-compose exec frontend curl http://backend:8081/actuator/health
```

### 日志位置

| 服务 | 日志位置 |
|------|---------|
| 后端 | `docker-compose logs backend` |
| 前端 | `docker-compose logs frontend` |
| Nginx | `docker-compose logs frontend` |
| MySQL | `docker-compose logs mysql` |

---

## 监控与维护

### 1. 查看资源使用

```bash
# Docker 容器资源使用
docker stats

# 系统资源使用
htop
```

### 2. 备份策略

```bash
# 自动备份（添加到 crontab）
0 2 * * * /opt/yuliyuli/deploy.sh backup

# 手动备份
./deploy.sh backup
```

### 3. 更新策略

```bash
# 更新代码并重新部署
git pull origin master
./deploy.sh deploy

# 仅更新镜像
docker-compose pull
docker-compose up -d
```

---
