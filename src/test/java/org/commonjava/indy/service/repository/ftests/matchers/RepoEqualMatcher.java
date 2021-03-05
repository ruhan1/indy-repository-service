/**
 * Copyright (C) 2011-2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
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
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RepoEqualMatcher<R extends ArtifactStore>
        extends BaseMatcher<R>
{
    private final ObjectMapper objectMapper;

    private final ArtifactStore expect;

    private final Class<R> repoClass;

    public RepoEqualMatcher( final ObjectMapper objectMapper, final ArtifactStore expect, final Class<R> repoClass )
    {
        this.objectMapper = objectMapper;
        this.expect = expect;
        this.repoClass = repoClass;
    }

    @Override
    public boolean matches( Object actual )
    {
        try
        {
            final R actualRepo = objectMapper.readValue( actual.toString(), repoClass );
            return expect.equals( actualRepo );
        }
        catch ( JsonProcessingException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void describeTo( Description description )
    {
        description.appendText( "equals " );

    }
}
