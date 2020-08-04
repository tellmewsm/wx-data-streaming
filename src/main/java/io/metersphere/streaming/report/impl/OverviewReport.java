package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.ChartsData;
import io.metersphere.streaming.report.base.Statistics;
import io.metersphere.streaming.report.base.TestOverview;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.*;
import org.apache.jmeter.report.processor.graph.impl.ActiveThreadsGraphConsumer;
import org.apache.jmeter.report.processor.graph.impl.HitsPerSecondGraphConsumer;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class OverviewReport extends AbstractReport {

    @Override
    public String getReportKey() {
        return ReportKeys.Overview.name();
    }

    private final DecimalFormat responseTimeFormat = new DecimalFormat("0.0000");

    @Override
    public void execute() {
        SampleContext activeDataMap = sampleContextMap.get(ActiveThreadsGraphConsumer.class.getSimpleName());
        List<ChartsData> usersList = ResultDataParse.graphMapParsing(activeDataMap.getData(), "users", "yAxis");
        Map<String, List<ChartsData>> collect = usersList.stream().collect(Collectors.groupingBy(ChartsData::getGroupName));
        AtomicInteger maxUser = new AtomicInteger();
        collect.forEach((k, cs) -> {
            Optional<ChartsData> max = cs.stream().max(Comparator.comparing(ChartsData::getyAxis));
            int i = max.get().getyAxis().setScale(0, BigDecimal.ROUND_UP).intValue();
            maxUser.addAndGet(i);
        });

        SampleContext hitsDataMap = sampleContextMap.get(HitsPerSecondGraphConsumer.class.getSimpleName());
        List<ChartsData> hitsList = ResultDataParse.graphMapParsing(hitsDataMap.getData(), "hits", "yAxis2");
        double hits = hitsList.stream().map(ChartsData::getyAxis2)
                .mapToDouble(BigDecimal::doubleValue)
                .average().orElse(0);

        SampleContext errorDataMap = sampleContextMap.get(StatisticsSummaryConsumer.class.getSimpleName());
        List<Statistics> statisticsList = ResultDataParse.summaryMapParsing(errorDataMap.getData(), Statistics.class);
        double allSamples = statisticsList.stream().map(item -> Double.parseDouble(item.getSamples())).mapToDouble(Double::doubleValue).sum();

        double avgTp90 = statisticsList.stream().map(item -> Double.parseDouble(item.getTp90()) * Double.parseDouble(item.getSamples()))
                .mapToDouble(Double::doubleValue).sum() / allSamples;
        double responseTime = statisticsList.stream().map(item -> Double.parseDouble(item.getAverage()) * Double.parseDouble(item.getSamples()))
                .mapToDouble(Double::doubleValue).sum() / allSamples;

        double avgBandwidth = statisticsList.stream().map(item -> Double.parseDouble(item.getReceived())).mapToDouble(Double::doubleValue).average().orElse(0);

        SampleContext sampleDataMap = sampleContextMap.get(RequestsSummaryConsumer.class.getSimpleName());
        Map<String, Object> data = sampleDataMap.getData();
        String error = "";
        for (String key : data.keySet()) {
            MapResultData mapResultData = (MapResultData) data.get(key);
            ResultData koPercent = mapResultData.getResult("KoPercent");
            error = ((ValueResultData) koPercent).getValue().toString();
        }

        TestOverview testOverview = new TestOverview();
        testOverview.setMaxUsers(String.valueOf(maxUser.get()));
        testOverview.setAvgThroughput(decimalFormat.format(hits));
        testOverview.setErrors(decimalFormat.format(Double.valueOf(error)));
        testOverview.setAvgResponseTime(responseTimeFormat.format(responseTime / 1000));
        testOverview.setResponseTime90(responseTimeFormat.format(avgTp90 / 1000));
        testOverview.setAvgBandwidth(decimalFormat.format(avgBandwidth));

        saveResult(reportId, testOverview);
    }
}
