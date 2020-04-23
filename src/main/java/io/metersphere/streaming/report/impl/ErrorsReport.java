package io.metersphere.streaming.report.impl;

import io.metersphere.streaming.commons.constants.ReportKeys;
import io.metersphere.streaming.report.base.Errors;
import io.metersphere.streaming.report.parse.ResultDataParse;
import org.apache.jmeter.report.processor.ErrorsSummaryConsumer;

import java.util.List;
import java.util.Map;

public class ErrorsReport extends AbstractReport {

    @Override
    public String getReportKey() {
        return ReportKeys.Errors.name();
    }

    @Override
    public void execute() {
        Map<String, Object> statisticsDataMap = ResultDataParse.getSummaryDataMap(content, new ErrorsSummaryConsumer());
        List<Errors> errors = ResultDataParse.summaryMapParsing(statisticsDataMap, Errors.class);

        saveResult(reportId, errors);
    }
}
