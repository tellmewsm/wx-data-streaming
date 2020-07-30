package io.metersphere.streaming.service;

import io.metersphere.streaming.base.domain.LoadTestReport;
import io.metersphere.streaming.base.domain.LoadTestReportDetail;
import io.metersphere.streaming.base.domain.LoadTestReportDetailExample;
import io.metersphere.streaming.base.domain.LoadTestWithBLOBs;
import io.metersphere.streaming.base.mapper.LoadTestMapper;
import io.metersphere.streaming.base.mapper.LoadTestReportDetailMapper;
import io.metersphere.streaming.base.mapper.LoadTestReportMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.streaming.commons.constants.TestStatus;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.model.Metric;
import io.metersphere.streaming.report.ReportGeneratorFactory;
import io.metersphere.streaming.report.impl.AbstractReport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TestResultService {
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private LoadTestReportDetailMapper loadTestReportDetailMapper;
    @Resource
    private TestResultSaveService testResultSaveService;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;

    ExecutorService completeThreadPool = Executors.newFixedThreadPool(10);
    ExecutorService reportThreadPool = Executors.newFixedThreadPool(30);

    public void savePartContent(String reportId, String testId, String content) {
        // 更新状态
        extLoadTestReportMapper.updateStatus(reportId, TestStatus.Running.name(), TestStatus.Starting.name());
        extLoadTestMapper.updateStatus(testId, TestStatus.Running.name(), TestStatus.Starting.name());

        LoadTestReportDetailExample example = new LoadTestReportDetailExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        long part = loadTestReportDetailMapper.countByExample(example);
        LoadTestReportDetail record = new LoadTestReportDetail();
        record.setReportId(reportId);
        record.setPart(part + 1);
        record.setContent(content);
        loadTestReportDetailMapper.insert(record);

        // 计算结果
        completeThreadPool.execute(() -> generateReport(reportId));
    }

    public String convertToLine(Metric metric) {
        //timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
        long start = metric.getTimestamp().getTime();
        StringBuilder content = new StringBuilder();
        content.append(start).append(",");
        content.append(metric.getResponseTime()).append(",");
        content.append(metric.getSampleLabel()).append(",");
        content.append(metric.getResponseCode()).append(",");
        // response message
        content.append(",");
        content.append(metric.getThreadName()).append(",");
        content.append(metric.getDataType()).append(",");
        content.append(metric.getSuccess()).append(",");
        // failure message contains \n
        String message = convertFailureMessage(metric);
        content.append(message).append(",");
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

    private String convertFailureMessage(Metric metric) {
        String message = StringUtils.remove(metric.getFailureMessage(), "\n");
        message = StringUtils.replace(message, ",", " ");
        return message;
    }

    public void completeReport(Metric metric) {
        LoadTestReport report = loadTestReportMapper.selectByPrimaryKey(metric.getReportId());
        if (report == null) {
            LogUtil.info("Report is null.");
            return;
        }

        // 更新测试的状态
        LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
        loadTest.setId(report.getTestId());
        loadTest.setStatus(TestStatus.Completed.name());
        loadTestMapper.updateByPrimaryKeySelective(loadTest);
        LogUtil.info("test completed: " + report.getTestId());

        // 确保计算报告完全执行
        generateReportComplete(report.getId());
    }

    public void generateReport(String reportId) {
        // 检查 report_status
        boolean reporting = testResultSaveService.isReporting(reportId);

        generateReport(reportId, reporting);
    }

    private void generateReportComplete(String reportId) {
        //
        generateReport(reportId, true); // 强制执行一次生成报告

        LoadTestReport report = new LoadTestReport();
        report.setId(reportId);
        report.setUpdateTime(System.currentTimeMillis());
        // 测试结束后保存状态
        report.setStatus(TestStatus.Completed.name());
        loadTestReportMapper.updateByPrimaryKeySelective(report);
    }

    private void generateReport(String reportId, boolean isForce) {
        if (!isForce) {
            LogUtil.info("report generator is running.");
//            return;
        }
        List<AbstractReport> reportGenerators = ReportGeneratorFactory.getReportGenerators();
        LogUtil.info("report generators size: {}", reportGenerators.size());
        CountDownLatch countDownLatch = new CountDownLatch(reportGenerators.size());
        reportGenerators.forEach(r -> reportThreadPool.execute(() -> {
            LogUtil.info("Report Key: " + r.getReportKey());
            r.init(reportId);
            try {
                r.execute();
            } finally {
                countDownLatch.countDown();
            }
        }));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LogUtil.error(e);
        } finally {
            testResultSaveService.saveReportReadyStatus(reportId);
        }
    }
}
