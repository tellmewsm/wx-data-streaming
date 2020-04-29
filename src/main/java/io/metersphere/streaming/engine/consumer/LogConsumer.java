package io.metersphere.streaming.engine.consumer;

import io.metersphere.streaming.service.LogResultService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LogConsumer {
    public static final String CONSUME_ID = "log-data";
    @Resource
    private LogResultService logResultService;

    @KafkaListener(id = CONSUME_ID, topics = "${kafka.log.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) throws Exception {
        logResultService.save(record.value());
    }
}
