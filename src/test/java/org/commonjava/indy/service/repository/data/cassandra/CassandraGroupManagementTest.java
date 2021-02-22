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
package org.commonjava.indy.service.repository.data.cassandra;

import org.commonjava.indy.service.repository.data.tck.GroupDataManagerTCK;
import org.commonjava.indy.service.repository.data.tck.TCKFixtureProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

@Disabled( "Cassandra dbunit always has problems to clean up resources when running test suite in maven")
public class CassandraGroupManagementTest
        extends GroupDataManagerTCK
{
    private static CassandraTCKFixtureProvider provider;

    @BeforeAll
    public static void initAll() throws Exception{
        provider = new CassandraTCKFixtureProvider();
        provider.init();
    }


    @Override
    protected TCKFixtureProvider getFixtureProvider()
    {
        return provider;
    }

    @AfterEach
    public void tearDown()
    {
        provider.clean();
    }

    @AfterAll
    public static void destroyAll(){
        provider.destroy();
    }
}
