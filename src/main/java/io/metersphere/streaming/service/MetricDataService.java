package io.metersphere.streaming.service;

import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.engine.consumer.MetricData;
import io.metersphere.streaming.model.Metric;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class MetricDataService {
    private final MetricData metricData = new MetricData();

    @Resource
    private TestResultService testResultService;

    public synchronized int save() {
        CopyOnWriteArrayList<Metric> metricList = metricData.getMetrics();
        int size = metricList.size();
        LogUtil.info("save metrics size: " + size);
        Map<String, List<Metric>> reportMetrics = metricList.stream().collect(Collectors.groupingBy(Metric::getReportId));
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
        metricList.clear();
        return size;
    }

    public void addToMetricQueue(Metric metric) throws InterruptedException {
        metricData.getMetricQueue().put(metric);
    }

    public void addToMetricList(Metric metric) {
        metricData.getMetrics().add(metric);
    }

    public CopyOnWriteArrayList<Metric> getMetricList() {
        return metricData.getMetrics();
    }

    public BlockingQueue<Metric> getMetricQueue() {
        return metricData.getMetricQueue();
    }
}
