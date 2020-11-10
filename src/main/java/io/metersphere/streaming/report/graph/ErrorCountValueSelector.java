package io.metersphere.streaming.report.graph;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.graph.CountValueSelector;
import org.apache.jmeter.report.utils.MetricUtils;

public class ErrorCountValueSelector extends CountValueSelector {
    private static final Double ONE = 1.0d;
    private static final Double ZERO = 0.0d;

    public ErrorCountValueSelector(boolean ignoreTransactionController) {
        super(ignoreTransactionController);
    }


    @Override
    public Double select(String series, Sample sample) {
        if (MetricUtils.isSuccessCode(sample.getResponseCode())) {
            return ZERO;
        }
        if (isIgnoreTransactionController()) {
            if (!sample.isController()) {
                return ONE;
            }
        } else {
            if (!sample.isEmptyController()) {
                return ONE;
            }
        }
        return ZERO;
    }

}
