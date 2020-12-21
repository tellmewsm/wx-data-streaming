package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.ChartsData;
import io.metersphere.streaming.report.graph.consumer.DistributedActiveThreadsGraphConsumer;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.report.processor.graph.impl.TransactionsPerSecondGraphConsumer;

import java.util.List;

public class LoadChartReport extends AbstractReport {


    @Override
    public void execute() {
        SampleContext activeThreadMap = sampleContextMap.get(DistributedActiveThreadsGraphConsumer.class.getSimpleName());
        SampleContext hitsMap = sampleContextMap.get(TransactionsPerSecondGraphConsumer.class.getSimpleName());
        List<ChartsData> resultList = ResultDataParse.graphMapParsing(activeThreadMap.getData(), "users", "yAxis");
        List<ChartsData> hitsList = ResultDataParse.graphMapParsing(hitsMap.getData(), "transactions", "yAxis2");
        resultList.addAll(hitsList);

        saveResult(reportId, resultList);
    }

    @Override
    public String getReportKey() {
        return ReportKeys.LoadChart.name();
    }

}
