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
package org.commonjava.indy.service.repository.ftests.query;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;

/**
 * <b>GIVEN:</b>
 * <ul>
 *     <li>3 remote repos, 3 hosted repos and 3 groups</li>
 *     <li>groups contains its own constituents which belong to these 9 repos</li>
 * </ul>
 */
@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class GetEnabledGroupsContainingTest
        extends AbstractQueryFuncTest
{
    /**
     * <br/>
     *  <b>WHEN:</b>
     *  <ul>
     *      <li>Client request query api to get groups containing, but:
     *          <ul>
     *              <li>Contains storekey not specified, or</li>
     *              <li>Wrong storekey format specified</li>
     *          </ul>
     *      </li>
     *  </ul>
     *
     *  <br/>
     *  <b>THEN:</b>
     *  <ul>
     *      <li>Will return bad request response</li>
     *  </ul>
     */
    @Test
    public void runWithBadRequest()
    {
        given().when().get( QUERY_BASE + "/groups/contains" ).then().statusCode( BAD_REQUEST.getStatusCode() );

        given().when()
               .get( QUERY_BASE + "/groups/contains?storeKey=nopkg:remote:test2" )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() );

    }

    /**
     * <br/>
     *  <b>WHEN:</b>
     *  <ul>
     *      <li>Client request query api to get groups which contains specified repo</li>
     *  </ul>
     *
     *  <br/>
     *  <b>THEN:</b>
     *  <ul>
     *      <li>The correct groups can be returned</li>
     *  </ul>
     */
    @Test
    public void runWithContent()
    {
        given().when()
               .get( QUERY_BASE + "/groups/contains?storeKey=maven:remote:test1" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 2 ) );

        given().when()
               .get( QUERY_BASE + "/groups/contains?storeKey=maven:remote:test2" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 2 ) );

        given().when()
               .get( QUERY_BASE + "/groups/contains?storeKey=maven:remote:test3" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 1 ) )
               .body( "items[0].key", is( "maven:group:test2" ) );
    }
}
