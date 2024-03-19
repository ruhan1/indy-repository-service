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
package org.commonjava.indy.service.repository.change;

import io.smallrye.reactive.messaging.annotations.Blocking;
import org.commonjava.event.common.EventMetadata;
import org.commonjava.event.store.EventStoreKey;
import org.commonjava.event.store.IndyStoreEvent;
import org.commonjava.event.store.StoreEventType;
import org.commonjava.indy.service.repository.audit.ChangeSummary;
import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.exception.IndyDataException;
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Set;

import static org.commonjava.indy.service.repository.change.event.kafka.KafkaEventUtils.CHANNEL_INTERNAL;
import static org.commonjava.indy.service.repository.model.StoreKey.fromEventStoreKey;

@ApplicationScoped
public class GroupConsistencyListener
{

    private static final String GROUP_CONSISTENCY_ORIGIN = "group-consistency";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    StoreDataManager storeDataManager;

    private void processChanged( final EventStoreKey eventKey )
    {
        final StoreKey key = fromEventStoreKey( eventKey );
        try
        {
            final Set<Group> groups = storeDataManager.query().getGroupsContaining( key );
            logger.trace( "For repo: {}, containing groups are: {}", key, groups );
            for ( final Group group : groups )
            {
                logger.debug( "Removing {} from membership of group: {}", key, group.getKey() );

                Group g = group.copyOf();
                g.removeConstituent( key );
                storeDataManager.storeArtifactStore( g, new ChangeSummary( ChangeSummary.SYSTEM_USER,
                                                                           "Auto-update groups containing: " + key
                                                                                   + " (to maintain consistency)" ),
                                                     false, false,
                                                     new EventMetadata().set( StoreDataManager.EVENT_ORIGIN,
                                                                              GROUP_CONSISTENCY_ORIGIN ) );
            }
        }
        catch ( final IndyDataException e )
        {
            logger.error( String.format( "Failed to remove group constituent listings for: %s. Error: %s", key,
                                         e.getMessage() ), e );
        }
    }

    @Incoming( CHANNEL_INTERNAL )
    @Blocking
    public void storeDeleted( IndyStoreEvent postDelEvent )
    {
        if ( postDelEvent.getEventType() == StoreEventType.PostDelete )
        {
            logger.trace( "Processing proxy-manager store deletion: {}", postDelEvent.getKeys() );
            for ( final EventStoreKey key : postDelEvent.getKeys() )
            {
                logger.trace( "Processing deletion of: {}", key );
                processChanged( key );
            }
        }
    }

}
