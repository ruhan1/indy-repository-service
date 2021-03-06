package org.commonjava.indy.service.repository.data.cassandra;

import org.apache.commons.lang3.RandomStringUtils;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.commonjava.indy.service.repository.model.StoreType;
import org.commonjava.indy.service.repository.model.pkg.PackageTypeConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
        config.setReplicationFactor( 1 );
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
