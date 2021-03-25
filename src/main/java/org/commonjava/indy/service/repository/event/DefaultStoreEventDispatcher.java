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
package org.commonjava.indy.service.repository.event;

import org.commonjava.event.common.EventMetadata;
import org.commonjava.event.store.ArtifactStoreDeletePostEvent;
import org.commonjava.event.store.ArtifactStoreDeletePreEvent;
import org.commonjava.event.store.ArtifactStoreEnablementEvent;
import org.commonjava.event.store.ArtifactStorePostUpdateEvent;
import org.commonjava.event.store.ArtifactStorePreUpdateEvent;
import org.commonjava.event.store.ArtifactStoreUpdateType;
import org.commonjava.event.store.Store;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.commonjava.indy.service.repository.event.EventUtils.fireEvent;

/**
 * Pre-events (deleting, updating, enabling) are single-threaded (inline to user thread) so there isn't a race
 * condition between their execution and the target action.
 * For post-events (deleted, updated, enabled), it's intended that those are less critical and can happen async.
 *
 * This also makes them in right order - pre before action and post after action.
 */
@ApplicationScoped
@Default
public class DefaultStoreEventDispatcher
        implements StoreEventDispatcher
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private Event<ArtifactStorePreUpdateEvent> updatePreEvent;

    @Inject
    private Event<ArtifactStorePostUpdateEvent> updatePostEvent;

    @Inject
    private Event<ArtifactStoreDeletePreEvent> preDelEvent;

    @Inject
    private Event<ArtifactStoreDeletePostEvent> postDelEvent;

    @Inject
    private Event<ArtifactStoreEnablementEvent> enablementEvent;

    //    @Inject
    //    @WeftManaged
    //    @ExecutorConfig( named = CoreEventManagerConstants.DISPATCH_EXECUTOR_NAME,
    //                     threads = CoreEventManagerConstants.DISPATCH_EXECUTOR_THREADS,
    //                     priority = CoreEventManagerConstants.DISPATCH_EXECUTOR_PRIORITY )
    private ExecutorService executor = Executors.newFixedThreadPool( 8 );

    @Override
    public void deleting( final EventMetadata eventMetadata, final ArtifactStore... stores )
    {
        if ( preDelEvent != null )
        {
            logger.trace( "Dispatch pre-delete event for: {}", Arrays.asList( stores ) );

            final ArtifactStoreDeletePreEvent event =
                    new ArtifactStoreDeletePreEvent( eventMetadata, stream( stores ).collect( Collectors.toSet() ) );

            fireEvent( preDelEvent, event );
        }
    }

    @Override
    public void deleted( final EventMetadata eventMetadata, final ArtifactStore... stores )
    {
        if ( postDelEvent != null )
        {
            final List<StoreKey> storeList =
                    stream( stores ).map( ArtifactStore::getKey ).collect( Collectors.toList() );

            logger.trace( "Dispatch post-delete event for: {}", storeList );

            final ArtifactStoreDeletePostEvent event =
                    new ArtifactStoreDeletePostEvent( eventMetadata, stream( stores ).collect( Collectors.toSet() ) );

            fireEvent( postDelEvent, event );

        }
    }

    @Override
    public void updating( final ArtifactStoreUpdateType type, final EventMetadata eventMetadata,
                          final Map<ArtifactStore, ArtifactStore> changeMap )
    {
        final ArtifactStorePreUpdateEvent event =
                new ArtifactStorePreUpdateEvent( type, eventMetadata, new HashMap<>( changeMap ) );
        fireEvent( updatePreEvent, event );
    }

    @Override
    public void updated( final ArtifactStoreUpdateType type, final EventMetadata eventMetadata,
                         final Map<ArtifactStore, ArtifactStore> changeMap )
    {
        final Map<Store, Store> changesForStores = new HashMap<>( changeMap );
        executor.execute( () -> {
            final ArtifactStorePostUpdateEvent event =
                    new ArtifactStorePostUpdateEvent( type, eventMetadata, changesForStores );
            fireEvent( updatePostEvent, event );
        } );
    }

    @Override
    public void enabling( EventMetadata eventMetadata, ArtifactStore... stores )
    {
        logger.trace( "Dispatch pre-enable event for: {}", Arrays.asList( stores ) );

        fireEnablement( true, eventMetadata, false, stores );
    }

    @Override
    public void enabled( EventMetadata eventMetadata, ArtifactStore... stores )
    {
        logger.trace( "Dispatch post-enable event for: {}", Arrays.asList( stores ) );

        executor.execute( () -> {
            fireEnablement( false, eventMetadata, false, stores );
        } );
    }

    @Override
    public void disabling( EventMetadata eventMetadata, ArtifactStore... stores )
    {
        fireEnablement( true, eventMetadata, true, stores );
    }

    @Override
    public void disabled( EventMetadata eventMetadata, ArtifactStore... stores )
    {
        executor.execute( () -> {
            fireEnablement( false, eventMetadata, true, stores );
        } );
    }

    private void fireEnablement( boolean preprocess, EventMetadata eventMetadata, boolean disabling,
                                 ArtifactStore... stores )
    {
        if ( enablementEvent != null )
        {
            final ArtifactStoreEnablementEvent event =
                    new ArtifactStoreEnablementEvent( preprocess, eventMetadata, disabling, stores );

            if ( preprocess )
            {
                fireEvent( enablementEvent, event );
            }
            else
            {
                executor.execute( () -> {
                    fireEvent( enablementEvent, event );
                } );
            }
        }
    }

}
