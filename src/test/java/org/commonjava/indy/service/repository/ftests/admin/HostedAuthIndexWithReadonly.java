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
package org.commonjava.indy.service.repository.ftests.admin;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.AbstractStoreManagementTest;
import org.commonjava.indy.service.repository.ftests.matchers.RepoEqualMatcher;
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.is;

/**
 * <b>GIVEN:</b>
 * <ul>
 *     <li>A hosted repo</li>
 * </ul>
 *
 * <br/>
 * <b>WHEN:</b>
 * <ul>
 *     <li>Change the hosted repo to readonly</li>
 *     <li>Change the hosted repo back to non-readonly</li>
 * </ul>
 *
 * <br/>
 * <b>THEN:</b>
 * <ul>
 *     <li>The hosted repo will also set to authoritative index on when changed to readonly</li>
 *     <li>The hosted repo will set to authoritative index off when changed back to non-readonly</li>
 * </ul>
 */
@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class HostedAuthIndexWithReadonly
        extends AbstractStoreManagementTest
{
    @Test
    public void addAndModifyHostedReadonlyThenAuthIndex()
            throws Exception
    {
        final String nameHosted = newName();
        final HostedRepository repo = new HostedRepository( MAVEN_PKG_KEY, nameHosted );
        String json = mapper.writeValueAsString( repo );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( repo.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, repo, HostedRepository.class ) )
               .body( "readonly", is( false ) )
               .body( "authoritative_index", is( false ) );

        final String repoUrl = getRepoUrl( repo.getKey() );

        repo.setReadonly( true );
        json = mapper.writeValueAsString( repo );
        given().body( json ).contentType( APPLICATION_JSON ).put( repoUrl ).then().statusCode( OK.getStatusCode() );

        Thread.sleep( 500 );

        given().get( repoUrl )
               .then()
               .statusCode( OK.getStatusCode() )
               .body( new RepoEqualMatcher<>( mapper, repo, HostedRepository.class ) )
               .body( "readonly", is( true ) )
               .body( "authoritative_index", is( true ) );

        repo.setReadonly( false );
        json = mapper.writeValueAsString( repo );
        given().body( json ).contentType( APPLICATION_JSON ).put( repoUrl ).then().statusCode( OK.getStatusCode() );

        Thread.sleep( 500 );

        given().get( repoUrl )
               .then()
               .statusCode( OK.getStatusCode() )
               .body( new RepoEqualMatcher<>( mapper, repo, HostedRepository.class ) )
               .body( "readonly", is( false ) )
               .body( "authoritative_index", is( false ) );
    }

}
