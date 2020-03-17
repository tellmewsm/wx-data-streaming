package io.metersphere.streaming.engine.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.streaming.model.Metric;
import io.metersphere.streaming.service.TestResultService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class Consumer {
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private TestResultService testResultService;

    @KafkaListener(topics = "${kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) throws Exception {
        Metric metric = objectMapper.readValue(record.value(), Metric.class);
        testResultService.save(metric);
    }
}
