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
package org.commonjava.indy.service.repository.testutil;

import org.commonjava.event.common.EventMetadata;
import org.commonjava.event.store.StoreUpdateType;
import org.commonjava.indy.service.repository.change.event.StoreEventDispatcher;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;

import java.util.Map;

public class StoreEventDispatcherStub
        implements StoreEventDispatcher
{

    @Override
    public void deleting( final EventMetadata metadata, final StoreKey... storeKeys )
    {
    }

    @Override
    public void deleted( final EventMetadata metadata, final StoreKey... storeKeys )
    {
    }

    @Override
    public void updating( final StoreUpdateType type, final EventMetadata metadata,
                          final Map<ArtifactStore, ArtifactStore> storeKeys )
    {
    }

    @Override
    public void updated( final StoreUpdateType type, final EventMetadata metadata,
                         final Map<ArtifactStore, ArtifactStore> storeKeys )
    {
    }

    @Override
    public void enabling( EventMetadata eventMetadata, StoreKey... storeKeys )
    {

    }

    @Override
    public void enabled( EventMetadata eventMetadata, StoreKey... storeKeys )
    {

    }

    @Override
    public void disabling( EventMetadata eventMetadata, StoreKey... storeKeys )
    {

    }

    @Override
    public void disabled( EventMetadata eventMetadata, StoreKey... storeKeys )
    {

    }

}
