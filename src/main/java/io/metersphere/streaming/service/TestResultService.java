package io.metersphere.streaming.service;

import io.metersphere.streaming.base.domain.LoadTestReport;
import io.metersphere.streaming.base.domain.LoadTestReportExample;
import io.metersphere.streaming.base.mapper.LoadTestReportMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.streaming.commons.constants.TestStatus;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.model.Metric;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestResultService {
    private static final String HEADERS = "timestamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;

    public void save(Metric metric) throws UnsupportedEncodingException {
        LoadTestReport record = new LoadTestReport();
        String testId = metric.getTestId();
        record.setTestId(testId);
        long createTime = metric.getTestStartTime();
        record.setCreateTime(createTime);
        record.setUpdateTime(createTime);
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andTestIdEqualTo(testId)
                .andCreateTimeEqualTo(createTime);
        // 一个jmx同时只能开启一次
        List<LoadTestReport> reports = loadTestReportMapper.selectByExample(example);
        if (reports.size() == 1) {
            LoadTestReport report = reports.get(0);
            extLoadTestReportMapper.appendLine(report.getId(), convertToLine(metric));
        } else if (reports.size() == 0) {
            record.setId(UUID.randomUUID().toString());
            record.setName(metric.getTestName());
            record.setContent(HEADERS);
            record.setStatus(TestStatus.Running.name());
            loadTestReportMapper.insert(record);
            // 补充内容
            extLoadTestReportMapper.appendLine(record.getId(), "\n" + convertToLine(metric));
        }
        LogUtil.debug(String.format("A consumed message -> %s", metric));
    }

    private String convertToLine(Metric metric) throws UnsupportedEncodingException {
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
        // 处理url换行问题
        if (StringUtils.isNotBlank(metric.getUrl())) {
            content.append(URLEncoder.encode(metric.getUrl(), "UTF-8")).append(",");
        } else {
            content.append(",");
        }
        content.append(metric.getLatency()).append(",");
        content.append(metric.getIdleTime()).append(",");
        content.append(metric.getConnectTime()).append("\n");
        return content.toString();
    }
}
