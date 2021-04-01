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
import org.commonjava.event.store.ArtifactStoreUpdateType;
import org.commonjava.event.store.EventStoreKey;
import org.commonjava.event.store.StoreEnablementEvent;
import org.commonjava.event.store.StorePostDeleteEvent;
import org.commonjava.event.store.StorePostUpdateEvent;
import org.commonjava.event.store.StorePreDeleteEvent;
import org.commonjava.event.store.StorePreUpdateEvent;
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
    Event<StorePreUpdateEvent> updatePreEvent;

    @Inject
    Event<StorePostUpdateEvent> updatePostEvent;

    @Inject
    Event<StorePreDeleteEvent> preDelEvent;

    @Inject
    Event<StorePostDeleteEvent> postDelEvent;

    @Inject
    Event<StoreEnablementEvent> enablementEvent;

    //    @Inject
    //    @WeftManaged
    //    @ExecutorConfig( named = CoreEventManagerConstants.DISPATCH_EXECUTOR_NAME,
    //                     threads = CoreEventManagerConstants.DISPATCH_EXECUTOR_THREADS,
    //                     priority = CoreEventManagerConstants.DISPATCH_EXECUTOR_PRIORITY )
    private ExecutorService executor = Executors.newFixedThreadPool( 8 );

    @Override
    public void deleting( final EventMetadata eventMetadata, final StoreKey... storeKeys )
    {
        if ( preDelEvent != null )
        {
            logger.trace( "Dispatch pre-delete event for: {}", Arrays.asList( storeKeys ) );

            final StorePreDeleteEvent event =
                    new StorePreDeleteEvent( stream( storeKeys ).collect( Collectors.toSet() ) );

            fireEvent( preDelEvent, event );
        }
    }

    @Override
    public void deleted( final EventMetadata eventMetadata, final StoreKey... storeKeys )
    {
        if ( postDelEvent != null )
        {
            final List<StoreKey> storeList = Arrays.asList( storeKeys );

            logger.trace( "Dispatch post-delete event for: {}", storeList );

            final StorePostDeleteEvent event =
                    new StorePostDeleteEvent( stream( storeKeys ).collect( Collectors.toSet() ) );

            fireEvent( postDelEvent, event );

        }
    }

    @Override
    public void updating( final ArtifactStoreUpdateType type, final EventMetadata eventMetadata,
                          final Map<StoreKey, StoreKey> changeMap )
    {
        final StorePreUpdateEvent event = new StorePreUpdateEvent( type, new HashMap<>( changeMap ) );
        fireEvent( updatePreEvent, event );
    }

    @Override
    public void updated( final ArtifactStoreUpdateType type, final EventMetadata eventMetadata,
                         final Map<StoreKey, StoreKey> changeMap )
    {
        final Map<EventStoreKey, EventStoreKey> changesForStores = new HashMap<>( changeMap );
        executor.execute( () -> {
            final StorePostUpdateEvent event = new StorePostUpdateEvent( type, changesForStores );
            fireEvent( updatePostEvent, event );
        } );
    }

    @Override
    public void enabling( EventMetadata eventMetadata, StoreKey... storeKeys )
    {
        logger.trace( "Dispatch pre-enable event for: {}", Arrays.asList( storeKeys ) );

        fireEnablement( true, eventMetadata, false, storeKeys );
    }

    @Override
    public void enabled( EventMetadata eventMetadata, StoreKey... storeKeys )
    {
        logger.trace( "Dispatch post-enable event for: {}", Arrays.asList( storeKeys ) );

        executor.execute( () -> {
            fireEnablement( false, eventMetadata, false, storeKeys );
        } );
    }

    @Override
    public void disabling( EventMetadata eventMetadata, StoreKey... storeKeys )
    {
        fireEnablement( true, eventMetadata, true, storeKeys );
    }

    @Override
    public void disabled( EventMetadata eventMetadata, StoreKey... storeKeys )
    {
        executor.execute( () -> {
            fireEnablement( false, eventMetadata, true, storeKeys );
        } );
    }

    private void fireEnablement( boolean preprocess, EventMetadata eventMetadata, boolean disabling,
                                 StoreKey... stores )
    {
        if ( enablementEvent != null )
        {
            final StoreEnablementEvent event = new StoreEnablementEvent( preprocess, disabling, stores );

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
