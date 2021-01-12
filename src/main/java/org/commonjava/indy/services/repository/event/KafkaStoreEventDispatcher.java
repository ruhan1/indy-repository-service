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
package org.commonjava.indy.services.repository.event;

import org.commonjava.indy.services.repository.change.ArtifactStoreUpdateType;
import org.commonjava.indy.services.repository.model.ArtifactStore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.util.Map;

/**
 * This event dispatcher will dispatch Store Event through kafka
 */
@ApplicationScoped
@Default
public class KafkaStoreEventDispatcher implements StoreEventDispatcher
{
    @Override
    public void deleting( EventMetadata eventMetadata, ArtifactStore... stores )
    {

    }

    @Override
    public void deleted( EventMetadata eventMetadata, ArtifactStore... stores )
    {

    }

    @Override
    public void updating( ArtifactStoreUpdateType type, EventMetadata eventMetadata,
                          Map<ArtifactStore, ArtifactStore> stores )
    {

    }

    @Override
    public void updated( ArtifactStoreUpdateType type, EventMetadata eventMetadata,
                         Map<ArtifactStore, ArtifactStore> stores )
    {

    }

    @Override
    public void enabling( EventMetadata eventMetadata, ArtifactStore... stores )
    {

    }

    @Override
    public void enabled( EventMetadata eventMetadata, ArtifactStore... stores )
    {

    }

    @Override
    public void disabling( EventMetadata eventMetadata, ArtifactStore... stores )
    {

    }

    @Override
    public void disabled( EventMetadata eventMetadata, ArtifactStore... stores )
    {

    }
}
