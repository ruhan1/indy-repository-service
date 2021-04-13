/**
 * Copyright (C) 2020 Red Hat, Inc.
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
package org.commonjava.indy.service.repository.ftests.admin.event;

import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import org.commonjava.event.store.IndyStoreEvent;
import org.commonjava.indy.service.repository.change.event.kafka.KafkaEventUtils;
import org.commonjava.indy.service.repository.ftests.AbstractStoreManagementTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

public abstract class AbstractStoreEventTest
        extends AbstractStoreManagementTest
{
    @Inject
    @Any
    InMemoryConnector connector;

    protected void clearStoreChannel()
    {
        final InMemorySink<IndyStoreEvent> eventsChannel = connector.sink( KafkaEventUtils.CHANNEL_STORE );
        eventsChannel.clear();
    }

    protected void doStart()
    {
        // Leave for sub tests
    }

    protected void doStop()
    {
        // Leave for sub tests
    }

    @BeforeEach
    public void start()
    {
        clearStoreChannel();
        doStart();
    }

    @AfterEach
    public void stop()
    {
        clearStoreChannel();
        doStop();
    }
}
