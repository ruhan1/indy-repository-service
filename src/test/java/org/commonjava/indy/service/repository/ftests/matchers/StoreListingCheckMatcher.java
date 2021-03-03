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
package org.commonjava.indy.service.repository.ftests.matchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.dto.StoreListingDTO;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.join;

public class StoreListingCheckMatcher
        extends BaseMatcher<ArtifactStore>
{
    private String mismatchDescription;

    private final ObjectMapper mapper;

    private final Set<ArtifactStore> expected;

    private final List<Set<ArtifactStore>> banned;

    public StoreListingCheckMatcher( final ObjectMapper mapper, final Set<ArtifactStore> expected,
                                     final List<Set<ArtifactStore>> banned )
    {
        this.mapper = mapper;
        this.expected = expected;
        this.banned = banned;
    }

    @Override
    public boolean matches( Object actual )
    {
        final StoreListingDTO<ArtifactStore> dto;
        try
        {
            dto = mapper.readValue( (String) actual, StoreListingDTO.class );
        }
        catch ( JsonProcessingException e )
        {
            throw new RuntimeException( e );
        }
        final List<? extends ArtifactStore> stores = dto.getItems();

        for ( final ArtifactStore store : expected )
        {
            if ( !stores.contains( store ) )
            {
                mismatchDescription = store.getKey() + " should be present in:\n  " + join( keys( stores ), "\n  " );
                return false;
            }
        }

        for ( final Set<ArtifactStore> bannedSet : banned )
        {
            for ( final ArtifactStore store : bannedSet )
            {
                if ( stores.contains( store ) )
                {
                    mismatchDescription =
                            store.getKey() + " should NOT be present in:\n  " + join( keys( stores ), "\n  " );
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void describeTo( Description description )
    {

    }

    private List<StoreKey> keys( final List<? extends ArtifactStore> stores )
    {
        final List<StoreKey> keys = new ArrayList<>();
        for ( final ArtifactStore store : stores )
        {
            keys.add( store.getKey() );
        }

        return keys;
    }
}
