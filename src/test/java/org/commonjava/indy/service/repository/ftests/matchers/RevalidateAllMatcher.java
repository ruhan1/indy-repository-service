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
package org.commonjava.indy.service.repository.ftests.matchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.indy.service.repository.data.ArtifactStoreValidateData;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class RevalidateAllMatcher
        extends BaseMatcher<ArtifactStore>
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    private final ObjectMapper mapper;

    public RevalidateAllMatcher( final ObjectMapper mapper )
    {
        this.mapper = mapper;
    }

    @Override
    public boolean matches( Object actual )
    {
        final HashMap<String, ArtifactStoreValidateData> remoteRepositoriesValidated;
        try
        {
            //noinspection unchecked
            remoteRepositoriesValidated = mapper.readValue( (String) actual, HashMap.class );
            String mismatchDescription;
            if ( remoteRepositoriesValidated == null )
            {
                mismatchDescription = "Should return validation results";
                return false;
            }
            logger.info( "=> All Validated Remote Repositories Response: " + remoteRepositoriesValidated );
            logger.info( "=> RESULT: " + remoteRepositoriesValidated.get( "maven:remote:central" ) );
            if ( remoteRepositoriesValidated.isEmpty() )
            {
                mismatchDescription = "Should return validation results";
                return false;
            }
            return true;
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
