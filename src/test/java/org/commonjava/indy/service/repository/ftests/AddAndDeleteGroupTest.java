/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.matchers.RepoEqualMatcher;
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.commonjava.indy.service.repository.ftests.profile.MemoryFunctionProfile;
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class AddAndDeleteGroupTest
        extends AbstractStoreManagementTest
{

    @Test
    public void addMinimalGroupThenDeleteIt()
            throws Exception
    {
        final String name = newName();
        final Group repo = new Group( MavenPackageTypeDescriptor.MAVEN_PKG_KEY, name );
        final String json = mapper.writeValueAsString( repo );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( repo.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, repo, Group.class ) );

        final String repoUrl = getRepoUrl( repo.getKey() );
        given().head( repoUrl ).then().statusCode( OK.getStatusCode() );

        delete( repoUrl );

        given().head( repoUrl ).then().statusCode( NOT_FOUND.getStatusCode() );

    }

}
