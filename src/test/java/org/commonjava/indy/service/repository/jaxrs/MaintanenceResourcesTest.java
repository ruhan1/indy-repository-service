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
package org.commonjava.indy.service.repository.jaxrs;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

@QuarkusTest
@TestProfile( MockTestProfile.class )
public class MaintanenceResourcesTest
{

    @Test
    public void test()
            throws Exception
    {
        try (InputStream in = Thread.currentThread()
                                    .getContextClassLoader()
                                    .getResourceAsStream( "indy-repo-bundle.zip" ))
        {
            RestAssured.given().when().body( in ).contentType( "application/zip" ).
                    post( "/api/admin/stores/maint/import" ).then().statusCode( 200 );
        }
    }

}
