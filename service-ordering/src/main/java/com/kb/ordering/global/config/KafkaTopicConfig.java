package com.kb.ordering.global.config;

import com.kb.contracts.ordering.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderConfirmedTopic() {
        return TopicBuilder.name(Topics.ORDER_CONFIRMED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic warehouseAssignedTopic() {
        return TopicBuilder.name(Topics.WAREHOUSE_ASSIGNED).partitions(3).replicas(1).build();
    }
}