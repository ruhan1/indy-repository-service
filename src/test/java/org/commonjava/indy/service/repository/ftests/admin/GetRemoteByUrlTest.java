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
package org.commonjava.indy.service.repository.ftests.admin;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.AbstractStoreManagementTest;
import org.commonjava.indy.service.repository.ftests.matchers.RepoEqualMatcher;
import org.commonjava.indy.service.repository.ftests.matchers.StoreListingCheckMatcher;
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.test.http.expect.ExpectationServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

/**
 * <b>GIVEN:</b>
 * <ul>
 *     <li>Two remote repositories with same accessible remote url</li>
 *     <li>One remote repo with another remote url</li>
 * </ul>
 *
 * <br/>
 * <b>WHEN:</b>
 * <ul>
 *     <li>Client request query by url for the first two remote</li>
 * </ul>
 *
 * <br/>
 * <b>THEN:</b>
 * <ul>
 *     <li>These two remote repositories can be got correctly</li>
 *     <li>The third one will not be got</li>
 * </ul>
 */
@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class GetRemoteByUrlTest
        extends AbstractStoreManagementTest
{
    public final ExpectationServer server = new ExpectationServer();

    @BeforeEach
    public void before()
            throws Exception
    {
        server.start();
    }

    @AfterEach
    public void after()
    {
        server.stop();
    }

    @Test
    public void getRemoteByUrl()
            throws Exception
    {
        final String urlName = "urltest";
        final String url = server.formatUrl( urlName );
        final RemoteRepository remote1 = new RemoteRepository( MAVEN_PKG_KEY, newName(), url );
        String json = mapper.writeValueAsString( remote1 );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( remote1.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, remote1, RemoteRepository.class ) );

        final RemoteRepository remote2 = new RemoteRepository( MAVEN_PKG_KEY, newName(), url );
        json = mapper.writeValueAsString( remote2 );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( remote2.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, remote2, RemoteRepository.class ) );

        final RemoteRepository remote3 =
                new RemoteRepository( MAVEN_PKG_KEY, newName(), server.formatUrl( "another test" ) );
        json = mapper.writeValueAsString( remote3 );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( remote3.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, remote3, RemoteRepository.class ) );

        server.expect( url, 200, "" );

        given().when()
               .get( "/api/admin/stores/maven/remote/query/byUrl?url=" + url )
               .then()
               .body( new StoreListingCheckMatcher( mapper, s -> {
                   if ( s == null )
                   {
                       return false;
                   }
                   if ( !s.getItems().contains( remote1 ) )
                   {
                       return false;
                   }
                   if ( !s.getItems().contains( remote2 ) )
                   {
                       return false;
                   }
                   return !s.getItems().contains( remote3 );
               } ) );

    }

}
