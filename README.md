# YuliYuli 视频分享平台

一个基于 `Spring Boot + Vue 3` 的前后端分离视频平台，覆盖视频投稿、播放、搜索、点赞、收藏、评论、关注等核心业务场景。

> 项目定位：个人全栈实战项目，重点偏后端工程能力建设。  
> 项目目标：不只完成业务闭环，还通过缓存、消息队列、搜索引擎、异步任务和持续重构来提升系统性能与可维护性。

## 项目概览

YuliYuli 模拟一个轻量视频社区，主要包含：

- 用户注册、登录、资料维护
- 视频投稿与展示
- 视频播放与播放量统计
- 点赞、收藏、评论、关注
- 热门推荐与搜索建议
- Docker Compose 部署与 GitHub Actions CI/CD

这个项目的重点不只是“功能做出来”，而是围绕真实业务链路逐步完成：

- 中间件接入
- 缓存与异步削峰
- 搜索能力建设
- 后端结构重构
- CI/CD 与交付文档完善

## 技术栈

### 后端

- `Spring Boot 3.2.5`
- `Spring Security`
- `MyBatis-Plus 3.5.5`
- `MySQL 8.0+`
- `Redis 7+`
- `Redisson 3.27.x`
- `RabbitMQ 3.12+`
- `Elasticsearch 8.11+`
- `JWT 0.12.3`
- `Spring Boot Actuator`

### 前端

- `Vue 3`
- `TypeScript`
- `Vite`
- `Element Plus`
- `Axios`
- `Pinia`

### 工程化

- `Docker`
- `Docker Compose`
- `GitHub Actions`

## 核心功能

### 用户模块

- 用户注册 / 登录
- JWT 登录态校验
- 用户信息获取与修改
- 头像上传
- 用户关注 / 取关

### 视频模块

- 视频投稿
- 分类分页加载
- 视频播放统计
- 视频点赞 / 收藏 / 评论
- 视频删除

### 搜索与推荐

- Elasticsearch 视频搜索
- 搜索建议
- 热门视频推荐

### 工程能力

- 全局异常处理
- 限流切面
- 操作日志切面
- 缓存与异步任务
- Docker 化部署
- GitHub Actions CI/CD

## 项目亮点

- **中间件使用贴合业务**：不是简单堆技术，而是围绕“播放、互动、推荐、搜索”这些场景使用 `Redis / RabbitMQ / Elasticsearch`
- **有真实的后端重构过程**：围绕鉴权边界、任务拆分、异步链路、service 返回风格做过多轮整理
- **兼顾性能与可维护性**：既做缓存、批量落库、防抖，也做 DTO 化、consumer 公共模板、README/CI/CD 完善
- **可作为实习项目讲解**：既能讲功能实现，也能讲优化、重构、设计取舍

## 系统设计思路

### 1. 为什么使用 Redis

Redis 在项目中承担两类职责：

- 缓存热点数据，例如视频列表、热门推荐
- 作为异步链路中的中间状态存储，例如播放量计数、点赞集合、关注集合

同时使用 `Redisson` 提供：

- 分布式锁
- 延时任务排序集合
- 原子计数器

### 2. 为什么使用 RabbitMQ

RabbitMQ 用于把高频写操作异步化，主要覆盖：

- 视频播放
- 视频点赞
- 视频收藏
- 视频评论
- 用户关注 / 取关
- 视频删除

这样可以把请求线程与数据库写入解耦，降低峰值流量对核心库的冲击。

### 3. 为什么使用 Elasticsearch

视频搜索场景更适合 ES，而不是 MySQL `like` 查询。  
ES 主要用于：

- 关键词搜索
- 搜索建议
- 推荐/热榜相关能力的检索支撑

### 4. 播放量链路设计

播放量统计链路大致如下：

1. 播放请求进入 MQ
2. consumer 将播放增量写入 Redis 原子计数器
3. 延时任务定时聚合增量
4. 批量写入 MySQL 并同步 ES

这样避免了“每次播放都直接写库”的写放大问题。

## 已完成优化与重构

### 性能与稳定性优化

- **修复视频列表缓存锁逻辑**  
  避免获取到锁后反而走降级逻辑，减少并发回源数据库压力

- **Redis `KEYS` 改 `SCAN`**  
  避免在大 keyspace 下触发阻塞扫描

- **播放统计改为批量落库**  
  从“每次播放都写 MySQL / ES”改为“Redis 计数 + 定时批量同步”

- **前端搜索建议增加防抖与请求取消**  
  减少无效请求和输入抖动

- **热点日志降噪**  
  降低高频链路日志级别，避免日志放大和敏感 token 明文输出

### 后端结构重构

- **统一鉴权边界**
  - 收口公开路径配置到 `AuthPathConstants`
  - 明确当前登录校验由 `LoginInterceptor` 负责
  - 删除空壳 `JwtTokenFilter`

- **拆分延时任务职责**
  - 从单个大类拆成：
    - `LikeDelayTask`
    - `PlayDelayTask`
    - `DeleteDelayTask`
    - `FollowDelayTask`

- **拆分任务 support**
  - 将处理逻辑进一步拆成：
    - `LikeTaskSupport`
    - `PlayTaskSupport`
    - `DeleteTaskSupport`
    - `FollowTaskSupport`

