package io.metersphere.streaming.report;

import io.metersphere.streaming.report.impl.*;

import java.util.ArrayList;
import java.util.List;

public class ReportGeneratorFactory {

    public static List<AbstractReport> getReportGenerators() {
        List<AbstractReport> reportGenerators = new ArrayList<>();
        reportGenerators.add(new LoadChartReport());
        reportGenerators.add(new ResponseTimeChartReport());
        reportGenerators.add(new ErrorsReport());
        reportGenerators.add(new ErrorsTop5Report());
        reportGenerators.add(new OverviewReport());
        reportGenerators.add(new RequestStatisticsReport());
        reportGenerators.add(new TimeInfoReport());
        return reportGenerators;
    }
}
