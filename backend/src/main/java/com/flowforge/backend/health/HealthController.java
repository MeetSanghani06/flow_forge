package com.flowforge.backend.health;
import com.flowforge.backend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<?> health() {

        return ApiResponse.success(
            Map.of(
                "application", "FlowForge",
                "status", "UP"
            )
        );

    }

}
