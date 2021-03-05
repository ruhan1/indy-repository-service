package org.commonjava.indy.service.repository.data.metrics;

public interface Meter
                extends Metric, Metered
{
    void mark();

    void mark( long n );
}