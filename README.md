# AI 知识库管理系统




![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.6-green.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

一个基于 Spring Boot + Vue 3 的智能知识库管理系统，集成 AI 对话、笔记管理、思维导图、文件管理等功能。

---



- 前端：[K-iitty/AIKnowledgeBaseFrontend: 智能问答知识库](https://github.com/K-iitty/AIKnowledgeBaseFrontend)
- 后端：[K-iitty/AIKnowledgeBaseBackend: 智能问答知识库](https://github.com/K-iitty/AIKnowledgeBaseBackend)

##  功能特性

### AI 智能助手

- **智能对话**：集成阿里云通义千问大模型
- **RAG 检索增强**：基于本地知识库的智能问答，支持笔记和思维导图双索引
- **异步索引重建**：使用 @Async + 自定义线程池，索引重建不阻塞用户操作
- **会话管理**：多会话支持，历史记录保存
- **流式响应**：SSE 实时流式输出，类似 ChatGPT 打字效果
- **文本润色**：AI 辅助优化笔记内容
- **智能摘要**：自动生成内容摘要
- **上下文记忆**：支持多轮对话上下文，滑动窗口管理（最近 20 条）
- **API 限流保护**：三级限流策略（全局/接口/用户级），防止系统过载

### 笔记管理

- **Markdown 编辑器**：支持实时预览、语法高亮
- **分类管理**：树形分类结构，支持拖拽排序
- **标签系统**：多标签支持，快速筛选
- **文件导入**：支持 Markdown、PDF 等格式导入
- **导出功能**：支持导出为 Markdown 或 PDF
- **封面设置**：自定义笔记封面图片

### 思维导图
- **可视化编辑**：基于 jsMind 的交互式思维导图编辑器
- **节点管理**：支持添加、编辑、删除节点
- **节点备注**：为每个节点添加详细备注
- **导入导出**：支持 XMind 格式导入导出

### 链接收藏
- **网址管理**：收藏常用网站链接
- **分类整理**：按类别组织链接
- **图标显示**：自动获取网站图标
- **快速访问**：一键跳转到收藏网站

### 用户系统
- **用户注册/登录**：JWT Token 认证
- **滑动验证码**：集成 AJ-Captcha 滑块拼图验证，防止暴力破解
- **权限管理**：基于 Spring Security 的权限控制
- **个人中心**：用户信息管理
- **数据隔离**：用户数据完全隔离

## 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.7 | 核心框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.x | 缓存数据库 |
| JWT | 0.11.5 | Token 认证 |
| Spring AI Alibaba | 1.0.0.2 | AI 集成框架 |
| 阿里云 OSS | 3.17.4 | 对象存储 |
| PDFBox | 2.0.30 | PDF 处理 |
| Knife4j | 4.4.0 | API 文档 |
| Lombok | 1.18.32 | 代码简化 |
| AJ-Captcha | 1.3.0 | 滑动验证码 |
| Guava | 32.1.3-jre | 限流工具 |

### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.6 | 前端框架 |
| Vue Router | 4.4.5 | 路由管理 |
| Element Plus | 2.8.6 | UI 组件库 |
| Axios | 1.7.7 | HTTP 客户端 |
| Marked | 17.0.0 | Markdown 解析 |
| jsMind | 0.8.5 | 思维导图库 |
| Vite | 5.4.10 | 构建工具 |

## 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Redis**: 7.x+
- **Maven**: 3.6+

### 后端启动

1. **克隆项目**
```bash
git clone <repository-url>
cd xian-AIKnowledgeBase/AIKnowledgeBaseBackend/AIKnowledgeBaseBackend
```

2. **配置数据库**

创建数据库：
```sql
CREATE DATABASE aiknowledgebase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **修改配置文件**

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiknowledgebase?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
  
  ai:
    dashscope:
      api-key: your_dashscope_api_key  # 阿里云通义千问 API Key

# AJ-Captcha 验证码配置
aj:
  captcha:
    type: blockPuzzle          # 验证码类型：滑块拼图
    cache-type: redis          # 缓存类型：Redis
    expire-seconds: 120        # 验证码有效期（秒）
    slip-offset: 5             # 滑动误差允许范围（像素）
    aes-status: false          # AES 加密状态

aliyun:
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com
    bucket-name: your_bucket_name
    access-key-id: ${OSS_ACCESS_KEY_ID}
    access-key-secret: ${OSS_ACCESS_KEY_SECRET}

jwt:
  secret: your_jwt_secret_key_at_least_256_bits_long
  expiration: 604800000  # 7天
```

4. **设置环境变量**

```bash
# Windows
set OSS_ACCESS_KEY_ID=your_access_key_id
set OSS_ACCESS_KEY_SECRET=your_access_key_secret

# Linux/Mac
export OSS_ACCESS_KEY_ID=your_access_key_id
export OSS_ACCESS_KEY_SECRET=your_access_key_secret
```

5. **启动后端**

```bash
# 使用 Maven
mvn clean install
mvn spring-boot:run

# 或使用 IDE 直接运行 AiKnowledgeBaseBackendApplication
```

后端服务将在 `http://localhost:8081` 启动

### 前端启动

1. **进入前端目录**
```bash
cd xian-AIKnowledgeBase/AIKnowledgeBaseFrontend
```

2. **安装依赖**
```bash
npm install
```

3. **启动开发服务器**
```bash
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

4. **构建生产版本**
```bash
npm run build
```

## 页面

![PixPin_2025-11-17_13-17-43](./assets/PixPin_2025-11-17_13-17-43.png)



![PixPin_2025-11-16_23-02-12](./assets/PixPin_2025-11-16_23-02-12.png)

![PixPin_2025-11-16_22-13-03](./assets/PixPin_2025-11-16_22-13-03.png)

![PixPin_2025-11-22_17-01-11](./assets/PixPin_2025-11-22_17-01-11.png)



![PixPin_2025-11-16_22-12-05](./assets/PixPin_2025-11-16_22-12-05.png)

![PixPin_2025-11-16_22-12-35](./assets/PixPin_2025-11-16_22-12-35.png)

![PixPin_2025-11-22_17-01-54](./assets/PixPin_2025-11-22_17-01-54.png)

![PixPin_2025-11-16_22-14-49](./assets/PixPin_2025-11-16_22-14-49.png)

![PixPin_2025-11-22_17-03-08](./assets/PixPin_2025-11-22_17-03-08.png)

![PixPin_2025-11-16_22-12-17](./assets/PixPin_2025-11-16_22-12-17.png)

##  项目结构

### 后端结构

```
AIKnowledgeBaseBackend/
├── src/main/java/com/fanfan/aiknowledgebasebackend/
│   ├── common/
│   │   ├── config/          # 配置类
│   │   │   ├── AsyncConfig.java          # 异步任务配置
│   │   │   ├── CaptchaConfig.java        # 验证码配置
│   │   │   ├── CorsConfig.java           # 跨域配置
│   │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   │   ├── OssConfig.java            # OSS 配置
│   │   │   ├── SecurityConfig.java       # 安全配置
│   │   │   └── WebMvcConfig.java         # Web MVC 配置
│   │   │
│   │   ├── interceptor/     # 拦截器
│   │   │   └── RateLimitInterceptor.java  # 限流拦截器
│   │   │
│   │   └── security/        # 安全相关
│   │       └── JwtAuthenticationFilter.java  # JWT 过滤器
│   │
│   ├── controller/          # 控制器层
│   │   ├── AiController.java         # AI 对话接口
│   │   ├── AuthController.java       # 认证接口
│   │   ├── CategoryController.java   # 分类管理
│   │   ├── FileController.java       # 文件管理
│   │   ├── LinkController.java       # 链接管理
│   │   ├── MindmapController.java    # 思维导图
│   │   ├── NoteController.java       # 笔记管理
│   │   └── ProfileController.java    # 个人资料
│   │
│   ├── dto/                 # 数据传输对象
│   │   ├── ChatRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── NoteRequest.java
│   │   ├── MindmapCreateRequest.java
│   │   └── ...
│   │
│   ├── entity/              # 实体类
│   │   ├── User.java
│   │   ├── Note.java
│   │   ├── Mindmap.java
│   │   ├── ChatSession.java
│   │   ├── ChatMessage.java
│   │   └── ...
│   │
│   ├── mapper/              # MyBatis Mapper
│   │   ├── UserMapper.java
│   │   ├── NoteMapper.java
│   │   ├── MindmapMapper.java
│   │   └── ...
│   │
│   ├── service/             # 业务逻辑层
│   │   ├── impl/
│   │   ├── UserService.java
│   │   ├── NoteService.java
│   │   ├── MindmapService.java
│   │   ├── EnhancedAiService.java    # AI 增强服务
│   │   └── RagService.java           # RAG 检索服务
│   │
│   └── util/                # 工具类
│       ├── JwtUtil.java
│       └── OssUtil.java
│
└── src/main/resources/
    ── application.yml      # 配置文件
```

### 前端结构

```
AIKnowledgeBaseFrontend/
├── src/
│   ├── assets/              # 静态资源
│   │   └── styles/          # 样式文件
│   │
│   ├── components/          # 组件
│   │   ├── Captcha.vue           # 滑动验证码组件
│   │   ├── MarkdownEditor.vue    # Markdown 编辑器
│   │   ├── MindMapEditor.vue     # 思维导图编辑器
│   │   ├── ChatWindow.vue        # AI 聊天窗口
│   │   └── ...
│   │
│   ├── layouts/             # 布局组件
│   │   └── MainLayout.vue        # 主布局
│   │
│   ├── router/              # 路由配置
│   │   └── index.js
│   │
│   ├── utils/               # 工具函数
│   │   └── http.js              # HTTP 请求封装
│   │
│   ├── views/               # 页面组件
│   │   ├── Login.vue            # 登录页
│   │   ├── Register.vue         # 注册页
│   │   ├── Notes.vue            # 笔记列表
│   │   ├── NoteDetail.vue       # 笔记详情
│   │   ├── Mindmaps.vue         # 思维导图列表
│   │   ├── MindmapDetail.vue    # 思维导图详情
│   │   ├── AIChat.vue           # AI 对话
│   │   ├── Links.vue            # 链接收藏
│   │   ├── Profile.vue          # 个人资料
│   │   └── KnowledgeContent.vue # 知识库内容
│   │
│   ├── App.vue              # 根组件
│   └── main.js              # 入口文件
│
├── index.html               # HTML 模板
├── package.json             # 依赖配置
└── vite.config.js           # Vite 配置
```

---

## API 文档

### 访问 Swagger UI

启动后端后，访问：
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- Knife4j UI: `http://localhost:8081/doc.html`

### 1. RAG 检索增强生成

系统实现了基于本地知识库的 RAG（Retrieval-Augmented Generation）功能：

- **索引构建**：自动为笔记和思维导图建立关键词索引，支持中英文停用词过滤
- **异步重建**：使用 @Async 注解 + 自定义线程池（CPU核心数 * 2），索引重建不阻塞用户请求
- **语义检索**：支持中英文混合检索，智能匹配相关内容
- **上下文增强**：将检索到的知识库内容作为上下文提供给 AI
- **相关性排序**：基于关键词重叠度和内容相似度的评分算法
- **Prompt Engineering**：动态注入检索结果，明确告知 AI 信息来源边界

### 2. 思维导图编辑器

基于 jsMind 实现的功能丰富的思维导图编辑器：

- **节点操作**：添加、编辑、删除子节点和同级节点
- **节点备注**：为每个节点添加详细的文字备注
- **图片附件**：支持为节点添加图片资源
- **样式自定义**：支持节点颜色、字体等样式设置
- **导入导出**：支持 XMind 格式的导入导出

### 3. Markdown 编辑器

功能完善的 Markdown 编辑器：

- **实时预览**：编辑和预览同步显示
- **语法高亮**：代码块语法高亮显示
- **工具栏**：常用 Markdown 语法快捷插入
- **图片上传**：支持拖拽上传图片到 OSS
- **全屏编辑**：支持全屏编辑模式

### 4. AI 对话系统

集成阿里云通义千问的智能对话系统：

- **流式响应**：支持 SSE（Server-Sent Events）流式输出，实时打字效果
- **上下文记忆**：保存对话历史，支持多轮对话，滑动窗口管理（最近 20 条）
- **知识库增强**：可选择基于本地知识库的问答（RAG 模式）
- **会话管理**：支持创建、切换、删除会话，双存储策略（内存 + 数据库）
- **异步执行**：使用 CompletableFuture 异步处理，不阻塞主线程

### 5. API 限流与服务治理

基于 Guava RateLimiter 实现的三级限流策略：

- **全局限流**：1000 QPS，防止 DDoS 攻击和系统过载
- **AI 接口限流**：50 QPS，保护高成本 AI 服务
- **用户级限流**：100 QPS/用户，防止单用户滥用
- **令牌桶算法**：平滑限流，避免流量突刺
- **动态限流器**：使用 ConcurrentHashMap 为每个用户动态创建限流器
- **友好提示**：返回 HTTP 429 状态码和友好错误信息
- **拦截器实现**：基于 Spring MVC HandlerInterceptor

### 6. 异步任务与线程池管理

优化耗时操作，提升系统响应速度：

- **自定义线程池**：根据 CPU 核心数动态配置（核心线程数 = CPU 核心数，最大线程数 = CPU 核心数 * 2）
- **异步索引重建**：使用 @Async 注解，索引重建从 5-10 秒优化为 200ms 响应
- **拒绝策略**：采用 CallerRunsPolicy，保证任务不丢失
- **优雅关闭**：应用关闭时等待任务完成，避免数据不一致
- **性能监控**：记录任务执行时间，便于性能分析
- **CompletableFuture**：支持链式调用和异常处理

### 7. 滑动验证码系统

基于 AJ-Captcha 实现的安全验证系统：

- **滑块拼图验证**：用户需拖动滑块完成拼图验证
- **Redis 缓存**：验证码数据存储在 Redis 中，提高性能
- **自动过期**：验证码 120 秒后自动失效
- **防暴力破解**：有效防止机器人暴力破解登录
- **友好交互**：支持鼠标拖拽和移动端触摸操作
- **失败重试**：验证失败自动刷新验证码

---

##  配置说明

### 必需配置

1. **数据库配置**
   - MySQL 8.0+ 数据库
   - 创建数据库 `aiknowledgebase`
   - 配置用户名和密码

2. **Redis 配置**
   - Redis 7.x+ 服务
   - 配置连接信息

3. **阿里云通义千问 API**
   - 注册阿里云账号
   - 开通通义千问服务
   - 获取 API Key

4. **阿里云 OSS**
   - 创建 OSS Bucket
   - 获取 AccessKey ID 和 Secret
   - 配置 Bucket 名称和 Endpoint

5. **JWT Secret**
   - 生成至少 256 位的密钥
   - 配置在 `application.yml` 中

### 可选配置

- **文件上传大小限制**：默认 10MB
- **Token 过期时间**：默认 7 天
- **AI 模型参数**：temperature、model 等

## 开发规范

### 代码规范

- **后端**：遵循阿里巴巴 Java 开发规范
- **前端**：遵循 Vue 3 官方风格指南
- **命名**：使用有意义的变量名和函数名
- **注释**：关键逻辑添加注释说明

---

## 🎯 核心技术亮点

### 1. RAG 检索增强生成
- ✅ 完整的 RAG 流程：索引构建 → 关键词检索 → Prompt 注入 → AI 生成
- ✅ 双索引支持：笔记 + 思维导图，多源数据融合
- ✅ 中文优化：自定义停用词过滤，支持中英文混合检索
- ✅ 相关性算法：关键词重叠度 + 内容相似度评分
- ✅ Prompt Engineering：动态注入检索结果，明确信息来源边界

### 2. 异步编程与性能优化
- ✅ @Async 注解 + 自定义线程池（CPU 核心数 * 2）
- ✅ 索引重建响应时间：5-10 秒 → 200ms（优化 95%+）
- ✅ CompletableFuture 支持链式调用和异常处理
- ✅ 拒绝策略：CallerRunsPolicy 保证任务不丢失
- ✅ 优雅关闭：等待任务完成后再关闭线程池

### 3. API 限流与服务治理
- ✅ 三级限流策略：全局（1000 QPS）+ 接口（50 QPS）+ 用户（100 QPS）
- ✅ Guava RateLimiter 令牌桶算法，平滑限流
- ✅ ConcurrentHashMap 动态创建用户限流器
- ✅ HTTP 429 状态码 + 友好错误提示
- ✅ 有效防止 DDoS 攻击和恶意请求

### 4. JWT 认证与权限控制
- ✅ 无状态认证：JWT Token，服务端无需维护 Session
- ✅ Spring Security 集成，接口级权限校验
- ✅ 数据隔离：所有表都有 user_id 字段，防止越权访问
- ✅ BCrypt 密码加密，Token 24 小时自动过期
- ✅ @AuthenticationPrincipal 自动注入当前用户

### 5. 多轮对话与上下文管理
- ✅ 双存储策略：内存（ConcurrentHashMap）+ 数据库（MySQL）
- ✅ 滑动窗口：保留最近 20 条消息，避免 Token 超限
- ✅ 会话隔离：每个会话独立管理上下文
- ✅ 模式切换：default（纯 AI）/ local（RAG 增强）
- ✅ 流式响应：SSE 实时推送，类似 ChatGPT 打字效果

### 6. 文件管理与数据一致性
- ✅ 双存储模式：开发环境（本地）+ 生产环境（阿里云 OSS）
- ✅ 文件生命周期管理：上传（唯一 ossKey）+ 删除（级联删除）
- ✅ 异步索引更新：增删改后自动触发索引重建
- ✅ 外键约束：保证数据一致性
- ✅ 全局异常处理：统一错误响应格式

### 7. 代码质量与工程化
- ✅ MyBatis-Plus：LambdaQueryWrapper 类型安全查询
- ✅ Knife4j：自动生成 API 文档，支持在线测试
- ✅ Redis 缓存：验证码缓存，可扩展为会话缓存
- ✅ 日志记录：记录关键操作和异常堆栈

---

## 📊 性能指标

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 索引重建响应时间 | 5-10 秒 | 200ms | **95%+** |
| 系统 QPS 承载能力 | 无限制 | 1000+ | **防护提升** |
| AI 接口保护 | 无 | 50 QPS | **成本控制** |
| 用户级限流 | 无 | 100 QPS | **防滥用** |

---

## License

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

##  贡献

欢迎提交 Issue 和 Pull Request！

##  联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue

**⭐ 如果这个项目对你有帮助，请给一个 Star！⭐**

