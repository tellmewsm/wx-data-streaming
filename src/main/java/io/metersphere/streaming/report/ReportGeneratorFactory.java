package io.metersphere.streaming.report;

import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.report.impl.AbstractReport;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReportGeneratorFactory {

    public static List<AbstractReport> getReportGenerators() {
        String packageName = ReportGeneratorFactory.class.getPackage().getName();
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends AbstractReport>> subTypes = reflections.getSubTypesOf(AbstractReport.class);
        List<AbstractReport> reportGenerators = new ArrayList<>();
        subTypes.forEach(s -> {
            try {
                reportGenerators.add(s.newInstance());
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
        return reportGenerators;
    }
}
