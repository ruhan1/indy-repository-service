/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy-repository-service)
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
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;
import static org.commonjava.indy.service.repository.util.PathUtils.normalize;

@QuarkusTest
@TestProfile( MockTestProfile.class )
@TestSecurity( authorizationEnabled = true, roles = {"power-user"}, user = "pouser")
public class RepositoryAdminResourcesTest
{
    private static final String BASE_STORE_PATH = "/api/admin/stores";

    @Test
    public void testExists()
    {
        given().when()
               .head( normalize( BASE_STORE_PATH, "maven/remote/exists" ) )
               .then()
               .statusCode( OK.getStatusCode() );
        given().when()
               .head( normalize( BASE_STORE_PATH, "maven/remote/nonexists" ) )
               .then()
               .statusCode( NOT_FOUND.getStatusCode() );
    }

    @Test
    public void testGetAllWithTwoValues()
    {
        given().when()
               .get( normalize( BASE_STORE_PATH, "maven/remote" ) )
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
               .get( normalize( BASE_STORE_PATH, "maven/hosted" ) )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( MediaType.APPLICATION_JSON )
               .contentType( MediaType.APPLICATION_JSON )
               .body( "size()", is( 0 ) );
    }

    @Test
    public void testGetAllNotFound()
    {
        given().when()
               .get( normalize( BASE_STORE_PATH, "maven/group" ) )
               .then()
               .statusCode( NOT_FOUND.getStatusCode() );
        given().when().get( normalize( BASE_STORE_PATH, "npm/remote" ) ).then().statusCode( NOT_FOUND.getStatusCode() );
    }

    @Test
    public void testGetByNameExists()
    {
        given().when()
               .get( normalize( BASE_STORE_PATH, "maven/remote/exists" ) )
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
        given().when()
               .get( normalize( BASE_STORE_PATH, "maven/remote/nonexists" ) )
               .then()
               .statusCode( NOT_FOUND.getStatusCode() );
    }

    @Test
    public void testGetByUrlWithTwoValues()
    {
        given().when()
               .get( BASE_STORE_PATH + "/maven/remote/query/byUrl?url=http://repo.test" )
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
               .get( BASE_STORE_PATH + "/maven/hosted/query/byUrl?url=http://repo.test" )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() )
               .body( "error", is( "Not supporte repository type of hosted" ) );

        given().when()
               .get( normalize( BASE_STORE_PATH, "maven/group/query/byUrl?url=http://repo.test" ) )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() )
               .body( "error", is( "Not supporte repository type of group" ) );
    }

    @Test
    public void testGetByUrlNotFound()
    {
        given().when()
               .get( BASE_STORE_PATH + "npm/remote/query/byUrl?url=http://repo.test" )
               .then()
               .statusCode( NOT_FOUND.getStatusCode() );

    }

    @Test
    public void testGetByUrlWithError()
    {
        given().when()
               .get( BASE_STORE_PATH + "/generic-http/remote/query/byUrl?url=http://repo.test" )
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
               .post( normalize( BASE_STORE_PATH, "maven/remote" ) )
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
               .post( normalize( BASE_STORE_PATH, "maven/remote" ) )
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
               .post( normalize( BASE_STORE_PATH, "maven/remote" ) )
               .then()
               .statusCode( INTERNAL_SERVER_ERROR.getStatusCode() );
    }

    @Test
    public void testChangeSuccess()
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
               .put( normalize( BASE_STORE_PATH, "maven/remote/success" ) )
               .then()
               .statusCode( OK.getStatusCode() );
    }

    @Test
    public void testChangeNonSuccess()
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
               .put( normalize( BASE_STORE_PATH, "maven/remote/nonsuccess" ) )
               .then()
               .statusCode( NOT_MODIFIED.getStatusCode() );
    }

    @Test
    public void testChangeBadReq()
    {
        Map<String, String> repoToCreate = new HashMap<>();
        repoToCreate.put( "packageType", "maven" );
        repoToCreate.put( "type", "remote" );
        repoToCreate.put( "url", "http://repo.success" );
        repoToCreate.put( "name", "wrong" );
        repoToCreate.put( "key", "maven:remote:wrong" );
        RestAssured.registerParser( APPLICATION_OCTET_STREAM, Parser.TEXT );
        given().when()
               .body( repoToCreate )
               .contentType( APPLICATION_JSON )
               .put( normalize( BASE_STORE_PATH, "maven/remote/error" ) )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() )
               .contentType( APPLICATION_OCTET_STREAM )
               .body( is( "Store in URL path is: 'maven:remote:error' but in JSON it is: 'maven:remote:wrong'" ) );
    }

    @Test
    public void testChangeError()
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
               .put( normalize( BASE_STORE_PATH, "maven/remote/error" ) )
               .then()
               .statusCode( INTERNAL_SERVER_ERROR.getStatusCode() );
    }

    @Test
    public void testDelete()
    {
        given().when()
               .contentType( APPLICATION_JSON )
               .delete( normalize( BASE_STORE_PATH, "maven/remote/success" ) )
               .then()
               .statusCode( NO_CONTENT.getStatusCode() );
    }

    @Test
    public void testDeleteError()
    {
        given().when()
               .contentType( APPLICATION_JSON )
               .delete( normalize( BASE_STORE_PATH, "maven/remote/error" ) )
               .then()
               .statusCode( INTERNAL_SERVER_ERROR.getStatusCode() );
    }

    @Test
    public void testGetAllInValid()
    {
        given().when()
               .get( normalize( BASE_STORE_PATH, "maven/remote/invalid/all" ) )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( MediaType.APPLICATION_JSON )
               .body( "size()", is( 2 ) );
    }

    @Test
    public void testGetAllInValidErrorType()
    {
        given().when()
               .get( normalize( BASE_STORE_PATH, "maven/hosted/invalid/all" ) )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() )
               .contentType( MediaType.APPLICATION_JSON )
               .body( "error", is( "Not supporte repository type of hosted" ) );
    }
}
