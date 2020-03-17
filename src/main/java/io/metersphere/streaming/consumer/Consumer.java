package io.metersphere.streaming.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.streaming.model.Metric;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
@Slf4j
public class Consumer {
    @Resource
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) throws IOException {
        Metric value = objectMapper.readValue(record.value(), Metric.class);
        log.info(String.format("A consumed message -> %s", value));
    }
}
