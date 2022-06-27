/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
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
import org.commonjava.indy.service.repository.util.PathUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.commonjava.indy.service.repository.util.PathUtils.normalize;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
@TestProfile( MockTestProfile.class )
public class RepositoryQueryResourcesTest
{
    private final static String BASE_QUERY_PATH = "/api/admin/stores/query";

    @Test
    public void testGetAll()
    {
        given().when()
               .get( normalize( BASE_QUERY_PATH, "all" ) )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", greaterThan( 1 ) );
    }

    @Test
    public void testGetAllByDefaultPackageTypes()
    {
        given().when()
               .get( normalize( BASE_QUERY_PATH, "byDefaultPkgTypes" ) )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "items.size()", greaterThan( 1 ) );

    }

}
