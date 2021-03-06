/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.repository.model;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.fail;

public class ModelJSONTest
        extends AbstractSerializatinTest
{

    @Test
    public void deserializeHostedRepo()
            throws Exception
    {
        final String resource = "hosted-with-storage.json";
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream( resource );
        if ( is == null )
        {
            fail( "Cannot find classpath resource: " + resource );
        }

        final String json = IOUtils.toString( is, Charset.defaultCharset() );
        System.out.println( json );
        final HostedRepository repo = mapper.readValue( json, HostedRepository.class );
        System.out.println( repo );
    }

}
