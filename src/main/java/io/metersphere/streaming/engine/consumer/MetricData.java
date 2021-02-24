package io.metersphere.streaming.engine.consumer;

import io.metersphere.streaming.model.Metric;
import lombok.Getter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class MetricData {
    public static final Integer QUEUE_SIZE = 1000;

    private final CopyOnWriteArrayList<Metric> metrics = new CopyOnWriteArrayList<>();
    private final BlockingQueue<Metric> metricQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
}
