/**
 * Copyright (C) 2022-2023 Red Hat, Inc. (https://github.com/Commonjava/indy-repository-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.repository.data.metrics;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.commonjava.indy.service.repository.data.metrics.TraceConstant.EXCEPTION;
import static org.commonjava.indy.service.repository.data.metrics.NameUtils.name;

@ApplicationScoped
public class TraceManager
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    public static final String DEFAULT = "default";

    public static final String INDY_METRIC_ISPN = "indy.ispn";

    private final Tracer tracer;

    @Inject
    public TraceManager( Tracer tracer )
    {
        this.tracer = tracer;
    }

    public <T> T wrapWithStandardMetrics( final Supplier<T> method, final Supplier<String> classifier )
    {
        return wrapWithStandardMetrics( ( span ) -> method.get(), classifier );
    }

    public <T> T wrapWithStandardMetrics( final Function<Span, T> method, final Supplier<String> classifier )
    {
        String spanName = classifier.get();

        String errorName = name( spanName, EXCEPTION );

        logger.trace( "START: {} ({})", spanName, System.currentTimeMillis() );
        Span span = tracer.spanBuilder( spanName ).setSpanKind( SpanKind.SERVER ).startSpan();
        try (Scope ignored = span.makeCurrent())
        {
            T result = method.apply( span );
            span.setStatus( StatusCode.OK );
            return result;
        }
        catch ( Throwable e )
        {
            String eClassName = name( spanName, EXCEPTION, e.getClass().getSimpleName() );
            span.setStatus( StatusCode.ERROR );
            span.recordException( e );
            span.setAttribute( errorName, eClassName );

            throw e;
        }
        finally
        {
            span.end();
        }

    }

}
