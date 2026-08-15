package com.flowforge.backend.workflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowRateLimitService {

    private final StringRedisTemplate redisTemplate;

    @Value("${flowforge.rate-limit.workflow-execution.max-requests:10}")
    private int maxRequests;

    @Value("${flowforge.rate-limit.workflow-execution.window-seconds:60}")
    private long windowSeconds;

    public boolean isAllowed(UUID userId) {

        String key =
            "flowforge:rate-limit:workflow-execution:"
                + userId;

        Long count =
            redisTemplate.opsForValue().increment(key);

        if (count == null) {
            return false;
        }

        if (count == 1) {
            redisTemplate.expire(
                key,
                Duration.ofSeconds(windowSeconds)
            );
        }

        return count <= maxRequests;
    }
}
