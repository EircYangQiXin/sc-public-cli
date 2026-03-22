# sc-common-redis

## 模块定位
对 `spring-data-redis` 做了常用扩展：Jackson 序列化的 `RedisTemplate`、Redisson 锁、通用缓存/限流工具、重复提交防护，适用于需要缓存、分布式锁、重复提交校验的业务场景。

## 核心组件
- `com.sc.common.redis.config.RedisConfig`：在 `RedisAutoConfiguration` 之前注册 `RedisTemplate<String, Object>`，默认使用 Jackson+JavaTimeModule 处理值，Key/HashKey 采用 `StringRedisSerializer`。
- `com.sc.common.redis.utils.CacheUtils`：封装字符串/Hash/List/Set、Redisson 锁、限流（利用 ZSET）等方法，组件内部复用 `RedisTemplate` + `RedissonClient`。
- `com.sc.common.redis.aspect.RepeatSubmitAspect` + `@RepeatSubmit`：在 Controller 方法上添加注解即可防止短时间内重复提交，默认 `interval=5s`，抛出 `ServiceException`。
- `com.sc.common.redis.utils.RedisKeyUtils`：辅助构造统一 Key 策略（如缓存前缀/日期）便于清理。

## 依赖方式
```xml
<dependency>
  <groupId>com.sc</groupId>
  <artifactId>sc-common-redis</artifactId>
</dependency>
```

## 使用说明
- 直接注入 `CacheUtils` 调用 `set/get/delete`，也支持 `getLock/tryLock/unlock` 操作。
- 控制器方法加上 `@RepeatSubmit(interval = 3, message = "请稍后再试")`，在 3 秒内重复请求会抛出 `ServiceException`，`RepeatSubmitAspect` 自动拼装 Key = `repeat_submit:{userId}:{method}`。
- `RateLimiter` 通过 `CacheUtils.rateLimiter(key, maxCount, windowSeconds)` 实现滑动窗口分布式限流。

## 注意事项
- 为了保证 Redisson 客户端可用，需要在应用配置中正确注入 `RedisConnectionFactory`/`RedissonClient`。
- `@RepeatSubmit` 依赖 `SecurityContextHolder` 获取用户 ID，匿名请求会使用请求 IP 拼 Key。
