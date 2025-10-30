package com.smartcity.common;

import java.util.HashMap;
import java.util.Map;


public class MetricsImpl implements Metrics {

    private final Map<String, Long> counters = new HashMap<>();
    private final Map<String, Long> timings = new HashMap<>();
    private final Map<String, Long> startTimes = new HashMap<>();

    @Override
    public void incrementCounter(String counterName) {
        incrementCounter(counterName, 1);
    }

    @Override
    public void incrementCounter(String counterName, int amount) {
        counters.put(counterName, counters.getOrDefault(counterName, 0L) + amount);
    }

    @Override
    public long getCounter(String counterName) {
        return counters.getOrDefault(counterName, 0L);
    }

    @Override
    public void startTiming(String operationName) {
        startTimes.put(operationName, System.nanoTime());
    }

    @Override
    public void stopTiming(String operationName) {
        Long startTime = startTimes.get(operationName);
        if (startTime != null) {
            long duration = System.nanoTime() - startTime;
            timings.put(operationName, duration);
            startTimes.remove(operationName);
        }
    }

    @Override
    public long getTime(String operationName) {
        return timings.getOrDefault(operationName, 0L);
    }

    @Override
    public void reset() {
        counters.clear();
        timings.clear();
        startTimes.clear();
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Metrics Summary ===\n");

        if (!counters.isEmpty()) {
            sb.append("Counters:\n");
            counters.forEach((name, value) -> sb.append(String.format("  %s: %d\n", name, value)));
        }

        if (!timings.isEmpty()) {
            sb.append("Timings:\n");
            timings.forEach((name, nanos) -> {
                double millis = nanos / 1_000_000.0;
                sb.append(String.format("  %s: %.3f ms (%d ns)\n", name, millis, nanos));
            });
        }

        return sb.toString();
    }
}
