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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ContainsOnlyOneMatcher
        extends BaseMatcher<String>
{
    private final String expectedContains;

    private String mismatchDesc;

    public ContainsOnlyOneMatcher( final String expectedContains )
    {
        this.expectedContains = expectedContains;
    }

    @Override
    public boolean matches( Object actual )
    {
        if ( actual == null )
        {
            mismatchDesc = "The actual content cannot be null";
            return false;
        }
        final String actualContent = actual.toString();
        int firstIndex = actualContent.indexOf( expectedContains );
        if ( firstIndex < 0 )
        {
            mismatchDesc = String.format( "{%s} does not exist!", expectedContains );
            return false;
        }
        if ( firstIndex != actualContent.lastIndexOf( expectedContains ) )
        {
            mismatchDesc = String.format( "{%s} does not only occurs for once!", expectedContains );
            return false;
        }
        return true;
    }

    @Override
    public void describeTo( Description description )
    {
        description.appendValue( String.format( "{%s} should exist for only once.", expectedContains ) );
    }

    @Override
    public void describeMismatch( Object item, Description description )
    {
        description.appendValue( mismatchDesc );
    }
}
