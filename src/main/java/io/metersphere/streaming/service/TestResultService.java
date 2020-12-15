package io.metersphere.streaming.service;

import io.metersphere.streaming.base.domain.*;
import io.metersphere.streaming.base.mapper.LoadTestMapper;
import io.metersphere.streaming.base.mapper.LoadTestReportDetailMapper;
import io.metersphere.streaming.base.mapper.LoadTestReportMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.streaming.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.streaming.commons.constants.TestStatus;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.engine.consumer.DataConsumer;
import io.metersphere.streaming.model.Metric;
import io.metersphere.streaming.report.ReportGeneratorFactory;
import io.metersphere.streaming.report.impl.AbstractReport;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.processor.SampleContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;
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
    @Resource
    private FileService fileService;

    ExecutorService completeThreadPool = Executors.newFixedThreadPool(10);
    ExecutorService reportThreadPool = Executors.newFixedThreadPool(30);

    @Transactional(rollbackFor = Exception.class)
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
        content.append(warp(metric.getSampleLabel())).append(",");
        content.append(metric.getResponseCode()).append(",");
        // response message
        content.append(",");
        content.append(metric.getThreadName()).append(",");
        content.append(metric.getDataType()).append(",");
        content.append(metric.getSuccess()).append(",");
        // failure message contains \n , etc.
        content.append(warp(convertFailureMessage(metric))).append(",");
        content.append(metric.getBytes()).append(",");
        content.append(metric.getSentBytes()).append(",");
        content.append(metric.getGrpThreads()).append(",");
        content.append(metric.getAllThreads()).append(",");
        // 处理url换行问题
        if (StringUtils.isNotBlank(metric.getUrl())) {
            content.append(warp(StringUtils.deleteWhitespace(metric.getUrl()))).append(",");
        } else {
            content.append(",");
        }
        content.append(metric.getLatency()).append(",");
        content.append(metric.getIdleTime()).append(",");
        content.append(metric.getConnectTime()).append("\n");
        return content.toString();
    }

    private String warp(String value) {
        // 1 先处理是否包含双引号
        if (StringUtils.contains(value, "\"")) {
            value = StringUtils.replace(value, "\"", "\"\"");
        }
        // 2 然后处理是否包含逗号、 \n
        if (StringUtils.contains(value, ",") || StringUtils.contains(value, "\n")) {
            value = StringUtils.wrapIfMissing(value, "\"");
        }
        // 返回结果
        return value;
    }

    private String convertFailureMessage(Metric metric) {
        return StringUtils.remove(metric.getFailureMessage(), "\n");
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
        // 保存jtl
        saveJtlFile(metric);
        // 确保计算报告完全执行
        completeThreadPool.execute(() -> generateReportComplete(report.getId()));
    }

    private void saveJtlFile(Metric metric) {
        String filename = metric.getReportId() + ".jtl";
        try {
            File file = new File(DataConsumer.TEMP_DIRECTORY_PATH + File.separator + filename);
            FileMetadata fileMetadata = fileService.saveFile(file);
            LoadTestReportWithBLOBs loadTestReportWithBLOBs = new LoadTestReportWithBLOBs();
            loadTestReportWithBLOBs.setFileId(fileMetadata.getId());
            loadTestReportWithBLOBs.setId(metric.getReportId());
            loadTestReportMapper.updateByPrimaryKeySelective(loadTestReportWithBLOBs);
            FileUtils.forceDelete(file);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void generateReport(String reportId) {
        // 检查 report_status
        boolean reporting = testResultSaveService.isReporting(reportId);

        generateReport(reportId, reporting);
    }

    private void generateReportComplete(String reportId) {
        LoadTestReportWithBLOBs report = new LoadTestReportWithBLOBs();
        report.setId(reportId);
        report.setUpdateTime(System.currentTimeMillis());
        // 测试结束后执行计算报告
        report.setStatus(TestStatus.Reporting.name());
        loadTestReportMapper.updateByPrimaryKeySelective(report);
        // 强制执行一次生成报告
        generateReport(reportId, true);
        // 标记结束
        testResultSaveService.saveReportCompletedStatus(reportId);

        // 测试结束后保存状态
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(TestStatus.Completed.name());
        loadTestReportMapper.updateByPrimaryKeySelective(report);
    }

    public void generateReport(String reportId, boolean isForce) {
        if (!isForce) {
            LogUtil.info("report generator is running.");
            return;
        }
        List<AbstractReport> reportGenerators = ReportGeneratorFactory.getReportGenerators();
        LogUtil.info("report generators size: {}", reportGenerators.size());
        CountDownLatch countDownLatch = new CountDownLatch(reportGenerators.size());

        Map<String, SampleContext> sampleContextMap = ResultDataParse.initJMeterConsumer(reportId, ResultDataParse.initConsumerList());

        reportGenerators.forEach(r -> reportThreadPool.execute(() -> {
            LogUtil.info("Report Key: " + r.getReportKey());
            r.init(reportId, sampleContextMap);
            try {
                r.execute();
            } catch (Exception e) {
                LogUtil.error(e);
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


    public void saveErrorMessage(String reportId, String message) {
        LoadTestReportWithBLOBs loadTestReport = new LoadTestReportWithBLOBs();
        loadTestReport.setId(reportId);
        loadTestReport.setStatus(TestStatus.Error.name());
        loadTestReport.setUpdateTime(System.currentTimeMillis());
        loadTestReport.setDescription(message);
        loadTestReportMapper.updateByPrimaryKeySelective(loadTestReport);
        // 查询 test_id
        LoadTestReport testReportFromDB = loadTestReportMapper.selectByPrimaryKey(reportId);
        LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
        loadTest.setId(testReportFromDB.getTestId());
        loadTest.setStatus(TestStatus.Error.name());
        loadTest.setDescription(message);
        loadTest.setUpdateTime(System.currentTimeMillis());
        loadTestMapper.updateByPrimaryKeySelective(loadTest);
    }
}
