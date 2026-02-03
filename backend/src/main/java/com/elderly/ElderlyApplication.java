package com.elderly;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
// @EnableAspectJAutoProxy  <-- 删除这一行，SpringBoot 默认会配置最好的代理模式
public class ElderlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElderlyApplication.class, args);
    }

    /**
     * 启动时进行 Redis 连接测试
     * 如果这里报错，说明 Redis 配置有问题
     * 如果这里成功，说明 Redis 连接没问题，是 @Cacheable 注解没生效
     */
    @Bean
    public CommandLineRunner testRedisConnection(StringRedisTemplate redisTemplate, CacheManager cacheManager) {
        return args -> {
            System.out.println("====== [1/2] 开始 Redis 连接测试 ======");
            try {
                // 1. 测试直接写入
                redisTemplate.opsForValue().set("test:connection", "Hello Redis from SpringBoot");
                String value = redisTemplate.opsForValue().get("test:connection");
                System.out.println("====== [2/2] Redis 连接成功！读写测试结果: " + value + " ======");

                // 2. 打印缓存管理器信息
                System.out.println("====== 当前缓存管理器: " + cacheManager.getClass().getName() + " ======");

            } catch (Exception e) {
                System.err.println("====== 严重错误：Redis 连接失败 ======");
                e.printStackTrace();
            }
        };
    }
}
