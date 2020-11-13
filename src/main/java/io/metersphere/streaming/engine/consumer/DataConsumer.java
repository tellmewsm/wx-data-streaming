package io.metersphere.streaming.engine.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.model.Metric;
import io.metersphere.streaming.service.TestResultService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class DataConsumer {
    public static final String HEADERS = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";

    public static final String CONSUME_ID = "metric-data";
    public static final Integer QUEUE_SIZE = 1000;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private TestResultService testResultService;
    private final CopyOnWriteArrayList<Metric> metrics = new CopyOnWriteArrayList<>();
    private final BlockingQueue<Metric> metricQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private boolean isRunning = true;

    public static final String TEMP_DIRECTORY_PATH = FileUtils.getTempDirectoryPath();

    static {
        LogUtil.info("Temp dir: " + TEMP_DIRECTORY_PATH);
    }

    @KafkaListener(id = CONSUME_ID, topics = "${kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) throws Exception {
        Metric metric = objectMapper.readValue(record.value(), Metric.class);
        if (StringUtils.contains(metric.getThreadName(), "tearDown Thread Group")) {
            // 收到结束信息时 save
            save();
            testResultService.completeReport(metric);
            return;
        }
        // 保存jtl文件
        appendJtl(metric);
        metricQueue.put(metric);
    }

    private void appendJtl(Metric metric) throws IOException {
        File file = new File(TEMP_DIRECTORY_PATH + File.separator + metric.getReportId() + ".jtl");
        if (file.exists()) {
            FileUtils.writeStringToFile(file, testResultService.convertToLine(metric), StandardCharsets.UTF_8, true);
        } else {
            FileUtils.writeStringToFile(file, HEADERS + "\n", StandardCharsets.UTF_8, true);
        }
    }

    @PreDestroy
    public void preDestroy() {
        isRunning = false;
    }

    @PostConstruct
    public void handleQueue() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    Metric metric = metricQueue.take();
                    metrics.add(metric);
                    // 长度达到 queue_size save 一次
                    int size = metrics.size();
                    if (size >= QUEUE_SIZE) {
                        save();
                    }
                } catch (Exception e) {
                    LogUtil.error("handle queue error: ", e);
                }
            }
        }).start();
    }

    @PostConstruct
    public void handleSave() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    // 确保 metrics 全部被保存
                    int size = metrics.size();
                    if (metricQueue.isEmpty() && size > 0 && size < QUEUE_SIZE) {
                        save();
                    }
                    Thread.sleep(20 * 1000);
                } catch (Exception e) {
                    LogUtil.error("handle save error: ", e);
                }
            }
        }).start();
    }


    public synchronized void save() {
        LogUtil.info("save metrics size: " + metrics.size());
        Map<String, List<Metric>> reportMetrics = metrics.stream().collect(Collectors.groupingBy(Metric::getReportId));
        reportMetrics.forEach((reportId, metrics) -> {
            String testId = "";
            StringBuilder content = new StringBuilder();
            for (Metric metric : metrics) {
                content.append(testResultService.convertToLine(metric));
                testId = metric.getTestId();
            }
            testResultService.savePartContent(reportId, testId, content.toString());
        });
        // 清空 list
        metrics.clear();
    }
}
