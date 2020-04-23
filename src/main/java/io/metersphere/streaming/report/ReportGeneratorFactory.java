package io.metersphere.streaming.report;

import io.metersphere.streaming.commons.utils.LogUtil;
import io.metersphere.streaming.commons.utils.ReflectUtils;
import io.metersphere.streaming.report.impl.AbstractReport;

import java.util.ArrayList;
import java.util.List;

public class ReportGeneratorFactory {

    public static List<AbstractReport> getReportGenerators() {
        List<AbstractReport> reportGenerators = new ArrayList<>();
        try {
            List<Class<?>> classList = ReflectUtils.getAllAssignedClass(AbstractReport.class);
            classList.forEach(s -> {
                try {
                    reportGenerators.add((AbstractReport) s.newInstance());
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            });
        } catch (Exception e) {
            return reportGenerators;
        }

        return reportGenerators;
    }
}
