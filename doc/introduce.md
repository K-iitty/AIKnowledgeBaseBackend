# 面试准备文档

## 1、安全相关的配置类

### JwtUtil.java (JWT工具类)
   这个工具类负责处理JWT令牌的所有操作：
   生成包含用户信息（用户名、用户ID、角色）的令牌
   解析令牌提取声明信息
   检查令牌是否过期
   提供获取用户ID和角色的方法
### JwtAuthenticationFilter.java (JWT认证过滤器)
   这是一个拦截器，用于验证请求中的JWT令牌：
   从Authorization头中提取Bearer令牌
   使用JwtUtil验证令牌有效性
   构建Spring Security认证对象
   将认证信息放入安全上下文
### SecurityConfig.java (安全配置类)
   主要的安全配置：
   禁用CSRF保护（因为使用JWT）
   设置无状态会话管理
   配置授权规则（哪些路径需要认证）
   注册JwtAuthenticationFilter
   配置密码编码器（BCrypt）
   交互流程：
   SecurityConfig → 注册 JwtAuthenticationFilter → 使用 JwtUtil 进行令牌操作

## 2、API保护相关的配置类
### RateLimitInterceptor.java (限流拦截器)
   实现API限流保护：
   全局限流器（1000请求/秒）
   AI API专用限流器（50请求/秒）
   用户级限流（每个用户100请求/秒）
   超限时返回429错误
### WebMvcConfig.java (Web MVC配置类)
   将拦截器注册到Spring MVC：
   添加RateLimitInterceptor到API路径
   排除某些路径不受限流影响（登录、注册、文档）
   交互流程：
   WebMvcConfig → 注册 RateLimitInterceptor → 对请求应用限流

## 3、服务集成配置类
### OssConfig.java (OSS配置类)
   配置阿里云对象存储服务：
   从配置文件读取凭证
   创建OSS客户端Bean
   被需要存储/检索文件的服务使用
### AiConfig.java (AI配置类)
   配置AI服务参数：
   DashScope的API密钥
   模型名称和温度设置
   被EnhancedAiService用于AI交互
### CaptchaConfig.java (验证码配置类)
   配置AJ-Captcha服务：
   设置滑动拼图验证码
   配置缓存类型（Redis）
   在认证流程中使用
### AsyncConfig.java (异步配置类)
   配置异步操作的线程池：
   RAG索引执行器（CPU密集型任务）
   通用任务执行器
   用于后台处理避免阻塞请求

## 4.错误处理
### GlobalExceptionHandler.java (全局异常处理器)
   集中处理异常：
   参数验证异常处理
   运行时异常处理
   安全异常处理（权限不足等）
   文件上传大小超限处理
   通用异常处理
   
## 5、类间协作示例
当一个用户发起API请求时，系统按照以下顺序处理：
    * WebMvcConfig 注册的 RateLimitInterceptor 首先检查请求频率是否超过限制
    * SecurityConfig 注册的 JwtAuthenticationFilter 验证JWT令牌
    * 如果令牌有效，使用 JwtUtil 解析用户信息并建立安全上下文
    * 请求到达控制器后，如果需要调用AI服务，则使用 AiConfig 中配置的参数
    * 如果需要存储文件，则使用 OssConfig 创建的OSS客户端上传文件
    * 如果发生异常，由 GlobalExceptionHandler 统一处理并返回适当错误信息
    * 对于耗时操作（如RAG索引重建），使用 AsyncConfig 配置的线程池异步执行
