package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.ChartsData;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.report.processor.graph.impl.ActiveThreadsGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.HitsPerSecondGraphConsumer;

import java.util.List;

public class LoadChartReport extends AbstractReport {


    @Override
    public void execute() {
        SampleContext activeThreadMap = sampleContextMap.get(ActiveThreadsGraphConsumer.class.getSimpleName());
        SampleContext hitsMap = sampleContextMap.get(HitsPerSecondGraphConsumer.class.getSimpleName());
        List<ChartsData> resultList = ResultDataParse.graphMapParsing(activeThreadMap.getData(), "users", "yAxis");
        List<ChartsData> hitsList = ResultDataParse.graphMapParsing(hitsMap.getData(), "hits", "yAxis2");
        resultList.addAll(hitsList);

        saveResult(reportId, resultList);
    }

    @Override
    public String getReportKey() {
        return ReportKeys.LoadChart.name();
    }

}
