# 面试准备文档

## 1、安全相关的配置类

### JwtUtil.java (JWT工具类)
-    这个工具类负责处理JWT令牌的所有操作：
-    生成包含用户信息（用户名、用户ID、角色）的令牌
-    解析令牌提取声明信息
-    检查令牌是否过期
-    提供获取用户ID和角色的方法

### JwtAuthenticationFilter.java (JWT认证过滤器)
-    这是一个拦截器，用于验证请求中的JWT令牌：
-    从Authorization头中提取Bearer令牌
-    使用JwtUtil验证令牌有效性
-    构建Spring Security认证对象
-    将认证信息放入安全上下文

### SecurityConfig.java (安全配置类)
-    主要的安全配置：

-    禁用CSRF保护（因为使用JWT）
-    设置无状态会话管理
-    配置授权规则（哪些路径需要认证）
-    注册JwtAuthenticationFilter
-    配置密码编码器（BCrypt）
-    交互流程：
- ​    SecurityConfig → 注册 JwtAuthenticationFilter → 使用 JwtUtil 进行令牌操作

## 2、API保护相关的配置类
### RateLimitInterceptor.java (限流拦截器)
-    实现API限流保护：
-    全局限流器（1000请求/秒）
-    AI API专用限流器（50请求/秒）
-    用户级限流（每个用户100请求/秒）
-    超限时返回429错误

### WebMvcConfig.java (Web MVC配置类)
-    将拦截器注册到Spring MVC：

-    添加RateLimitInterceptor到API路径
-    排除某些路径不受限流影响（登录、注册、文档）
-    交互流程：
-    WebMvcConfig → 注册 RateLimitInterceptor → 对请求应用限流

## 3、服务集成配置类
### OssConfig.java (OSS配置类)
-    配置阿里云对象存储服务：
-    从配置文件读取凭证
-    创建OSS客户端Bean
-    被需要存储/检索文件的服务使用

### AiConfig.java (AI配置类)
-    配置AI服务参数：
-    DashScope的API密钥
-    模型名称和温度设置
-    被EnhancedAiService用于AI交互

### CaptchaConfig.java (验证码配置类)
-    配置AJ-Captcha服务：
-    设置滑动拼图验证码
-    配置缓存类型（Redis）
-    在认证流程中使用

### AsyncConfig.java (异步配置类)
-    配置异步操作的线程池：

-    RAG索引执行器（CPU密集型任务）
-    通用任务执行器
-    用于后台处理避免阻塞请求

## 4.错误处理
### GlobalExceptionHandler.java (全局异常处理器)
-    集中处理异常：

-    参数验证异常处理
-    运行时异常处理
-    安全异常处理（权限不足等）
-    文件上传大小超限处理
-    通用异常处理

## 5、类间协作示例
- 当一个用户发起API请求时，系统按照以下顺序处理：

  - ​    * WebMvcConfig 注册的 RateLimitInterceptor 首先检查请求频率是否超过限制
  - ​    * SecurityConfig 注册的 JwtAuthenticationFilter 验证JWT令牌
  - ​    * 如果令牌有效，使用 JwtUtil 解析用户信息并建立安全上下文
  - ​    * 请求到达控制器后，如果需要调用AI服务，则使用 AiConfig 中配置的参数
  - ​    * 如果需要存储文件，则使用 OssConfig 创建的OSS客户端上传文件
  - ​    * 如果发生异常，由 GlobalExceptionHandler 统一处理并返回适当错误信息
  - ​    * 对于耗时操作（如RAG索引重建），使用 AsyncConfig 配置的线程池异步执行


## 6、关于重建索引功能以及AsyncConfig.java
* 想象你有一个图书馆，里面有100本书：
  * 没有索引（目录）的情况：
    你问管理员："有没有关于Java的书？"
    管理员需要把100本书一本一本翻开，看看内容是不是关于Java
    这要花很长时间 ⏰
    有索引（目录）的情况：
  * 管理员提前做了一张目录卡片，记录了：
    第1本书：关键词【Java, 编程, 面向对象】
    第2本书：关键词【Python, 数据分析】
    第3本书：关键词【Java, Spring, 后端】

- 自动触发机制：

    * 第一次使用AI搜索笔记时
      系统发现：咦，这个用户还没有索引
      系统自动建立索引
      然后进行搜索

    * 以后每次使用
      索引已经存在
      直接使用，速度飞快

    * 手动点击"重建索引"按钮的作用
      当你新增/修改/删除了很多笔记后
      手动更新索引，让AI能搜到最新内容
      但这不是必须的，系统会在需要时自动建立


* 那为什么还需要"重建索引"按钮？
  * 索引会过时
  * 用户应该在这些情况下点击"重建索引"： 
    * ✅ 批量导入了很多笔记
    * ✅ 修改了大量笔记内容
    * ✅ 删除了很多笔记
    * ✅ 感觉AI搜索结果不准确
  * 普通使用不需要点击： 
    * ❌ 每天正常写几篇笔记 → 不需要
    * ❌ 每次和AI对话前 → 不需要
    * ❌ 定期维护 → 不需要
    * ✅ 自动建立索引：用户无感知
    * ✅ 异步线程池：防止卡顿 
    * ✅ 手动重建按钮：给用户控制权 
    * ✅ 性能优化：从10秒降到0.2秒

## 7、redis得使用场景
 ### redis使用-存储滑动拼图验证码的临时数据
