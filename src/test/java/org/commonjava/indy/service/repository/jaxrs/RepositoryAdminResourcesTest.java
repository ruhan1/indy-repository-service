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
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class RepositoryAdminResourcesTest
{
    @Test
    public void testExists()
    {
        given().when().head( "/api/admin/stores/maven/remote/exists" ).then().statusCode( OK.getStatusCode() );
        given().when()
               .head( "/api/admin/stores/maven/remote/nonexists" )
               .then()
               .statusCode( NOT_FOUND.getStatusCode() );
    }

    @Test
    public void testGetAllWithTwoValues()
    {
        given().when()
               .get( "/api/admin/stores/maven/remote" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( MediaType.APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 2 ) );
    }

    @Test
    public void testGetAllWithEmpty()
    {
        given().when()
               .get( "/api/admin/stores/maven/hosted" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( MediaType.APPLICATION_JSON )
               .contentType( MediaType.APPLICATION_JSON )
               .body( "size()", is( 0 ) );
    }

    @Test
    public void testGetAllNotFound()
    {
        given().when().get( "/api/admin/stores/maven/group" ).then().statusCode( NOT_FOUND.getStatusCode() );
        given().when().get( "/api/admin/stores/npm/remote" ).then().statusCode( NOT_FOUND.getStatusCode() );
    }

    @Test
    public void testGetByNameExists()
    {
        given().when()
               .get( "/api/admin/stores/maven/remote/exists" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( MediaType.APPLICATION_JSON )
               .body( "packageType", is( "maven" ) )
               .body( "type", is( "remote" ) )
               .body( "name", is( "exists" ) );
    }

    @Test
    public void testGetByNameNonExists()
    {
        given().when().get( "/api/admin/stores/maven/remote/nonexists" ).then().statusCode( NOT_FOUND.getStatusCode() );
    }

    @Test
    public void testGetByUrlWithTwoValues()
    {
        given().when()
               .get( "/api/admin/stores/maven/remote/query/byUrl?url=http://repo.test" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( MediaType.APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 2 ) );
    }

    @Test
    public void testGetByUrlBadReq()
    {
        given().when()
               .get( "/api/admin/stores/maven/hosted/query/byUrl?url=http://repo.test" )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() )
               .body( "error", is( "Not supporte repository type of hosted" ) );

        given().when()
               .get( "/api/admin/stores/maven/group/query/byUrl?url=http://repo.test" )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() )
               .body( "error", is( "Not supporte repository type of group" ) );
    }

    @Test
    public void testGetByUrlNotFound()
    {
        given().when()
               .get( "/api/admin/stores/npm/remote/query/byUrl?url=http://repo.test" )
               .then()
               .statusCode( NOT_FOUND.getStatusCode() );

    }

    @Test
    public void testGetByUrlWithError()
    {
        given().when()
               .get( "/api/admin/stores/generic-http/remote/query/byUrl?url=http://repo.test" )
               .then()
               .statusCode( INTERNAL_SERVER_ERROR.getStatusCode() );
    }

    @Test
    public void testCreateSuccess()
    {
        Map<String, String> repoToCreate = new HashMap<>();
        repoToCreate.put( "packageType", "maven" );
        repoToCreate.put( "type", "remote" );
        repoToCreate.put( "name", "success" );
        repoToCreate.put( "url", "http://repo.success" );
        repoToCreate.put( "key", "maven:remote:success" );

        given().when()
               .body( repoToCreate )
               .contentType( APPLICATION_JSON )
               .post( "/api/admin/stores/maven/remote" )
               .then()
               .statusCode( CREATED.getStatusCode() )
               .contentType( MediaType.APPLICATION_JSON )
               .body( "packageType", is( "maven" ) )
               .body( "type", is( "remote" ) )
               .body( "name", is( "success" ) );
    }

    @Test
    public void testCreateNonSuccess()
    {
        Map<String, String> repoToCreate = new HashMap<>();
        repoToCreate.put( "packageType", "maven" );
        repoToCreate.put( "type", "remote" );
        repoToCreate.put( "url", "http://repo.success" );
        repoToCreate.put( "name", "nonsuccess" );
        repoToCreate.put( "key", "maven:remote:nonsuccess" );
        given().when()
               .body( repoToCreate )
               .contentType( APPLICATION_JSON )
               .post( "/api/admin/stores/maven/remote" )
               .then()
               .statusCode( CONFLICT.getStatusCode() );
    }

    @Test
    public void testCreateError()
    {
        Map<String, String> repoToCreate = new HashMap<>();
        repoToCreate.put( "packageType", "maven" );
        repoToCreate.put( "type", "remote" );
        repoToCreate.put( "url", "http://repo.success" );
        repoToCreate.put( "name", "error" );
        repoToCreate.put( "key", "maven:remote:error" );
        given().when()
               .body( repoToCreate )
               .contentType( APPLICATION_JSON )
               .post( "/api/admin/stores/maven/remote" )
               .then()
               .statusCode( INTERNAL_SERVER_ERROR.getStatusCode() );
    }
}
