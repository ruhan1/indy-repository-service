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
package org.commonjava.indy.service.repository.data.cassandra;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.data.infinispan.InfinispanStoreDataManager;
import org.commonjava.indy.service.repository.data.tck.TCKFixtureProvider;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.commonjava.indy.service.repository.testutil.TestUtil;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

import java.util.Map;
import java.util.Set;

import static org.commonjava.indy.service.repository.data.infinispan.StoreDataCacheProducer.AFFECTED_BY_STORE_CACHE;
import static org.commonjava.indy.service.repository.data.infinispan.StoreDataCacheProducer.STORE_BY_PKG_CACHE;
import static org.commonjava.indy.service.repository.data.infinispan.StoreDataCacheProducer.STORE_DATA_CACHE;

public class CassandraTCKFixtureProvider
        implements TCKFixtureProvider
{
    private static CassandraStoreDataManager dataManager;

    private static CassandraClient client;

    protected void init()
            throws Exception
    {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();

        CassandraConfiguration config = new CassandraConfiguration();
        config.setEnabled( true );
        config.setCassandraHost( "localhost" );
        config.setCassandraPort( 9142 );
        config.setKeyspaceReplicas( 1 );
        config.setKeyspace( "noncontent" );
        config.setReplicationFactor( 1 );

        client = new CassandraClient( config );

        CassandraStoreQuery storeQuery = new CassandraStoreQuery( client, config );

        dataManager = new CassandraStoreDataManager( storeQuery, TestUtil.prepareCustomizedMapper() );
    }

    @Override
    public StoreDataManager getDataManager()
    {
        return dataManager;
    }

    protected void clean(){
        EmbeddedCassandraServerHelper.getSession();
        EmbeddedCassandraServerHelper.cleanDataEmbeddedCassandra("noncontent");
    }

    protected void destroy()
    {
        client.close();
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }
}
