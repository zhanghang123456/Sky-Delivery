package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    /**
     * 创建redis模板对象
     * RedisConnectionFactor在导入依赖后，由Spring自动创建并注入
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis模板对象...");

        RedisTemplate redisTemplate = new RedisTemplate();

        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //设置redis key的反序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
