package io.metersphere.streaming.producer;

import io.metersphere.streaming.commons.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class Producer {

    @Value("${kafka.topic}")
    private String topic;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String message) {
        LogUtil.info(String.format("#### -> Producing message -> %s", message));
        this.kafkaTemplate.send(topic, message);
    }
}
