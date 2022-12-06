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
package org.commonjava.indy.service.repository.ftests.matchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.indy.service.repository.data.ArtifactStoreValidateData;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.function.Function;

public class RevalidateRepoMatcher
        extends BaseMatcher<ArtifactStore>
{
    private final ObjectMapper mapper;

    private final Function<ArtifactStoreValidateData, Boolean> validateFunc;

    public RevalidateRepoMatcher( final ObjectMapper mapper,
                                  final Function<ArtifactStoreValidateData, Boolean> validateFunc )
    {
        this.mapper = mapper;
        this.validateFunc = validateFunc;
    }

    @Override
    public boolean matches( Object actual )
    {
        final ArtifactStoreValidateData repoValidateResult;
        try
        {
            repoValidateResult = mapper.readValue( (String) actual, ArtifactStoreValidateData.class );

            return validateFunc.apply( repoValidateResult );

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

}
