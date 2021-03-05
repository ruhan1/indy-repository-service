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
package org.commonjava.indy.service.repository.ftests;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.config.SslValidationConfiguration;
import org.commonjava.indy.service.repository.ftests.matchers.RepoEqualMatcher;
import org.commonjava.indy.service.repository.ftests.matchers.RevalidateRepoMatcher;
import org.commonjava.indy.service.repository.ftests.matchers.RevalidateAllMatcher;
import org.commonjava.indy.service.repository.ftests.profile.ISPNSSLFunctionProfile;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.commonjava.indy.service.repository.model.StoreType.remote;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

@QuarkusTest
@Tag( "function" )
@TestProfile( ISPNSSLFunctionProfile.class )
public class IndySslValidationApiTest
        extends AbstractStoreManagementTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( IndySslValidationApiTest.class );

    @Inject
    SslValidationConfiguration configuration;

    @Test
    public void run()
            throws JsonProcessingException
    {
        final String revalidAllUrl =
                String.format( ADMIN_REPO_TYPE_BASE, MAVEN_PKG_KEY, remote.singularEndpointName() ) + "/revalidate/all";
        given().post( revalidAllUrl ).then().body( new RevalidateAllMatcher( mapper ) );

        // REPO TESTING URL - http://repo.maven.apache.org/maven2 - NOT VALID NOT ALLOWED SSL REPO
        // first there is need for config variables to be set to false (remote.ssl.required , _internal.store.validation.enabled )
        if ( configuration.isSSLRequired() )
        {
            configuration.setSslRequired( false );
        }
        if ( configuration.getStoreValidationEnabled() )
        {
            configuration.setStoreValidationEnabled( false );
        }

        RemoteRepository testRepo = new RemoteRepository( "maven", "test", "http://repo.maven.apache.org/maven2" );
        String json = mapper.writeValueAsString( testRepo );
        LOGGER.info( "=> Storing Remote RemoteRepository: " + testRepo.getUrl() );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( testRepo.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, testRepo, RemoteRepository.class ) );

        // now there is need for config varables to be set to true (remote.ssl.required , _internal.store.validation.enabled )
        if ( !configuration.isSSLRequired() )
        {
            configuration.setSslRequired( true );
        }
        if ( !configuration.getStoreValidationEnabled() )
        {
            configuration.setStoreValidationEnabled( true );
        }

        LOGGER.info( "=> Validating Remote RemoteRepository: " + testRepo.getUrl() );
        String revalidRepoUrl = getRepoUrl( testRepo.getKey() ) + "/revalidate";
        given().post( revalidRepoUrl ).then().body( new RevalidateRepoMatcher( mapper, r -> {
            LOGGER.info( "=> API Returned Result [Validate Remote Repo]: " + r );
            if ( r == null )
            {
                return false;
            }
            if ( r.isValid() )
            {
                return false;
            }
            return r.getErrors().get( "NOT_ALLOWED_SSL" ).equals( r.getRepositoryUrl() );
        } ) );

        // REPO TESTING URL - https://repo.maven.apache.org/maven2 - VALID SSL REPO
        RemoteRepository testRepoSsl =
                new RemoteRepository( "maven", "test-ssl", "https://repo.maven.apache" + ".org/maven2" );
        json = mapper.writeValueAsString( testRepoSsl );
        LOGGER.info( "=> Storing Remote RemoteRepository: " + testRepoSsl.getUrl() );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( testRepoSsl.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, testRepoSsl, RemoteRepository.class ) );

        LOGGER.info( "=> Validating Remote RemoteRepository: " + testRepoSsl.getUrl() );
        revalidRepoUrl = getRepoUrl( testRepoSsl.getKey() ) + "/revalidate";
        given().post( revalidRepoUrl ).then().body( new RevalidateRepoMatcher( mapper, r -> {
            LOGGER.info( "=> API Returned Result [Validate Remote Repo]: " + r );
            if ( r == null )
            {
                return false;
            }
            if ( !r.isValid() )
            {
                return false;
            }
            if ( !String.valueOf( 200 ).equals( r.getErrors().get( "HTTP_HEAD_STATUS" ) ) )
            {
                return false;
            }
            return String.valueOf( 200 ).equals( r.getErrors().get( "HTTP_GET_STATUS" ) );
        } ) );

        // first there is need for config variables to be set to false (remote.ssl.required , _internal.store.validation.enabled )
        if ( configuration.isSSLRequired() )
        {
            configuration.setSslRequired( false );
        }
        if ( configuration.getStoreValidationEnabled() )
        {
            configuration.setStoreValidationEnabled( false );
        }

        // REPO TESTING URL - https://repo.maven.apache.org/maven2 - NOT VALID , ALLOWED , NOT AVAILABLE NON-SSL REPO
        RemoteRepository testRepoAllowed = new RemoteRepository( "maven", "test-ssl", "http://127.0.0.1/maven2" );
        json = mapper.writeValueAsString( testRepoAllowed );
        LOGGER.info( "=> Storing Remote RemoteRepository: " + testRepoAllowed.getUrl() );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( testRepoAllowed.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, testRepoAllowed, RemoteRepository.class ) );

        // now there is need for config varables to be set to true (remote.ssl.required , _internal.store.validation.enabled )
        if ( !configuration.isSSLRequired() )
        {
            configuration.setSslRequired( true );
        }
        if ( !configuration.getStoreValidationEnabled() )
        {
            configuration.setStoreValidationEnabled( true );
        }

        LOGGER.info( "=> Validating Remote RemoteRepository: " + testRepoAllowed.getUrl() );
        revalidRepoUrl = getRepoUrl( testRepoAllowed.getKey() ) + "/revalidate";
        given().post( revalidRepoUrl ).then().body( new RevalidateRepoMatcher( mapper, r -> {
            LOGGER.info( "=> API Returned Result [Validate Remote Repo]: " + r );
            if ( r == null )
            {
                return false;
            }
            if ( r.isValid() )
            {
                return false;
            }
            return r.getErrors().containsKey( "Exception" );
        } ) );

        given().post( revalidAllUrl ).then().body( new RevalidateAllMatcher( mapper ) );
    }

}

