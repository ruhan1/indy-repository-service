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
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

@TestProfile( ISPNFunctionProfile.class )
@QuarkusTest
@Tag( "function" )
//@Disabled("StoreManager.postStore not implemented yet, will cause this ftest fail.")
public class GroupAdjustmentToMemberDeletionTest
        extends AbstractStoreManagementTest
{

    @Test
    public void groupAdjustsToConstituentDeletion()
            throws Exception
    {
        final String nameHosted = newName();
        final HostedRepository hosted = new HostedRepository( MAVEN_PKG_KEY, nameHosted );
        String json = mapper.writeValueAsString( hosted );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( hosted.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, hosted, HostedRepository.class ) );

        final String nameGroup = newName();
        final Group group = new Group( MAVEN_PKG_KEY, nameGroup );
        group.addConstituent( hosted );
        json = mapper.writeValueAsString( group );
        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( group.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, group, Group.class ) )
               .body( "constituents.size()", CoreMatchers.is( 1 ) );

        delete( getRepoUrl( hosted.getKey() ) );

        waitForEventPropagationWithMultiplier( 5 ); // to make cascading deletion finish

        // TODO: need to finish the StoreDataManager.postStore function to make this pass.
        given().get( getRepoUrl( group.getKey() ) )
               .then()
               .statusCode( OK.getStatusCode() )
               .body( new RepoEqualMatcher<>( mapper, group, Group.class ) )
               .body( "constituents", CoreMatchers.nullValue() );

    }
}
