package io.metersphere.streaming.report.graph;

import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.processor.graph.AbstractSeriesSelector;

import java.util.Arrays;

public class ErrorSeriesSelector extends AbstractSeriesSelector {


    @Override
    public Iterable<String> select(Sample sample) {
        return Arrays.asList(sample.getFailureMessage());
    }

}
