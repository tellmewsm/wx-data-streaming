package io.metersphere.streaming.report.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.streaming.base.domain.LoadTestReportResult;
import io.metersphere.streaming.commons.utils.CommonBeanFactory;
import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.report.Report;
import io.metersphere.streaming.report.base.ChartsData;
import io.metersphere.streaming.report.parse.ResultDataParse;
import io.metersphere.streaming.service.TestResultSaveService;
import org.apache.jmeter.report.processor.graph.impl.ActiveThreadsGraphConsumer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractReport implements Report {

    protected String reportId;
    protected List<String> content;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestResultSaveService testResultSaveService;

    public void init(String reportId, List<String> content) {
        this.reportId = reportId;
        this.content = content;
    }

    public AbstractReport() {
        this.testResultSaveService = CommonBeanFactory.getBean(TestResultSaveService.class);
    }


    public List<ChartsData> getUsersGraph() {
        List<ChartsData> resultList = new ArrayList<>();
        Map<String, Object> activeThreadMap = ResultDataParse.getGraphDataMap(content, new ActiveThreadsGraphConsumer());

        List<ChartsData> usersList = ResultDataParse.graphMapParsing(activeThreadMap, "users", "yAxis");
        Map<String, List<ChartsData>> collect = usersList.stream().collect(Collectors.groupingBy(ChartsData::getxAxis));
        collect.forEach((k, cs) -> resultList.add(new ChartsData() {{
            setGroupName("users");
            setxAxis(k);
            long sum = cs.stream().mapToLong(c -> c.getyAxis().longValue()).sum();
            setyAxis(new BigDecimal(sum));
        }}));

        return resultList;
    }

    public void saveResult(String reportId, Object content) {
        LoadTestReportResult record = new LoadTestReportResult();
        record.setId(UUID.randomUUID().toString());
        record.setReportId(reportId);
        record.setReportKey(getReportKey());
        try {
            record.setReportValue(objectMapper.writeValueAsString(content));
        } catch (JsonProcessingException e) {
            LogUtil.error(e);
        }
        testResultSaveService.saveResult(record);
        LogUtil.info("Report generate success: {}, reportId: {}", getReportKey(), reportId);
    }
}
