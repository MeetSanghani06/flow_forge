package com.flowforge.backend.workflow.execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration LOCK_TTL =
        Duration.ofMinutes(10);

    public String tryAcquire(String key) {

        String lockValue =
            UUID.randomUUID().toString();

        Boolean acquired =
            redisTemplate.opsForValue().setIfAbsent(
                key,
                lockValue,
                LOCK_TTL
            );

        if (Boolean.TRUE.equals(acquired)) {

            log.info(
                "DISTRIBUTED_LOCK_ACQUIRED | key={}",
                key
            );

            return lockValue;
        }

        log.info(
            "DISTRIBUTED_LOCK_NOT_ACQUIRED | key={}",
            key
        );

        return null;
    }

    public void release(
        String key,
        String lockValue
    ) {

        String currentValue =
            redisTemplate.opsForValue().get(key);

        if (lockValue.equals(currentValue)) {

            redisTemplate.delete(key);

            log.info(
                "DISTRIBUTED_LOCK_RELEASED | key={}",
                key
            );
        }
    }
}
