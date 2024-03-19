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
package org.commonjava.indy.service.repository.util.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.commonjava.event.store.EventStoreKey;
import org.commonjava.event.store.jackson.EventStoreKeyDeserializer;
import org.commonjava.event.store.jackson.EventStoreKeySerializer;
import org.commonjava.indy.service.repository.model.StoreKey;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RepoApiSerializerModule
    extends SimpleModule
{

    private static final long serialVersionUID = 1L;

    public RepoApiSerializerModule()
    {
        super( "Indy Repository API" );
        addDeserializer( StoreKey.class, new StoreKeyDeserializer() );
        addSerializer( StoreKey.class, new StoreKeySerializer() );
        addDeserializer( EventStoreKey.class, new EventStoreKeyDeserializer() );
        addSerializer( EventStoreKey.class, new EventStoreKeySerializer() );
    }

    @Override
    public int hashCode()
    {
        return getClass().getSimpleName()
                         .hashCode() + 17;
    }

    @Override
    public boolean equals( final Object other )
    {
        if ( other == null )
        {
            return false;
        }
        return getClass().equals( other.getClass() );
    }
}
