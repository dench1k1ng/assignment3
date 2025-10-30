package com.smartcity.common;

public interface Metrics {

    void incrementCounter(String counterName);

    void incrementCounter(String counterName, int amount);

    long getCounter(String counterName);

    void startTiming(String operationName);

    void stopTiming(String operationName);

    long getTime(String operationName);

    void reset();

    String getSummary();
}
