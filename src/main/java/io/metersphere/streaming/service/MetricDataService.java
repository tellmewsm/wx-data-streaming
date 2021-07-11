package io.metersphere.streaming.service;

import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.model.Metric;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Service
public class MetricDataService {
    public static final Integer QUEUE_SIZE = 1000;

    private final List<Metric> metrics = new ArrayList<>();
    private final BlockingQueue<Metric> metricQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

    @Resource
    private TestResultService testResultService;

    public synchronized int save() {
        List<Metric> metricList = metrics;
        int size = metricList.size();
        LogUtil.info("save metrics size: " + size);
        Map<String, List<Metric>> reportMetrics = metricList.stream().collect(Collectors.groupingBy(Metric::getReportId));
        reportMetrics.forEach((reportId, metrics) -> {
            String testId = "";
            StringBuilder content = new StringBuilder();
            for (Metric metric : metrics) {
                content.append(testResultService.convertToLine(metric));
                testId = metric.getTestId();
                metricList.remove(metric);
            }
            testResultService.savePartContent(reportId, testId, content.toString());
        });
        return size;
    }

    public void addToMetricQueue(Metric metric) throws InterruptedException {
        metricQueue.put(metric);
    }

    public void addToMetricList(Metric metric) {
        metrics.add(metric);
    }

    public List<Metric> getMetricList() {
        return metrics;
    }

    public BlockingQueue<Metric> getMetricQueue() {
        return metricQueue;
    }
}
