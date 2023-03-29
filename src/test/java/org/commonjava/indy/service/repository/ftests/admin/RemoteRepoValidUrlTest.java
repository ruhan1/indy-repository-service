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
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class RemoteRepoValidUrlTest
        extends AbstractStoreManagementTest
{
    @Test
    public void run()
            throws Exception
    {
        final String VALID_REPO = "valid";
        final String VALID_URL = "http://www.foo.com";

        final RemoteRepository repo = new RemoteRepository( MAVEN_PKG_KEY, VALID_REPO, VALID_URL );
        final String json = mapper.writeValueAsString( repo );

        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( repo.getKey() ) )
               .then()
               .statusCode( CREATED.getStatusCode() )
               .body( new RepoEqualMatcher<>( mapper, repo, RemoteRepository.class ) );

        given().head( getRepoUrl( repo.getKey() ) ).then().statusCode( OK.getStatusCode() );

    }

}
