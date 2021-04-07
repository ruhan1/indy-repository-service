/**
 * Copyright (C) 2011-2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
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
package org.commonjava.indy.service.repository.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.stream.Stream;

import static org.commonjava.indy.service.repository.model.pkg.PackageTypeConstants.PKG_TYPE_MAVEN;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Created by jdcasey on 2/15/16.
 */
public class RemoteRepositoryTest
        extends AbstractSerializatinTest
{
    @Test
    public void serializeRemoteWithServerPem()
            throws JsonProcessingException
    {
        RemoteRepository remote = new RemoteRepository( PKG_TYPE_MAVEN, "test", "http://test.com/repo" );
        remote.setServerCertPem( "AAAAFFFASDFADSFASDFSADFa" );
        remote.setServerTrustPolicy( "self-signed" );

        String json = mapper.writeValueAsString( remote );

        assertThat( json, containsString( "server_certificate_pem" ) );
        assertThat( json, containsString( "AAAAFFFASDFADSFASDFSADFa" ) );
        assertThat( json, containsString( "server_trust_policy" ) );
        assertThat( json, containsString( "self-signed" ) );
    }

    @Test
    public void serializeRemoteWithKeyPemAndPassword()
            throws JsonProcessingException
    {
        RemoteRepository remote = new RemoteRepository( PKG_TYPE_MAVEN, "test", "http://test.com/repo" );
        remote.setKeyCertPem( "AAAAFFFASDFADSFASDFSADFa" );
        remote.setKeyPassword( "testme" );

        String json = mapper.writeValueAsString( remote );

        assertThat( json, containsString( "key_certificate_pem" ) );
        assertThat( json, containsString( "AAAAFFFASDFADSFASDFSADFa" ) );
        assertThat( json, containsString( "key_password" ) );
        assertThat( json, containsString( "testme" ) );
    }

    @Test
    public void copyFidelity()
    {
        RemoteRepository src =
                new RemoteRepository( GenericPackageTypeDescriptor.GENERIC_PKG_KEY, "test", "http://test.com/repo" );

        src.setTimeoutSeconds( 100 );
        src.setPassword( "foo" );
        src.setUser( "bar" );
        src.setMetadata( "key", "value" );
        src.setCacheTimeoutSeconds( 200 );
        src.setKeyCertPem( "THISISACERTIFICATEPEM" );
        src.setKeyPassword( "certpass" );
        src.setMetadataTimeoutSeconds( 300 );
        src.setNfcTimeoutSeconds( 400 );
        src.setPassthrough( false );
        src.setProxyHost( "127.0.0.1" );
        src.setProxyPort( 8888 );
        src.setProxyUser( "proxyuser" );
        src.setProxyPassword( "proxypass" );
        src.setServerCertPem( "ANOTHERCERTIFICATEPEM" );
        src.setServerTrustPolicy( "all" );
        src.setAllowReleases( false );
        src.setAllowSnapshots( false );
        src.setDescription( "some description" );
        src.setDisableTimeout( 500 );
        src.setDisabled( true );
        src.setPathMaskPatterns( Collections.singleton( "some/path" ) );
        src.setTransientMetadata( "transient", "someval" );
        src.setPathStyle( PathStyle.hashed );

        RemoteRepository target = src.copyOf();

        Stream.of( RemoteRepository.class.getMethods() )
              .filter( m -> m.getName().startsWith( "get" ) && m.isAccessible() && m.getParameterCount() == 0 )
              .forEach( m -> {
                  try
                  {
                      assertThat( m.getName() + " didn't get copied correctly!", m.invoke( target ),
                                  equalTo( m.invoke( src ) ) );
                  }
                  catch ( IllegalAccessException e )
                  {
                      e.printStackTrace();
                      fail( "Failed to invoke: " + m.getName() );
                  }
                  catch ( InvocationTargetException e )
                  {
                      e.printStackTrace();
                  }
              } );
    }
}
