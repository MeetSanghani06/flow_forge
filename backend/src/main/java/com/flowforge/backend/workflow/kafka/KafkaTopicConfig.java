package com.flowforge.backend.workflow.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic workflowExecutionTopic(
        @Value("${flowforge.kafka.workflow-execution-topic}")
        String topic
    ) {
        return new NewTopic(
            topic,
            3,
            (short) 1
        );
    }
}
