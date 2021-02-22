/**
 * Copyright (C) 2020 Red Hat, Inc.
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

import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResponseHelperTest
{
    private final ResponseHelper helper = new ResponseHelper();

    @Test
    public void testFormatResponse()
    {
        URI uri = URI.create( "https://www.google.com" );
        Response response = helper.formatRedirect( uri );
        assertThat( response.getLocation(), equalTo( uri ) );
        assertThat( response.getStatus(), equalTo( MOVED_PERMANENTLY.getStatusCode() ) );

        Map<String, String> testObj = Collections.emptyMap();
        response = helper.formatCreatedResponseWithJsonEntity( uri, testObj );
        assertThat( response.getLocation(), equalTo( uri ) );
        assertThat( response.getStatus(), equalTo( CREATED.getStatusCode() ) );
        assertThat( response.getHeaderString( HttpHeaders.CONTENT_TYPE ), equalTo( APPLICATION_JSON ) );

        response = helper.formatOkResponseWithJsonEntity( testObj );
        assertThat( response.getStatus(), equalTo( OK.getStatusCode() ) );
        assertThat( response.getHeaderString( HttpHeaders.CONTENT_TYPE ), equalTo( APPLICATION_JSON ) );

        response = helper.formatOkResponseWithEntity( testObj, APPLICATION_XML, null );
        assertThat( response.getStatus(), equalTo( OK.getStatusCode() ) );
        assertThat( response.getHeaderString( HttpHeaders.CONTENT_TYPE ), equalTo( APPLICATION_XML ) );

    }

    @Test
    public void testFormatBadResponse()
    {
        Response response = helper.formatBadRequestResponse( "error" );
        assertThat( response.getStatus(), equalTo( BAD_REQUEST.getStatusCode() ) );
        assertThat( response.getHeaderString( HttpHeaders.CONTENT_TYPE ), equalTo( APPLICATION_JSON ) );
        assertThat( response.getEntity(), equalTo( "{\"error\": \"error\"}\n" ) );

        response = helper.formatResponse( new Exception( "error" ) );
        assertThat( response.getStatus(), equalTo( 500 ) );
        assertThat( response.getHeaderString( HttpHeaders.CONTENT_TYPE ), equalTo( TEXT_PLAIN ) );
        assertThat( response.getEntity(), notNullValue() );

        response = helper.formatResponse( new IndyWorkflowException( 503, "workflow error" ) );
        assertThat( response.getStatus(), equalTo( 503 ) );
        assertThat( response.getHeaderString( HttpHeaders.CONTENT_TYPE ), equalTo( TEXT_PLAIN ) );
        assertThat( response.getEntity(), notNullValue() );
    }
}
