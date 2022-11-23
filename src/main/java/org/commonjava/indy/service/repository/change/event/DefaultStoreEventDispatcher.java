/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy-repository-service)
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
package org.commonjava.indy.service.repository.change.event;

import org.commonjava.event.common.EventMetadata;
import org.commonjava.event.store.EventStoreKey;
import org.commonjava.event.store.StoreEnablementEvent;
import org.commonjava.event.store.StorePostDeleteEvent;
import org.commonjava.event.store.StorePostUpdateEvent;
import org.commonjava.event.store.StorePreDeleteEvent;
import org.commonjava.event.store.StorePreUpdateEvent;
import org.commonjava.event.store.StoreUpdateType;
import org.commonjava.indy.service.repository.change.event.kafka.KafkaEventUtils;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreDiffer;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

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
    KafkaEventUtils kafkaEvent;

    //    @Inject
    //    @WeftManaged
    //    @ExecutorConfig( named = CoreEventManagerConstants.DISPATCH_EXECUTOR_NAME,
    //                     threads = CoreEventManagerConstants.DISPATCH_EXECUTOR_THREADS,
    //                     priority = CoreEventManagerConstants.DISPATCH_EXECUTOR_PRIORITY )
    private final ExecutorService executor = Executors.newFixedThreadPool( 8 );

    @Override
    public void deleting( final EventMetadata eventMetadata, final StoreKey... storeKeys )
    {
        if ( kafkaEvent != null )
        {
            logger.trace( "Dispatch pre-delete event for: {}", asList( storeKeys ) );

            final StorePreDeleteEvent event = new StorePreDeleteEvent( eventMetadata, stream( storeKeys ).map(
                    StoreKey::toEventStoreKey ).collect( Collectors.toSet() ) );

            kafkaEvent.fireEvent( event );
        }
    }

    @Override
    public void deleted( final EventMetadata eventMetadata, final StoreKey... storeKeys )
    {
        if ( kafkaEvent != null )
        {
            final List<StoreKey> storeList = asList( storeKeys );

            logger.trace( "Dispatch post-delete event for: {}", storeList );

            final StorePostDeleteEvent event = new StorePostDeleteEvent( eventMetadata, stream( storeKeys ).map(
                    StoreKey::toEventStoreKey ).collect( Collectors.toSet() ) );

            kafkaEvent.fireEvent( event );

        }
    }

    @Override
    public void updating( final StoreUpdateType type, final EventMetadata eventMetadata,
                          final Map<ArtifactStore, ArtifactStore> changeMap )
    {
        final Map<EventStoreKey, Map<String, List<Object>>> newChangeMap = new HashMap<>( changeMap.size() );
        for ( Map.Entry<ArtifactStore, ArtifactStore> e : changeMap.entrySet() )
        {
            newChangeMap.put( e.getKey().getKey().toEventStoreKey(),
                              StoreDiffer.instance().diffArtifactStores( e.getKey(), e.getValue() ) );
        }
        final StorePreUpdateEvent event = new StorePreUpdateEvent( type, eventMetadata, newChangeMap );
        kafkaEvent.fireEvent( event );
    }

    @Override
    public void updated( final StoreUpdateType type, final EventMetadata eventMetadata,
                         final Map<ArtifactStore, ArtifactStore> changeMap )
    {
        final Map<EventStoreKey, Map<String, List<Object>>> newChangeMap = new HashMap<>( changeMap.size() );
        for ( Map.Entry<ArtifactStore, ArtifactStore> e : changeMap.entrySet() )
        {
            newChangeMap.put( e.getKey().getKey().toEventStoreKey(),
                              StoreDiffer.instance().diffArtifactStores( e.getKey(), e.getValue() ) );
        }
        executor.execute( () -> {
            final StorePostUpdateEvent event = new StorePostUpdateEvent( type, eventMetadata, newChangeMap );
            kafkaEvent.fireEvent( event );
        } );
    }

    private Map<EventStoreKey, EventStoreKey> toEventChangeMap( final Map<StoreKey, StoreKey> changeMap )
    {
        final Map<EventStoreKey, EventStoreKey> eventChangeMap = new HashMap<>( changeMap.size() );
        changeMap.forEach( ( key, value ) -> {
            if ( value != null )
            {
                eventChangeMap.put( key.toEventStoreKey(), value.toEventStoreKey() );
            }
            else
            {
                eventChangeMap.put( key.toEventStoreKey(), null );
            }
        } );
        return eventChangeMap;
    }

    @Override
    public void enabling( EventMetadata eventMetadata, StoreKey... storeKeys )
    {
        logger.trace( "Dispatch pre-enable event for: {}", asList( storeKeys ) );

        fireEnablement( true, eventMetadata, false, storeKeys );
    }

    @Override
    public void enabled( EventMetadata eventMetadata, StoreKey... storeKeys )
    {
        logger.trace( "Dispatch post-enable event for: {}", asList( storeKeys ) );

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
        if ( kafkaEvent != null )
        {
            final List<EventStoreKey> eventStoreKeys =
                    stream( stores ).map( StoreKey::toEventStoreKey ).collect( Collectors.toList() );
            final EventStoreKey[] eventStoreKeyArrs = eventStoreKeys.toArray( new EventStoreKey[] {} );
            final StoreEnablementEvent event =
                    new StoreEnablementEvent( eventMetadata, preprocess, disabling, eventStoreKeyArrs );

            if ( preprocess )
            {
                kafkaEvent.fireEvent( event );
            }
            else
            {
                executor.execute( () -> {
                    kafkaEvent.fireEvent( event );
                } );
            }
        }
    }

}
