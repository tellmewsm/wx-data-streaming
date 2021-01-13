package io.metersphere.streaming.engine.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.streaming.base.domain.LoadTest;
import io.metersphere.streaming.commons.utils.LogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LoadTestProducer {

    @Value("${kafka.test.topic}")
    private String topic;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Resource
    private ObjectMapper objectMapper;

    public void sendMessage(LoadTest loadTest) {
        if (loadTest == null) {
            LogUtil.error("当前测试已删除");
            return;
        }
        try {
            this.kafkaTemplate.send(topic, objectMapper.writeValueAsString(loadTest));
        } catch (JsonProcessingException e) {
            LogUtil.error("发送成功通知失败", e);
        }
    }
}
