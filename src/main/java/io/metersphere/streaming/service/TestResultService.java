package io.metersphere.streaming.service;

import io.metersphere.streaming.base.domain.LoadTestReport;
import io.metersphere.streaming.base.domain.LoadTestReportExample;
import io.metersphere.streaming.base.mapper.LoadTestReportMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.streaming.commons.constants.TestStatus;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.model.Metric;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestResultService {
    private static final String HEADERS = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n";
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;

    public void save(Metric metric) {
        LoadTestReport record = new LoadTestReport();
        record.setTestId(metric.getTestId());
        long createTime = metric.getTestStartTime();
        record.setCreateTime(createTime);
        record.setUpdateTime(createTime);
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andTestIdEqualTo(metric.getTestId())
                .andCreateTimeEqualTo(createTime);
        // 一个jmx同时只能开启一次
        List<LoadTestReport> reports = loadTestReportMapper.selectByExample(example);
        if (reports.size() == 1) {
            LoadTestReport report = reports.get(0);
            extLoadTestReportMapper.appendLine(report.getId(), convertToLine(metric));
        } else if (reports.size() == 0) {
            record.setId(UUID.randomUUID().toString());
            String content = HEADERS + convertToLine(metric);
            record.setContent(content);
            record.setStatus(TestStatus.Running.name());
            loadTestReportMapper.insert(record);
        }
        LogUtil.debug(String.format("A consumed message -> %s", metric));
    }

    private String convertToLine(Metric metric) {
        //timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
        StringBuilder content = new StringBuilder();
        content.append(metric.getTimestamp()).append(",");
        content.append(metric.getElapsedTime()).append(",");
        content.append(metric.getSampleLabel()).append(",");
        content.append(metric.getResponseCode()).append(",");
        content.append(metric.getResponseCode()).append(",");
        content.append(metric.getThreadName()).append(",");
        content.append(metric.getDataType()).append(",");
        content.append(metric.getSuccess()).append(",");
        content.append(metric.getFailureMessage()).append(",");
        content.append(metric.getBytes()).append(",");
        content.append(metric.getSentBytes()).append(",");
        content.append(metric.getGrpThreads()).append(",");
        content.append(metric.getAllThreads()).append(",");
        content.append(metric.getUrl()).append(",");
        content.append(metric.getLatency()).append(",");
        content.append(metric.getIdleTime()).append(",");
        content.append(metric.getConnectTime()).append("\n");
        return content.toString();
    }
}
