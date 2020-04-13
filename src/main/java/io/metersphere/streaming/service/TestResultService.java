package io.metersphere.streaming.service;

import io.metersphere.streaming.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.streaming.base.domain.LoadTestWithBLOBs;
import io.metersphere.streaming.base.mapper.LoadTestMapper;
import io.metersphere.streaming.base.mapper.LoadTestReportDetailMapper;
import io.metersphere.streaming.base.mapper.LoadTestReportMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportDetailMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.streaming.commons.constants.TestStatus;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.model.Metric;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

@Service
public class TestResultService {
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private LoadTestReportDetailMapper loadTestReportDetailMapper;
    @Resource
    private ExtLoadTestReportDetailMapper extLoadTestReportDetailMapper;

    public void save(Metric metric) {
        // 如果 testid 为空，无法关联到test，此条消息作废
        if (StringUtils.isBlank(metric.getTestId())) {
            return;
        }
        if (StringUtils.isBlank(metric.getReportId())) {
            LogUtil.warn("ReportId is null");
            return;
        }
        if (StringUtils.contains(metric.getThreadName(), "tearDown Thread Group")) {
            LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey(metric.getReportId());
            LogUtil.info("test tearDown message received, report:{}, test:{} ", report.getId(), report.getTestId());
            report.setUpdateTime(System.currentTimeMillis());
            report.setStatus(TestStatus.Completed.name());
            loadTestReportMapper.updateByPrimaryKeySelective(report);
            // 更新测试的状态
            LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
            loadTest.setId(metric.getTestId());
            loadTest.setStatus(TestStatus.Completed.name());
            loadTestMapper.updateByPrimaryKeySelective(loadTest);
            LogUtil.info("test completed: " + metric.getTestName());
        } else {
            extLoadTestReportMapper.appendLine(metric.getReportId(), convertToLine(metric), TestStatus.Running.name());
            extLoadTestMapper.updateStatus(metric.getTestId(), TestStatus.Running.name());
        }
    }

    public void saveDetail(Metric metric) {
        // 如果 testid 为空，无法关联到test，此条消息作废
        if (StringUtils.isBlank(metric.getTestId())) {
            return;
        }
        if (StringUtils.isBlank(metric.getReportId())) {
            LogUtil.warn("ReportId is null");
            return;
        }
        if (StringUtils.contains(metric.getThreadName(), "tearDown Thread Group")) {
            return;
        }
        extLoadTestReportDetailMapper.appendLine(metric.getReportId(), convertToLine(metric));
    }

    private String convertToLine(Metric metric) {
        //timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
        long start = metric.getTimestamp().getTime();
        Date end = metric.getElapsedTime();
        StringBuilder content = new StringBuilder();
        content.append(start).append(",");
        int elapsed = getElapsed(end);
        content.append(elapsed).append(",");
        content.append(metric.getSampleLabel()).append(",");
        content.append(metric.getResponseCode()).append(",");
        // response message
        content.append(",");
        content.append(metric.getThreadName()).append(",");
        content.append(metric.getDataType()).append(",");
        content.append(metric.getSuccess()).append(",");
        // failure message contains \n
        try {
            String message = URLEncoder.encode(metric.getFailureMessage(), "UTF-8");
            content.append(message).append(",");
        } catch (UnsupportedEncodingException e) {
            content.append("Error UnsupportedEncodingException").append(",");
        }
        content.append(metric.getBytes()).append(",");
        content.append(metric.getSentBytes()).append(",");
        content.append(metric.getGrpThreads()).append(",");
        content.append(metric.getAllThreads()).append(",");
        // 处理url换行问题
        if (StringUtils.isNotBlank(metric.getUrl())) {
            content.append(StringUtils.deleteWhitespace(metric.getUrl())).append(",");
        } else {
            content.append(",");
        }
        content.append(metric.getLatency()).append(",");
        content.append(metric.getIdleTime()).append(",");
        content.append(metric.getConnectTime()).append("\n");
        return content.toString();
    }

    private int getElapsed(Date end) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        return minutes * 60 + seconds;
    }
}
