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
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.indy.service.repository.model.StoreType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.commonjava.indy.service.repository.model.StoreType.group;
import static org.commonjava.indy.service.repository.model.StoreType.hosted;
import static org.commonjava.indy.service.repository.model.StoreType.remote;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class ListStoresByTypeTest
        extends AbstractStoreManagementTest
{

    @Test
    public void listByType()
            throws Exception
    {
        final Set<ArtifactStore> hosteds = new HashSet<>();
        for ( int i = 0; i < 3; i++ )
        {
            final HostedRepository repo = new HostedRepository( MAVEN_PKG_KEY, newName() );
            final String json = mapper.writeValueAsString( repo );
            given().body( json )
                   .contentType( APPLICATION_JSON )
                   .post( getRepoTypeUrl( repo.getKey() ) )
                   .then()
                   .body( new RepoEqualMatcher<>( mapper, repo, HostedRepository.class ) );
            hosteds.add( repo );
        }

        final Set<ArtifactStore> remotes = new HashSet<>();
        for ( int i = 0; i < 3; i++ )
        {
            final RemoteRepository repo = new RemoteRepository( MAVEN_PKG_KEY, newName(), newUrl() );
            final String json = mapper.writeValueAsString( repo );
            given().body( json )
                   .contentType( APPLICATION_JSON )
                   .post( getRepoTypeUrl( repo.getKey() ) )
                   .then()
                   .body( new RepoEqualMatcher<>( mapper, repo, RemoteRepository.class ) );
            remotes.add( repo );
        }

        final Set<ArtifactStore> groups = new HashSet<>();
        for ( int i = 0; i < 3; i++ )
        {
            final Group repo = new Group( MAVEN_PKG_KEY, newName() );
            final String json = mapper.writeValueAsString( repo );
            given().body( json )
                   .contentType( APPLICATION_JSON )
                   .post( getRepoTypeUrl( repo.getKey() ) )
                   .then()
                   .body( new RepoEqualMatcher<>( mapper, repo, Group.class ) );
            groups.add( repo );
        }

        // Now, start listing by type and verify that ONLY those of the given type are present
        checkStoreListing( hosted, hosteds, Arrays.asList( remotes, groups ) );

        checkStoreListing( remote, remotes, Arrays.asList( groups, hosteds ) );

        checkStoreListing( group, groups, Arrays.asList( hosteds, remotes ) );
    }

    private void checkStoreListing( final StoreType type, final Set<ArtifactStore> expected,
                                    final List<Set<ArtifactStore>> banned )
    {
        given().when()
               .get( "/api/admin/stores/_all/" + type.singularEndpointName() )
               .then()
               .body( new StoreListingCheckMatcher( mapper, s -> {
                   final List<? extends ArtifactStore> stores = s.getItems();

                   for ( final ArtifactStore store : expected )
                   {
                       if ( !stores.contains( store ) )
                       {
                           return false;
                       }
                   }

                   for ( final Set<ArtifactStore> bannedSet : banned )
                   {
                       for ( final ArtifactStore store : bannedSet )
                       {
                           if ( stores.contains( store ) )
                           {
                               return false;
                           }
                       }
                   }
                   return true;
               } ) );
    }

}
