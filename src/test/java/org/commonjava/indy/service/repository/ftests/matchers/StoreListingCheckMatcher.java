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
import java.util.function.Function;

public class StoreListingCheckMatcher
        extends BaseMatcher<ArtifactStore>
{
    private final ObjectMapper mapper;

    private final Function<StoreListingDTO<ArtifactStore>, Boolean> checkFunc;

    public StoreListingCheckMatcher( final ObjectMapper mapper,
                                     final Function<StoreListingDTO<ArtifactStore>, Boolean> validateFunc )
    {
        this.mapper = mapper;
        this.checkFunc = validateFunc;
    }

    @Override
    public boolean matches( Object actual )
    {
        final StoreListingDTO<ArtifactStore> dto;
        try
        {
            dto = mapper.readValue( (String) actual, StoreListingDTO.class );
            return checkFunc.apply( dto );
        }
        catch ( JsonProcessingException e )
        {
            throw new RuntimeException( e );
        }
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
