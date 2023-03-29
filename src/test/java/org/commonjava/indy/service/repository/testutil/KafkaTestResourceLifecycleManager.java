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
package org.commonjava.indy.service.repository.testutil;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;

import java.util.Map;

import static org.commonjava.indy.service.repository.change.event.kafka.KafkaEventUtils.CHANNEL_STORE;

public class KafkaTestResourceLifecycleManager
        implements QuarkusTestResourceLifecycleManager
{

    @Override
    public Map<String, String> start()
    {
        return InMemoryConnector.switchOutgoingChannelsToInMemory( CHANNEL_STORE );
    }

    @Override
    public void stop()
    {
        InMemoryConnector.clear();
    }
}