- **拆分视频服务职责**
  - 把 `VideoServiceImpl` 中的消息发布抽到 `VideoEventPublisher`
  - 把查询、缓存、热门推荐逻辑抽到 `VideoQuerySupport`

- **强类型化异步消息体**
  - 用 `FollowCommand`、`VideoDeleteCommand` 替代部分 `Map<String, Object>`

- **抽取 consumer 公共重试模板**
  - 通过 `ConsumerRetrySupport` 统一：
    - 重试次数读取
    - `basicNack`
    - `basicReject`
    - 死信 ack

- **统一 service 返回风格**
  - 引入 `ServiceResult`
  - 减少 controller 中的字符串判断逻辑

### 业务正确性精修

- 修复点赞链路中缓存 key 使用前后不一致的问题
- 修复点赞延时任务时间戳写死的问题
- 修复收藏链路中判断对象错误和计数重复递增的问题
- 修复视频分发 consumer 中重复确认/重复重试风险
- 修复 `InfoController` 中 `Map` 参数强转 `Long` 可能导致的类型异常

## 项目结构

```text
yuliyuli_enterprise/
├─ .github/workflows/              # GitHub Actions 工作流
├─ src/main/java/com/yuliyuli/
│  ├─ annotation/                  # 自定义注解
│  ├─ aspect/                      # AOP 切面（日志、限流）
│  ├─ common/                      # 通用返回体、上下文、结果模型
│  ├─ config/                      # 安全、MQ、Redis、Web 配置
│  ├─ consumer/                    # MQ 消费者
│  │  └─ support/                  # consumer 公共重试模板
│  ├─ controller/                  # 接口层
│  ├─ dto/                         # DTO / VO / command
│  ├─ entity/                      # 实体对象
│  ├─ exception/                   # 全局异常处理
│  ├─ init/                        # 启动初始化逻辑
│  ├─ mapper/                      # MyBatis 数据访问层
│  ├─ repository/                  # Elasticsearch 仓库
│  ├─ service/                     # 业务层
│  │  └─ support/                  # service 支撑类
│  ├─ task/                        # 延时任务与任务 support
│  └─ util/                        # 工具类
├─ src/main/resources/
│  └─ application.yml
├─ yuliyuli-frontend/              # 前端项目
├─ Dockerfile
├─ docker-compose.yml
└─ README.md
```

## 本地运行

### 1. 环境要求

- `JDK 17+`
- `Maven 3.9+`
- `Node.js 18+`
- `MySQL 8.0+`
- `Redis 7+`
- `RabbitMQ 3.12+`
- `Elasticsearch 8.11+`

### 2. 初始化数据库

- 新建数据库：`yuliyuli_enterprise`
- 导入初始化 SQL

如果后续继续维护，建议将 SQL 独立沉淀到：

- `docs/sql/init.sql`

### 3. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yuliyuli_enterprise?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  elasticsearch:
    uris: http://localhost:9200
```

### 4. 启动后端

```bash
./mvnw spring-boot:run
```

或：

```bash
mvn spring-boot:run
```

### 5. 启动前端

```bash
cd yuliyuli-frontend
npm install
npm run dev
```

### 6. 默认访问地址

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8081`
- 健康检查：`http://localhost:8081/actuator/health`

## Docker 部署

项目提供：

- `Dockerfile`
- `yuliyuli-frontend/Dockerfile`
- `docker-compose.yml`

可通过以下方式本地启动整套服务：

```bash
docker compose up -d --build
```

默认会启动：

- MySQL
- Redis
- RabbitMQ
- Elasticsearch
- Backend
- Frontend

## CI/CD

项目提供 GitHub Actions 工作流：

- 后端测试
- 前端构建
- Docker Compose 构建
- 条件化部署
- 流程通知

并已针对以下问题做过修复与增强：

- 部署分支不再写死 `master`
- Redis 健康检查兼容有无密码场景
- 后端健康检查接入 Actuator
- workflow 支持按目录变更范围触发前后端 job
- 部署前增加服务器 `docker` / `docker compose` 预检查

## 面试可重点讲的内容

如果作为实习项目，比较推荐重点讲这几块：

- **缓存击穿防护**：视频列表先查缓存，再用分布式锁控制回源
- **播放量异步统计链路**：MQ + Redis 计数器 + 延时任务 + 批量落库
- **异步任务重构**：将大而全的任务处理类拆成多个独立任务与 support
- **工程可维护性优化**：DTO 替代 `Map`、抽公共重试模板、统一 service 返回模型
- **搜索能力建设**：使用 ES 支撑视频搜索与搜索建议
- **CI/CD 和部署**：项目不仅能开发运行，也具备基本构建部署能力

## 项目不足

目前项目仍然有一些可以继续改进的地方：

- 部分接口仍然使用 `Map` 作为返回结构，后续可继续 DTO 化
- 自动化测试覆盖不足
- 还没有系统性压测数据
- 监控、告警、灰度发布等能力尚未补齐
- 前端部分页面仍可继续抽离公共逻辑

## 后续计划

- 继续统一 controller / DTO 返回风格
- 补充核心链路测试
- 完善接口文档与部署文档
- 增强前端模块复用能力
- 在条件允许的情况下补压测与监控方案

## 相关文档

- `docs/INTERVIEW_QA.md`：项目面试问答

## 作者

`Dima`
