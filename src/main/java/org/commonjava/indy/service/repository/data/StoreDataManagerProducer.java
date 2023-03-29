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
package org.commonjava.indy.service.repository.data;

import org.commonjava.indy.service.repository.config.IndyRepositoryConfiguration;
import org.commonjava.indy.service.repository.data.annotations.ClusterStoreDataManager;
import org.commonjava.indy.service.repository.data.annotations.MemStoreDataManager;
import org.commonjava.indy.service.repository.data.annotations.StandaloneStoreDataManager;
import org.commonjava.indy.service.repository.data.cassandra.CassandraStoreDataManager;
import org.commonjava.indy.service.repository.data.infinispan.InfinispanStoreDataManager;
import org.commonjava.indy.service.repository.data.mem.MemoryStoreDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class StoreDataManagerProducer
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    @Inject
    IndyRepositoryConfiguration repoConfig;

    @Produces
    @ApplicationScoped
    @Default
    public StoreDataManager produces( @StandaloneStoreDataManager InfinispanStoreDataManager ispnStoreDataManager,
                                      @ClusterStoreDataManager CassandraStoreDataManager clusterStoreDataManager,
                                      @MemStoreDataManager MemoryStoreDataManager memoryStoreDataManager )
    {
        String storageType = repoConfig.storageType().orElse( "" );
        if ( IndyRepositoryConfiguration.STORAGE_INFINISPAN.equals( storageType ) )
        {
            return ispnStoreDataManager;
        }
        else if ( IndyRepositoryConfiguration.STORAGE_CASSANDRA.equals( storageType ) )
        {
            return clusterStoreDataManager;
        }
        else
        {
            logger.warn( "No store manager for the configured property: {}, so use default memory one",
                         repoConfig.storageType().orElse( "" ) );
            return memoryStoreDataManager;
        }
    }
}
