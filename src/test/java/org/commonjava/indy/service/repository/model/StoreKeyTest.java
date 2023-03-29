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
package org.commonjava.indy.service.repository.model;

import org.junit.jupiter.api.Test;

import static org.commonjava.indy.service.repository.model.GenericPackageTypeDescriptor.GENERIC_PKG_KEY;
import static org.commonjava.indy.service.repository.model.StoreType.remote;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by jdcasey on 5/12/17.
 */
public class StoreKeyTest
{

    @Test
    public void constructWithInvalidPackageType()
    {
        assertThrows( IllegalArgumentException.class, () -> new StoreKey( "invalid", remote, "stuff" ) );

    }

    @Test
    public void parseDeprecated()
    {
        StoreKey key = StoreKey.fromString( "remote:central" );

        assertThat( key.getPackageType(), equalTo( MAVEN_PKG_KEY ) );
        assertThat( key.getType(), equalTo( remote ) );
        assertThat( key.getName(), equalTo( "central" ) );
    }

    @Test
    public void parseWithValidPackageType()
    {
        StoreKey key = StoreKey.fromString( "maven:remote:central" );

        assertThat( key.getPackageType(), equalTo( MAVEN_PKG_KEY ) );
        assertThat( key.getType(), equalTo( remote ) );
        assertThat( key.getName(), equalTo( "central" ) );

        key = StoreKey.fromString( "generic-http:remote:httprox_stuff" );

        assertThat( key.getPackageType(), equalTo( GENERIC_PKG_KEY ) );
        assertThat( key.getType(), equalTo( remote ) );
        assertThat( key.getName(), equalTo( "httprox_stuff" ) );
    }

    @Test()
    public void parseWithInvalidPackageType()
    {
        assertThrows( IllegalArgumentException.class,
                      () -> System.out.println( StoreKey.fromString( "invalid:remote:stuff" ) ) );
    }

    @Test()
    public void parseWithInvalidStoreType()
    {
        assertThrows( IllegalArgumentException.class,
                      () -> System.out.println( StoreKey.fromString( "maven:invalid:stuff" ) ) );
    }
}
