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
package org.commonjava.indy.service.repository.ftests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static java.lang.String.format;
import static org.commonjava.indy.service.repository.testutil.TestUtil.prepareCustomizedMapper;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractStoreManagementTest
{
    private static final int NAME_LEN = 8;

    private static final String NAME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";

    protected static final String ADMIN_REPO_TYPE_BASE = "/api/admin/stores/%s/%s";

    protected static final String ADMIN_REPO_NAME_BASE = "/api/admin/stores/%s/%s/%s";

    protected final ObjectMapper mapper = prepareCustomizedMapper();

    protected String newName()
    {
        final Random rand = new Random();
        final StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < NAME_LEN; i++ )
        {
            sb.append( NAME_CHARS.charAt( ( Math.abs( rand.nextInt() ) % ( NAME_CHARS.length() - 1 ) ) ) );
        }

        return sb.toString();
    }

    protected String newUrl()
    {
        return format( "http://%s.com/", newName() );
    }

    protected static String getRepoTypeUrl( final StoreKey key )
    {
        return format( ADMIN_REPO_TYPE_BASE, key.getPackageType(), key.getType() );
    }

    protected static String getRepoUrl( final StoreKey key )
    {
        return format( ADMIN_REPO_NAME_BASE, key.getPackageType(), key.getType(), key.getName() );
    }

    protected void waitForEventPropagationWithMultiplier( final int multiplier )
    {
        long ms = 100L * multiplier;

        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Waiting {}ms for Indy server events to clear.", ms );
        // give events time to propagate
        try
        {
            Thread.sleep( ms );
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
            fail( "Thread interrupted while waiting for server events to propagate." );
        }

        logger.info( "Resuming test" );
    }
}
