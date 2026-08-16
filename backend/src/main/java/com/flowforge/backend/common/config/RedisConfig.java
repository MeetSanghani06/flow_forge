package com.flowforge.backend.common.config;

import com.flowforge.backend.workflow.dto.WorkflowGraphResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public RedisCacheManager cacheManager(
        RedisConnectionFactory connectionFactory
    ) {

        RedisCacheWriter cacheWriter =
            RedisCacheWriter.nonLockingRedisCacheWriter(
                connectionFactory
            );

        RedisCacheConfiguration defaultConfig =
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        JacksonJsonRedisSerializer<WorkflowGraphResponse> serializer =
            new JacksonJsonRedisSerializer<>(
                objectMapper,
                WorkflowGraphResponse.class
            );

        RedisCacheConfiguration graphConfig =
            defaultConfig.serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(serializer)
            );

        return RedisCacheManager.builder(cacheWriter)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration(
                "workflow-graphs",
                graphConfig
            )
            .build();
    }
}
