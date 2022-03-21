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
package org.commonjava.indy.service.repository.data.infinispan;

import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.data.tck.TCKFixtureProvider;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

import java.util.Map;
import java.util.Set;

import static org.commonjava.indy.service.repository.data.infinispan.StoreDataCacheProducer.AFFECTED_BY_STORE_CACHE;
import static org.commonjava.indy.service.repository.data.infinispan.StoreDataCacheProducer.STORE_BY_PKG_CACHE;
import static org.commonjava.indy.service.repository.data.infinispan.StoreDataCacheProducer.STORE_DATA_CACHE;

public class InfinispanTCKFixtureProvider
        implements TCKFixtureProvider
{
    private InfinispanStoreDataManager dataManager;

    protected void init()
    {
        DefaultCacheManager cacheManager = new DefaultCacheManager( new GlobalConfigurationBuilder().build() );
        cacheManager.createCache( STORE_DATA_CACHE, new ConfigurationBuilder().build() );
        cacheManager.createCache( STORE_BY_PKG_CACHE, new ConfigurationBuilder().build() );
        cacheManager.createCache( AFFECTED_BY_STORE_CACHE, new ConfigurationBuilder().build() );
        Cache<StoreKey, ArtifactStore> storeCache = cacheManager.getCache( STORE_DATA_CACHE, true );
        Cache<String, Map<StoreType, Set<StoreKey>>> storesByPkgCache =
                cacheManager.getCache( STORE_BY_PKG_CACHE, true );
        Cache<StoreKey, Set<StoreKey>> affected = cacheManager.getCache( AFFECTED_BY_STORE_CACHE, true );
        dataManager = new InfinispanStoreDataManager( storeCache, storesByPkgCache, affected );
    }

    @Override
    public StoreDataManager getDataManager()
    {
        return dataManager;
    }
}