1. 对比项	    使用Redis	            不使用Redis（本地内存）
     分布式部署	✅ 支持多台服务器共享验证码	❌ 每台服务器独立存储
     自动过期  	✅ 120秒自动删除	        ❌ 需要手动清理
     持久化	    ✅ 服务重启数据不丢失	    ❌ 服务重启数据丢失
     内存管理	    ✅ Redis专业管理内存	    ❌ 占用应用服务器内存
     性能	    ✅ 高性能读写	            ✅ 本地内存更快

2. 为什么使用redis存储滑动拼图验证码

     * 分布式支持：如果项目部署多台服务器做负载均衡，
       
       使用本地缓存会导致验证码无法跨服务器验证。
       Redis作为独立的缓存服务器，所有应用服务器都可以访问同一份数据。 
       
     * 自动过期：Redis原生支持TTL（Time To Live），
       验证码设置120秒后自动删除，不需要手动清理，避免内存泄漏。 
       
     * 持久化：虽然验证码是临时数据，但Redis可以配置持久化，
       应用服务器重启不影响正在使用的验证码。 
       
     * 内存管理：Redis专门用于缓存，有完善的内存淘汰策略，
       不会占用应用服务器的JVM内存。

3. 验证码的过期时间是120秒（2分钟）
     * 用户体验：2分钟足够用户完成滑动验证，不会太短导致频繁刷新
     * 安全性：时间不能太长，防止验证码被截获后长时间有效
     * 资源释放：及时清理过期验证码，避免Redis内存占用过多
     * 可在yml文件中配置修改

4. 如果Redis宕机了，验证码功能会怎样
     * Redis宕机会导致验证码功能完全失效，用户无法登录。这是一个单点故障问题。 
     * Redis高可用方案： 
       * 使用Redis哨兵（Sentinel）模式，主节点宕机自动切换到从节点 
       * 使用Redis集群（Cluster）模式，数据分片存储 
       * 降级方案： 
       * 配置文件中可以切换为本地缓存：cache-type: local 
       * 监控Redis状态，宕机时自动切换到本地缓存模式 
       * 业务降级： 
       * 临时关闭验证码功能，允许用户直接登录（风险较高） 
       * 使用其他验证方式，如短信验证码

5. Redis中验证码数据的Key是怎么设计的
     * AJ-Captcha组件自动生成Key：captcha:verification:uuid
     * 前缀：captcha:verification: 用于区分不同业务
     * 唯一标识：使用UUID保证每个验证码唯一 
     * 可读性：通过前缀可以快速识别是验证码相关数据

6. redis得【扩展前景】：
     * 用户会话缓存：
       * 存储用户登录状态和权限信息
       * Key: session:userId:xxx
       * TTL: 30分钟（滑动过期） 
     * 热点数据缓存： 
       * 缓存高频访问的笔记和思维导图
       * 减少数据库查询压力
       * Key: note:id:123, mindmap:id:456
     * 限流控制：
       * 使用Redis的INCR命令实现接口限流
       * Key: ratelimit:userId:xxx:api
       * 防止用户频繁调用AI接口

7. dis有两种持久化策略：
     * 1. RDB（Redis Database）快照：
       * 定期将内存数据保存到磁盘
       * 优点：恢复速度快、文件小
       * 缺点：可能丢失最后一次快照后的数据
     * 2. AOF（Append Only File）日志：
       * 记录每一条写命令
       * 优点：数据完整性高
       * 缺点：文件大、恢复慢
     * 我的项目配置： 

       * 定期将内存数据保存到磁盘：
         * 优点：恢复速度快、文件小
         * 缺点：可能丢失最后一次快照后的数据
       * AOF（Append Only File）日志：
         * 记录每一条写命令
         * 优点：数据完整性高
         * 缺点：文件大、恢复慢
     * 我的项目配置：
       * 使用默认的RDB策略 
       * 因为验证码是临时数据，丢失也不影响业务 
       * 如果扩展到用户会话缓存，建议开启AOF保证数据安全

8. 如何监控Redis的性能？
      * **用监控指标：**
           1. 内存使用率
              - 命令：`INFO memory`
              - 关注：`used_memory`, `maxmemory`
           2. 命令执行统计
              - 命令：`INFO stats`
              - 关注：`total_commands_processed`, `instantaneous_ops_per_sec`
           3. 慢查询日志
              - 命令：`SLOWLOG GET 10`
              - 查看执行时间超过阈值的命令
           4. Key数量
              - 命令：`DBSIZE`
              - 监控Key的增长趋势
              **监控工具：**
           - Redis自带的`redis-cli --stat`
           - RedisInsight（官方GUI工具）
           - Prometheus + Grafana（生产环境推荐）
           - 云服务商的监控面板（阿里云、腾讯云）

### redis使用-**用户会话缓存** 

- **作用**: 减少数据库查询，提升90%响应速度
- **缓存Key**: `user:session:userId:{userId}`
- **过期时间**: 30分钟
- **性能提升**: 从50ms降至5ms

### redis使用-**热点笔记缓存**

- **作用**: 提升AI搜索速度5-10倍
- 缓存Key
  - `note:detail:id:{noteId}` - 笔记详情
  - `note:content:id:{noteId}` - 笔记内容
  - `note:list:userId:{userId}` - 用户笔记列表
- **过期时间**: 10分钟
- **性能提升**: 笔记列表加载从200ms降至20ms

### redis使用-**接口限流**

- **作用**: 防止AI接口被滥用，控制成本
- 实现：RateLimit注解 + RateLimitAspect切面
- **限流规则**: AI接口每分钟最多10次
- **效果**: 有效防止恶意刷接口
