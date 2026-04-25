# 前端 Dockerfile
# 使用多阶段构建

# 第一阶段：构建阶段
FROM node:20-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm ci

# 复制源代码
COPY . .

# 构建项目
RUN npm run build

# 第二阶段：运行阶段（使用 Nginx 提供服务）
FROM nginx:1.25-alpine

# 安装 tzdata 设置时区
RUN apk add --no-cache tzdata

# 设置时区
ENV TZ=Asia/Shanghai

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 创建静态资源目录
RUN mkdir -p /usr/share/nginx/html/static/videoUrl \
    /usr/share/nginx/html/static/coverUrl \
    /usr/share/nginx/html/static/avatarUrl \
    /usr/share/nginx/html/static/images

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
