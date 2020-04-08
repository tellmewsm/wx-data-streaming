package io.metersphere.streaming.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@PropertySource("file:/opt/fit2cloud/conf/metersphere.properties")
public class KafkaConfig {

    @Value("${kafka.topic}")
    private String topic;
    @Value("${kafka.partitions:1}")
    private Integer partitions;
    @Value("${kafka.replicas:1}")
    private Integer replicas;

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
