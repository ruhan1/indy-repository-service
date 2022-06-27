/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
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
package org.commonjava.indy.service.repository.ftests.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.indy.service.repository.ftests.AbstractStoreManagementTest;
import org.commonjava.indy.service.repository.ftests.matchers.RepoEqualMatcher;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.indy.service.repository.model.StoreType;
import org.commonjava.indy.service.repository.model.dto.StoreListingDTO;
import org.commonjava.indy.service.repository.testutil.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

public abstract class AbstractQueryFuncTest
        extends AbstractStoreManagementTest
{
    protected static final String QUERY_BASE = "/api/admin/stores/query";

    @BeforeEach
    public void prepare()
            throws Exception
    {
        deleteAllRepos();
        setUpRepos( "remote" );
        setUpRepos( "hosted" );
        setUpRepos( "group" );
    }

    @AfterEach
    public void destroy()
            throws Exception
    {
        deleteAllRepos();
    }

    private static void setUpRepos( final String type )
            throws IOException
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource( "query-setup" );
        try (Stream<Path> pathStream = Files.list( Paths.get( Objects.requireNonNull( url ).getPath() ) )
                                            .filter( s -> s.getFileName().toString().startsWith( type ) ))
        {
            final Set<Path> paths = pathStream.collect( Collectors.toSet() );
            final ObjectMapper mapper = TestUtil.prepareCustomizedMapper();
            for ( Path path : paths )
            {
                StoreType s = StoreType.get( type );

                Class<? extends ArtifactStore> storeClass = null;
                switch ( s )
                {
                    case group:
                        storeClass = Group.class;
                        break;
                    case remote:
                        storeClass = RemoteRepository.class;
                        break;
                    case hosted:
                        storeClass = HostedRepository.class;
                        break;
                }

                if ( storeClass != null )
                {
                    ArtifactStore store = mapper.readValue( path.toFile(), storeClass );
                    final String json = mapper.writeValueAsString( store );
                    given().body( json )
                           .contentType( APPLICATION_JSON )
                           .post( getRepoTypeUrl( store.getKey() ) )
                           .then()
                           .body( new RepoEqualMatcher<>( mapper, store, storeClass ) );
                }

            }
        }

    }

    private void deleteAllRepos()
            throws IOException
    {
        String json = given().get( QUERY_BASE + "/all" ).body().prettyPrint();
        if ( json != null && !json.trim().equals( "" ) )
        {
            StoreListingDTO<ArtifactStore> stores = mapper.readValue( json, StoreListingDTO.class );
            for ( ArtifactStore store : stores )
            {
                given().delete( getRepoUrl( store.getKey() ) ).then().statusCode( NO_CONTENT.getStatusCode() );
            }
        }
    }

}
