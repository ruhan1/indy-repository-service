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
import org.commonjava.indy.service.repository.change.ArtifactStoreUpdateType;
import org.commonjava.indy.service.repository.model.ArtifactStore;

import javax.enterprise.event.Event;
import java.util.Map;

/**
 * Convenience component that standardizes the process of interacting with JEE {@link Event}s relating to changes in {@link ArtifactStore} definitions.
 */
public interface StoreEventDispatcher
{

    void deleting( final EventMetadata eventMetadata, final ArtifactStore... stores );

    void deleted( final EventMetadata eventMetadata, final ArtifactStore... stores );

    void updating(final ArtifactStoreUpdateType type, final EventMetadata eventMetadata, final Map<ArtifactStore, ArtifactStore> stores );

    void updated( final ArtifactStoreUpdateType type, final EventMetadata eventMetadata, final Map<ArtifactStore, ArtifactStore> stores );

    void enabling( final EventMetadata eventMetadata, final ArtifactStore...stores );

    void enabled( final EventMetadata eventMetadata, final ArtifactStore...stores );

    void disabling( final EventMetadata eventMetadata, final ArtifactStore... stores );

    void disabled( final EventMetadata eventMetadata, final ArtifactStore...stores );

}
