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
package org.commonjava.indy.service.repository.data.mem;

import org.commonjava.indy.service.repository.audit.ChangeSummary;
import org.commonjava.indy.service.repository.data.AbstractStoreDataManager;
import org.commonjava.indy.service.repository.data.annotations.MemStoreDataManager;
import org.commonjava.indy.service.repository.event.NoOpStoreEventDispatcher;
import org.commonjava.indy.service.repository.event.StoreEventDispatcher;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@MemStoreDataManager
public class MemoryStoreDataManager
        extends AbstractStoreDataManager
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final Map<StoreKey, ArtifactStore> stores = new ConcurrentHashMap<>();

    @Inject
    StoreEventDispatcher dispatcher;

    protected MemoryStoreDataManager()
    {
    }

    public MemoryStoreDataManager( final boolean unitTestUsage )
    {
        this.dispatcher = new NoOpStoreEventDispatcher();
    }

    public MemoryStoreDataManager( final StoreEventDispatcher dispatcher )
    {
        this.dispatcher = dispatcher;
    }

    @Override
    protected StoreEventDispatcher getStoreEventDispatcher()
    {
        return dispatcher;
    }

    @Override
    protected ArtifactStore getArtifactStoreInternal( StoreKey key )
    {
        return stores.get( key );
    }

    @Override
    protected void removeAffectedBy( StoreKey key, StoreKey affected )
    {

    }

    @Override
    protected void addAffectedBy( StoreKey key, StoreKey affected )
    {

    }

    @Override
    protected void removeAffectedStore( StoreKey key )
    {

    }

    @Override
    protected ArtifactStore removeArtifactStoreInternal( StoreKey key )
    {
        return stores.remove( key );
    }

    @Override
    public void install()
    {
    }

    @Override
    public void clear( final ChangeSummary summary )
    {
        stores.clear();
    }

    @Override
    public Set<ArtifactStore> getAllArtifactStores()
    {
        return new HashSet<>( stores.values() );
    }

    @Override
    public Map<StoreKey, ArtifactStore> getArtifactStoresByKey()
    {
        return new HashMap<>( stores );
    }

    @Override
    public boolean hasArtifactStore( final StoreKey key )
    {
        return stores.containsKey( key );
    }

    @Override
    public void reload()
    {
    }

    @Override
    public boolean isStarted()
    {
        return true;
    }

    @Override
    public boolean isEmpty()
    {
        return stores.isEmpty();
    }

    @Override
    public Stream<StoreKey> streamArtifactStoreKeys()
    {
        return stores.keySet().stream();
    }

    @Override
    public Set<ArtifactStore> getArtifactStoresByPkgAndType( String packageType, StoreType storeType )
    {
        return stores.values()
                     .stream()
                     .filter( item -> packageType.equals( item.getPackageType() ) && storeType.equals(
                             item.getType() ) )
                     .collect( Collectors.toSet() );
    }

    @Override
    protected ArtifactStore putArtifactStoreInternal( StoreKey storeKey, ArtifactStore store )
    {
        return stores.put( storeKey, store );
    }

}
