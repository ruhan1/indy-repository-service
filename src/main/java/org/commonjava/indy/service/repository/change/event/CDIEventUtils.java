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
package org.commonjava.indy.service.repository.change.event;

import org.commonjava.event.store.IndyStoreEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Common way to insulate the system from event processing failures. Event handling is derivative or peripheral
 * to the main user workflows, even though it can harm long-term operation of the system. However, we can still
 * serve content if even handling is malfunctioning...and errors in event handling cannot be fixed by the user
 * anyway.
 *
 * Created by jdcasey on 11/1/17.
 */
@ApplicationScoped
@Deprecated
public class CDIEventUtils
{
    @Inject
    Event<IndyStoreEvent> eventEmitter;

    public void fireEvent( IndyStoreEvent event )
    {
        Logger logger = LoggerFactory.getLogger( CDIEventUtils.class );
        try
        {
            logger.trace( "Firing event: {}", event );
            eventEmitter.fire( event );
        }
        catch ( RuntimeException e )

        {
            logger.error( String.format( "Error processing event: %s. Reason: %s", event, e.getMessage() ), e );
        }
    }
}
