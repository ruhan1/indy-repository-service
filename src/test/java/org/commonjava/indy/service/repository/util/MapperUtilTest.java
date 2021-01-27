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
package org.commonjava.indy.service.repository.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.indy.service.repository.util.jackson.MapperUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class MapperUtilTest
{
    @Test
    public void testPatchLegacyStoreJson()
            throws Exception
    {
        final var unpatched = "{\"key\":\"remote:test\",\"name\":\"test\"}";

        final var patched = MapperUtil.patchLegacyStoreJson( new ObjectMapper(), unpatched );

        assertThat( patched, containsString( "\"packageType\":\"maven\"" ) );
        assertThat( patched, containsString( "\"type\":\"remote\"" ) );
        assertThat( patched, containsString( "\"key\":\"maven:remote:test\"" ) );
    }
}
