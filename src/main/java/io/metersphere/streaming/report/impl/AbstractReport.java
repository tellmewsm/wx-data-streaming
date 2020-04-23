package io.metersphere.streaming.report.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.streaming.base.domain.LoadTestReportResult;
import io.metersphere.streaming.commons.utils.CommonBeanFactory;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.report.Report;
import io.metersphere.streaming.service.TestResultSaveService;

public abstract class AbstractReport implements Report {

    protected String reportId;
    protected String content;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestResultSaveService testResultSaveService;

    public void init(String reportId, String content) {
        this.reportId = reportId;
        this.content = content;
    }

    public AbstractReport() {
        this.testResultSaveService = CommonBeanFactory.getBean(TestResultSaveService.class);
    }

    public void saveResult(String reportId, Object content) {
        // todo 保存json
        LoadTestReportResult record = new LoadTestReportResult();
        record.setReportId(reportId);
        record.setReportKey(getReportKey());
        try {
            record.setReportValue(objectMapper.writeValueAsString(content));
        } catch (JsonProcessingException e) {
            LogUtil.error(e);
        }
        testResultSaveService.saveResult(record);
        LogUtil.info("生成报告: {}, reportId: {}", getReportKey(), reportId);
    }
}
