package org.commonjava.indy.service.repository.data.metrics;

public interface Metered
{
    long getCount();

    double getFifteenMinuteRate();

    double getFiveMinuteRate();

    double getMeanRate();

    double getOneMinuteRate();
}
