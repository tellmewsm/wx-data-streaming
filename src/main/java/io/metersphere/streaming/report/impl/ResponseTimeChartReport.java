package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.ChartsData;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.graph.impl.ResponseTimeOverTimeGraphConsumer;

import java.util.List;
import java.util.Map;

public class ResponseTimeChartReport extends AbstractReport {

    @Override
    public String getReportKey() {
        return ReportKeys.ResponseTimeChart.name();
    }

    @Override
    public void execute() {
        List<ChartsData> resultList = getUsersGraph();

        Map<String, Object> responseTimeMap = ResultDataParse.getGraphDataMap(content, new ResponseTimeOverTimeGraphConsumer());
        List<ChartsData> responseTimeList = ResultDataParse.graphMapParsing(responseTimeMap, "responseTime", "yAxis2");
        resultList.addAll(responseTimeList);

        saveResult(reportId, resultList);
    }

}
