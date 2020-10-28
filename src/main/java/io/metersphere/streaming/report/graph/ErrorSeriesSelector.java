package io.metersphere.streaming.report.graph;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;

import java.util.Collections;

public class ErrorSeriesSelector extends AbstractSeriesSelector {


    @Override
    public Iterable<String> select(Sample sample) {
        if (StringUtils.isBlank(sample.getFailureMessage())) {
            return Collections.emptyList();
        }
        return Collections.singletonList(sample.getFailureMessage());
    }

}
