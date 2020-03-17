package io.metersphere.streaming.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@PropertySource("file:/opt/fit2cloud/conf/data-streaming.properties")
public class KafkaConfig {

    @Value("${topic.name}")
    private String topic;

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }
}
