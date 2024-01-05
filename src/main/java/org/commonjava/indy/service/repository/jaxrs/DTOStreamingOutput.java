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
package org.commonjava.indy.service.repository.jaxrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.output.CountingOutputStream;
import org.commonjava.indy.service.repository.data.metrics.TraceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import static org.commonjava.indy.service.repository.data.metrics.NameUtils.getDefaultName;
import static org.commonjava.indy.service.repository.data.metrics.NameUtils.getName;

public class DTOStreamingOutput
        implements StreamingOutput
{
    private static final String TRANSFER_METRIC_NAME = "indy.transferred.dto";

    private static final double NANOS_PER_SEC = 1000000000.0;

    private final ObjectMapper mapper;

    private final Object dto;

    private final TraceManager traceManager;

    public DTOStreamingOutput( final ObjectMapper mapper, final Object dto, final TraceManager traceManager )
    {
        this.mapper = mapper;
        this.dto = dto;
        this.traceManager = traceManager;
    }

    @Override
    public String toString()
    {
        try
        {
            return mapper.writeValueAsString( dto );
        }
        catch ( JsonProcessingException e )
        {
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.error( "Could not render toString() for DTO: " + dto, e );
            return String.valueOf( dto );
        }
    }

    @Override
    public void write( final OutputStream outputStream )
            throws IOException, WebApplicationException
    {
        AtomicReference<IOException> ioe = new AtomicReference<>();
        traceManager.wrapWithStandardMetrics( ( span ) -> {
            CountingOutputStream cout = new CountingOutputStream( outputStream );
            if ( span.isPresent() )
            {
                long start = System.nanoTime();
                try
                {
                    mapper.writeValue( cout, dto );
                }
                catch ( IOException e )
                {
                    ioe.set( e );
                }
                finally
                {
                    Logger logger = LoggerFactory.getLogger( getClass() );
                    logger.trace( "Wrote: {} bytes", cout.getByteCount() );

                    String name = getName( TRANSFER_METRIC_NAME, getDefaultName( dto.getClass(), "write" ), "size" );

                    long end = System.nanoTime();
                    double elapsed = ( end - start ) / NANOS_PER_SEC;

                    span.get().setAttribute( name, Math.round( cout.getByteCount() / elapsed ) );
                }
            }
            else
            {
                try
                {
                    mapper.writeValue( cout, dto );
                }
                catch ( IOException e )
                {
                    ioe.set( e );
                }
            }
            return null;

        }, () -> TRANSFER_METRIC_NAME );

        if ( ioe.get() != null )
        {
            throw ioe.get();
        }
    }
}
