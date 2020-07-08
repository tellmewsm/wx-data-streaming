package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.ChartsData;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.graph.impl.HitsPerSecondGraphConsumer;

import java.util.List;
import java.util.Map;

public class LoadChartReport extends AbstractReport {


    @Override
    public void execute() {
        List<ChartsData> resultList = getUsersGraph();

        Map<String, Object> hitsMap = ResultDataParse.getGraphDataMap(content, new HitsPerSecondGraphConsumer());
        List<ChartsData> hitsList = ResultDataParse.graphMapParsing(hitsMap, "hits", "yAxis2");
        resultList.addAll(hitsList);

        saveResult(reportId, resultList);
    }

    @Override
    public String getReportKey() {
        return ReportKeys.LoadChart.name();
    }

}
