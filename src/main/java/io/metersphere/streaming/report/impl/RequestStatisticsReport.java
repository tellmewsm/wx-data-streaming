package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.Statistics;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.StatisticsSummaryConsumer;

import java.util.List;
import java.util.Map;

public class RequestStatisticsReport extends AbstractReport {

    @Override
    public String getReportKey() {
        return ReportKeys.RequestStatistics.name();
    }

    @Override
    public void execute() {
        Map<String, Object> statisticsDataMap = ResultDataParse.getSummaryDataMap(content, new StatisticsSummaryConsumer());
        List<Statistics> statistics = ResultDataParse.summaryMapParsing(statisticsDataMap, Statistics.class);

        saveResult(reportId, statistics);
    }
}
