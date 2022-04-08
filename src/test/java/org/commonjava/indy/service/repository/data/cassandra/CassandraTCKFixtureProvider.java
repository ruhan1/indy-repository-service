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
package org.commonjava.indy.service.repository.data.cassandra;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.data.infinispan.CacheProducer;
import org.commonjava.indy.service.repository.data.tck.TCKFixtureProvider;
import org.commonjava.indy.service.repository.testutil.TestUtil;
import org.infinispan.manager.DefaultCacheManager;

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
        DefaultCacheManager cacheManager = new DefaultCacheManager();
        dataManager = new CassandraStoreDataManager( storeQuery, TestUtil.prepareCustomizedMapper(), new CacheProducer( null, cacheManager ) );
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
