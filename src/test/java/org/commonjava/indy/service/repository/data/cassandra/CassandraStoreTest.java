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
package org.commonjava.indy.service.repository.data.cassandra;

import org.apache.commons.lang3.RandomStringUtils;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.commonjava.indy.service.repository.model.StoreType;
import org.commonjava.indy.service.repository.model.pkg.PackageTypeConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled( "Cassandra dbunit always has problems to clean up resources when running test suite in maven")
public class CassandraStoreTest
{

    CassandraClient client;

    CassandraStoreQuery storeQuery;

    @BeforeEach
    public void start() throws Exception
    {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();

        CassandraConfiguration config = new CassandraConfiguration();
        config.setEnabled( true );
        config.setCassandraHost( "localhost" );
        config.setCassandraPort( 9142 );
        config.setKeyspaceReplicas( 1 );
        config.setKeyspace( "noncontent" );
        config.setCassandraUser( "user" );
        config.setCassandraPass( "pass" );

        client = new CassandraClient( config );

        storeQuery = new CassandraStoreQuery( client, config );

    }

    @AfterEach
    public void stop()
    {
        client.close();
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void testQuery()
    {
        DtxArtifactStore store = createTestStore( PackageTypeConstants.PKG_TYPE_MAVEN, StoreType.hosted.name() );

        Set<DtxArtifactStore> storeSet = storeQuery.getAllArtifactStores();

        assertThat(storeSet.size(), equalTo( 1 ));

        storeQuery.removeArtifactStore( store.getPackageType(), StoreType.hosted, store.getName() );

        Set<DtxArtifactStore> storeSet2 = storeQuery.getAllArtifactStores();

        assertThat(storeSet2.size(), equalTo( 0 ));

    }

    @Test
    public void testIsEmpty()
    {

        assertThat( storeQuery.isEmpty(), equalTo( Boolean.TRUE ));

        createTestStore( PackageTypeConstants.PKG_TYPE_MAVEN, StoreType.hosted.name() );

        assertThat( storeQuery.isEmpty(), equalTo( Boolean.FALSE ));
    }

    @Test
    public void testGetStoreByPkgAndType()
    {

        createTestStore( PackageTypeConstants.PKG_TYPE_MAVEN, StoreType.hosted.name() );
        Set<DtxArtifactStore> artifactStoreSet =
                        storeQuery.getArtifactStoresByPkgAndType( PackageTypeConstants.PKG_TYPE_MAVEN,
                                                                  StoreType.hosted );
        assertThat(artifactStoreSet.size(), equalTo( 1 ));
    }

    @Test
    public void testHashPrefix()
    {
        for ( int i = 0; i< 50; i++ )
        {
            String generatedName = RandomStringUtils.random( 10, true, false );
            int result = CassandraStoreUtil.getHashPrefix( generatedName );
            assertThat( (0 <= result && result < CassandraStoreUtil.MODULO_VALUE), equalTo( true ) );
        }

    }

    private DtxArtifactStore createTestStore( final String packageType, final String storeType )
    {
        String name = "build-001";
        DtxArtifactStore store = new DtxArtifactStore();
        store.setTypeKey( CassandraStoreUtil.getTypeKey( packageType, storeType ) );
        store.setPackageType( packageType );
        store.setStoreType( storeType );
        store.setNameHashPrefix( CassandraStoreUtil.getHashPrefix( name ) );
        store.setName( name );
        store.setDescription( "test cassandra store" );
        store.setDisabled( true );

        Set<String> maskPatterns = new HashSet<>(  );

        store.setPathMaskPatterns( maskPatterns );
        storeQuery.createDtxArtifactStore( store );

        return store;
    }

}
