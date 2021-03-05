package org.commonjava.indy.service.repository.data.metrics;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@InterceptorBinding
@Target( { PARAMETER } )
@Retention( RUNTIME )
public @interface MetricWrapperNamed
{
}
