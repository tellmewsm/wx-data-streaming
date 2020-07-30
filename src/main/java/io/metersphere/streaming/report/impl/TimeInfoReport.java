package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.ReportTimeInfo;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.apache.jmeter.report.dashboard.ReportGenerator.*;

public class TimeInfoReport extends AbstractReport {

    @Override
    public String getReportKey() {
        return ReportKeys.TimeInfo.name();
    }

    @Override
    public void execute() {
        ReportTimeInfo reportTimeInfo = getReportTimeInfo();

        saveResult(reportId, reportTimeInfo);
    }

    private ReportTimeInfo getReportTimeInfo() {
        FilterConsumer dateRangeConsumer = createFilterByDateRange();
        dateRangeConsumer.addSampleConsumer(createBeginDateConsumer());
        dateRangeConsumer.addSampleConsumer(createEndDateConsumer());

        Map<String, Object> sampleDataMap = ResultDataParse.getSampleDataMap(reportId, dateRangeConsumer);
        ValueResultData beginDateResult = (ValueResultData) sampleDataMap.get(BEGIN_DATE_CONSUMER_NAME);
        ValueResultData endDateResult = (ValueResultData) sampleDataMap.get(END_DATE_CONSUMER_NAME);
        long startTimeStamp = ((Double) beginDateResult.getValue()).longValue();
        long endTimeStamp = ((Double) endDateResult.getValue()).longValue();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String startTime = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeStamp), ZoneId.systemDefault()));
        String endTime = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeStamp), ZoneId.systemDefault()));

        // todo 时间问题
        long seconds = Duration.between(Instant.ofEpochMilli(startTimeStamp), Instant.ofEpochMilli(endTimeStamp)).getSeconds();
        ReportTimeInfo reportTimeInfo = new ReportTimeInfo();
        reportTimeInfo.setStartTime(startTime);
        reportTimeInfo.setEndTime(endTime);
        reportTimeInfo.setDuration(String.valueOf(seconds));
        return reportTimeInfo;
    }

    private FilterConsumer createFilterByDateRange() {
        FilterConsumer dateRangeFilter = new FilterConsumer();
        dateRangeFilter.setName(DATE_RANGE_FILTER_CONSUMER_NAME);
        dateRangeFilter.setSamplePredicate(sample -> true);
        return dateRangeFilter;
    }

    private AggregateConsumer createEndDateConsumer() {
        AggregateConsumer endDateConsumer = new AggregateConsumer(
                new MaxAggregator(), sample -> (double) sample.getEndTime());
        endDateConsumer.setName(END_DATE_CONSUMER_NAME);
        return endDateConsumer;
    }

    private AggregateConsumer createBeginDateConsumer() {
        AggregateConsumer beginDateConsumer = new AggregateConsumer(
                new MinAggregator(), sample -> (double) sample.getStartTime());
        beginDateConsumer.setName(BEGIN_DATE_CONSUMER_NAME);
        return beginDateConsumer;
    }
}
