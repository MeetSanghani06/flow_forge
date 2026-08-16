package com.flowforge.backend.workflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowGraphCacheService {

    private final CacheManager cacheManager;

    public void logCacheStatus(
        UUID workflowId,
        int versionNumber
    ) {

        String key =
            workflowId + ":" + versionNumber;

        Cache cache =
            cacheManager.getCache("workflow-graphs");

        if (cache == null) {
            log.warn(
                "WORKFLOW_GRAPH_CACHE_NOT_CONFIGURED"
            );
            return;
        }

        Cache.ValueWrapper value =
            cache.get(key);

        if (value != null) {

            log.info(
                "WORKFLOW_GRAPH_CACHE_HIT | key={}",
                key
            );

        } else {

            log.info(
                "WORKFLOW_GRAPH_CACHE_MISS | key={}",
                key
            );
        }
    }
}
