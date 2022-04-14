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

import org.commonjava.indy.service.repository.data.cassandra.RemoteKojiStoreDataCache;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated infinispan data store is disabled in indy cluster mode. Will use {@link org.commonjava.indy.service.repository.data.cassandra.CassandraStoreDataManager} instead.
 */
@Deprecated
public class StoreDataCacheProducer
{
    public static final String STORE_DATA_CACHE = "store-data-v2";

    public static final String STORE_BY_PKG_CACHE = "store-by-package";

    public static final String AFFECTED_BY_STORE_CACHE = "affected-by-stores";

    public static final String REMOTE_KOJI_STORE = "remote-koji-stores";

    @Inject
    CacheProducer cacheProducer;

    @StoreDataCache
    @Produces
    @ApplicationScoped
    public CacheHandle<StoreKey, ArtifactStore> getStoreDataCache()
    {
        return cacheProducer.getCache( STORE_DATA_CACHE );
    }

    @StoreByPkgCache
    @Produces
    @ApplicationScoped
    public CacheHandle<String, Map<StoreType, Set<StoreKey>>> getStoreByPkgCache()
    {
        return cacheProducer.getCache( STORE_BY_PKG_CACHE );
    }

    @AffectedByStoreCache
    @Produces
    @ApplicationScoped
    public CacheHandle<StoreKey, Set<StoreKey>> getAffectedByStores()
    {
        return cacheProducer.getCache( AFFECTED_BY_STORE_CACHE );
    }

    @RemoteKojiStoreDataCache
    @Produces
    @ApplicationScoped
    public CacheHandle<StoreKey, ArtifactStore> getRemoteKojiStoreDataCache()
    {
        return cacheProducer.getCache( REMOTE_KOJI_STORE );
    }

}