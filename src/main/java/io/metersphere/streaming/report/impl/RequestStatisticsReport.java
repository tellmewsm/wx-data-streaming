package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.Statistics;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.StatisticsSummaryConsumer;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class RequestStatisticsReport extends AbstractReport {

    @Override
    public String getReportKey() {
        return ReportKeys.RequestStatistics.name();
    }

    @Override
    public void execute() {

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        Map<String, Object> statisticsDataMap = ResultDataParse.getSummaryDataMap(reportId, new StatisticsSummaryConsumer());
        List<Statistics> statistics = ResultDataParse.summaryMapParsing(statisticsDataMap, Statistics.class);

        statistics.forEach(statistic -> {
            statistic.setAverage(decimalFormat.format(new BigDecimal(statistic.getAverage())));
            statistic.setTp90(decimalFormat.format(new BigDecimal(statistic.getTp90())));
            statistic.setTp95(decimalFormat.format(new BigDecimal(statistic.getTp95())));
            statistic.setTp99(decimalFormat.format(new BigDecimal(statistic.getTp99())));
            statistic.setTransactions(decimalFormat.format(new BigDecimal(statistic.getTransactions())));
            statistic.setReceived(decimalFormat.format(new BigDecimal(statistic.getReceived())));
            statistic.setSent(decimalFormat.format(new BigDecimal(statistic.getSent())));
            statistic.setError(decimalFormat.format(new BigDecimal(statistic.getError())));
        });

        saveResult(reportId, statistics);
    }
}
