# YuliYuli 视频分享平台

一个基于 Spring Boot + Vue3 的视频分享平台，类似B站的视频社区系统。

## 项目简介

YuliYuli 是一个视频分享平台，支持视频投稿、播放、搜索等核心功能。项目采用前后端分离架构，使用主流技术栈实现。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存数据库 |
| Elasticsearch | 8.11+ | 搜索引擎 |
| RabbitMQ | 3.12+ | 消息队列 |
| JWT | 0.12.3 | 身份认证 |
| Redisson | 3.27.0 | Redis客户端 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.21 | 前端框架 |
| TypeScript | 5.4.5 | 类型语言 |
| Element Plus | 2.6.3 | UI组件库 |
| Axios | 1.6.8 | HTTP客户端 |
| Pinia | 2.1.7 | 状态管理 |

## 项目结构

```
yuliyuli_enterprise/
├── src/main/java/com/yuliyuli/
│   ├── annotation/          # 自定义注解
│   │   ├── Desensitize.java     # 数据脱敏注解
│   │   ├── OperationLog.java    # 操作日志注解
│   │   └── RateLimit.java       # 限流注解
│   ├── aspect/              # AOP切面
│   │   ├── OperationLogAspect.java  # 操作日志切面
│   │   └── RateLimitAspect.java     # 限流切面
│   ├── common/              # 公共类
│   │   ├── CurrentUserHolder.java   # 当前用户持有者
│   │   └── Result.java              # 统一响应结果
│   ├── config/              # 配置类
│   │   ├── CacheConfig.java         # Spring Cache配置
│   │   ├── DesensitizeSerializer.java # 脱敏序列化器
│   │   ├── RabbitMqConfig.java      # RabbitMQ配置
│   │   ├── RedisConfig.java         # Redis配置
│   │   └── WebConfig.java           # Web配置
│   ├── controller/          # 控制器层
│   │   ├── InfoController.java
│   │   ├── SearchController.java
│   │   ├── UserController.java
│   │   └── VideoController.java
│   ├── document/            # ES文档
│   │   └── VideoDocument.java
│   ├── dto/                 # 数据传输对象
│   │   ├── query/           # 查询包装器
│   │   └── vo/              # 视图对象
│   ├── entity/              # 实体类
│   ├── enums/               # 枚举类
│   │   └── DesensitizeType.java
│   ├── exception/           # 异常处理
│   │   └── GlobalExceptionHandler.java
│   ├── init/                # 初始化类
│   │   ├── BloomFilterInit.java     # 布隆过滤器初始化
│   │   ├── HotUserInit.java         # 热门用户初始化
│   │   ├── SearchVideoInit.java     # 搜索视频初始化
│   │   └── VideoInfoInit.java       # 视频信息初始化
│   ├── interceptor/         # 拦截器
│   │   └── LoginInterceptor.java
│   ├── mapper/              # 数据访问层
│   ├── repository/          # ES仓库
│   ├── service/             # 服务层
│   │   ├── impl/            # 服务实现
│   │   ├── InfoService.java
│   │   ├── SearchService.java
│   │   ├── UserService.java
│   │   └── VideoService.java
│   ├── util/                # 工具类
│   │   ├── BloomFilterUtil.java
│   │   ├── DesensitizeUtil.java
│   │   ├── JwtUtil.java
│   │   ├── TransferUtil.java
│   │   └── VideoConvertUtil.java
│   └── YuliyuliEnterpriseApplication.java
├── src/main/resources/
│   └── application.yml
└── yuliyuli-frontend/       # 前端项目
    ├── src/
    ├── public/
    └── package.json
```

## 核心功能

### 用户模块
- 用户注册/登录（JWT认证）
- 用户信息管理
- 头像上传
- 关注/粉丝功能

### 视频模块
- 视频投稿（支持分片上传）
- 视频播放
- 视频点赞/收藏/评论
- 视频搜索（Elasticsearch）
- 热门视频推荐

### 其他功能
- 接口限流（RateLimit）
- 数据脱敏（手机号、密码）
- 操作日志记录（AOP）
- 事务管理
- Spring Cache缓存

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- Elasticsearch 8.11+
- RabbitMQ 3.12+
- Node.js 18+

### 后端启动

1. 创建数据库

<details>
<summary>点击查看完整SQL</summary>

```sql
CREATE DATABASE yuliyuli_enterprise CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` varchar(255) NOT NULL COMMENT '视频ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT '0' COMMENT '父评论ID(0为顶级)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '0为未删除，1为已删除',
  `comment_id` bigint DEFAULT NULL COMMENT '评论id',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3979 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

# 模拟存在的手机号表
CREATE TABLE `exist_phone` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '电话号主姓名',
  `phone` varchar(11) NOT NULL COMMENT '电话号码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `follow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follow_user_id` bigint NOT NULL COMMENT '关注者ID',
  `fan_user_id` bigint NOT NULL COMMENT '被关注者（粉丝）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow_fan` (`follow_user_id`,`fan_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关注表';

CREATE TABLE `type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL COMMENT '分区名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频分区';

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户账号/手机号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(加密)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '/static/images/202304061680747832129368.jpg' COMMENT '头像(图片路径)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_id` bigint DEFAULT NULL,
  `follow_count` bigint DEFAULT '0' COMMENT '关注数量',
  `fans_count` bigint DEFAULT '0' COMMENT '粉丝数量',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE `user_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `gender` tinyint DEFAULT '0' COMMENT '0-未知 1-男 2-女',
  `birthday` date DEFAULT NULL,
  `sign` varchar(100) DEFAULT NULL COMMENT '签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息扩展';

CREATE TABLE `video` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `intro` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '这个作者很懒，没有任何描述' COMMENT '简介',
  `url` varchar(255) NOT NULL COMMENT '视频地址',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面',
  `type_id` bigint NOT NULL COMMENT '分区ID',
  `play_count` int DEFAULT '0' COMMENT '播放量',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `collection_count` int DEFAULT '0' COMMENT '收藏数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '1表示已删除，0表示未删除',
  `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '懒得起名的作者' COMMENT '作者名字',
  `comment_count` int DEFAULT '0' COMMENT '评论数量',
  `author_avatar` varchar(255) DEFAULT '/static/images/202304061680747832129368.jpg',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频表';

CREATE TABLE `video_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_user` (`video_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频收藏';

CREATE TABLE `video_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_video_user` (`video_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频点赞';
```
</details>

2. 修改配置
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yuliyuli_enterprise?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
  elasticsearch:
    uris: http://localhost:9200
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

3. 运行项目
```bash
./mvnw spring-boot:run
```

### 前端启动

```bash
cd yuliyuli-frontend
npm install
npm run dev
```

## 项目亮点

1. **架构设计**：前后端分离，分层清晰，职责明确
2. **性能优化**：
   - Redis缓存（视频列表、热门视频）
   - Spring Cache注解式缓存
   - 布隆过滤器防止缓存穿透
   - 消息队列异步处理
3. **数据安全**：
   - JWT认证
   - 接口限流
   - 敏感数据脱敏
   - 事务管理保证数据一致性
4. **可观测性**：
   - 操作日志记录（AOP实现）
   - 全局异常处理
   - 详细的日志输出
5. **可扩展性**：模块化设计，易于扩展新功能

## 作者

Dima

## 项目介绍

夯实技术，现在前端还未完全掌握，后续添加生产环境、开发环境、测试环境等，并完成部署。
有一丁丁功能尚未完成（收藏和分享的数量更新），急着去完成分布式了。
