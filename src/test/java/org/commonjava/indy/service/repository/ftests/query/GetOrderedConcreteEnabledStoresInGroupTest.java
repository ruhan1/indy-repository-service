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
package org.commonjava.indy.service.repository.ftests.query;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.matchers.StoreListingCheckMatcher;
import org.commonjava.indy.service.repository.ftests.profile.MemoryFunctionProfile;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.commonjava.indy.service.repository.testutil.TestUtil.prepareCustomizedMapper;
import static org.hamcrest.CoreMatchers.is;

/**
 * <b>GIVEN:</b>
 * <ul>
 *     <li>3 remote repos, 3 hosted repos and 3 groups</li>
 *     <li>groups contains its own constituents which belong to these 9 repos</li>
 * </ul>
 */
@QuarkusTest
@TestProfile( MemoryFunctionProfile.class )
@Tag( "function" )
public class GetOrderedConcreteEnabledStoresInGroupTest
        extends AbstractQueryFuncTest
{

    /**
     * <br/>
     *  <b>WHEN:</b>
     *  <ul>
     *      <li>Client request query api to get ordered enabled concrete stores in specified group but:
     *          <ul>
     *              <li>Group store key not specified, or</li>
     *              <li>Not a group store key specified, or</li>
     *              <li>Wrong store key format specified</li>
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
    public void runWithBadReq()
    {
        given().when().get( QUERY_BASE + "/concretes/inGroup/" ).then().statusCode( BAD_REQUEST.getStatusCode() );

        given().when()
               .get( QUERY_BASE + "/concretes/inGroup/?storeKey=maven:remote:test1" )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() );

        given().when()
               .get( QUERY_BASE + "/concretes/inGroup/?storeKey=nopkg:group:test1" )
               .then()
               .statusCode( BAD_REQUEST.getStatusCode() );
    }

    /**
     * <br/>
     *  <b>WHEN:</b>
     *  <ul>
     *      <li>Client request query api to get ordered enabled concrete stores in specified group and the correct group store key specified</li>
     *  </ul>
     *
     *  <br/>
     *  <b>THEN:</b>
     *  <ul>
     *      <li>The correct stores can be returned</li>
     *      <li>These stores are ordered and concrete(not group)</li>
     *  </ul>
     */
    @Test
    public void runWithContent()
    {
        final StoreListingCheckMatcher concreteRepoMatcher =
                new StoreListingCheckMatcher( prepareCustomizedMapper(), ( listing ) -> {
                    for ( ArtifactStore store : listing.getItems() )
                    {
                        if ( store.getType() == StoreType.group )
                        {
                            return false;
                        }
                    }
                    return true;
                } );
        given().when()
               .get( QUERY_BASE + "/concretes/inGroup/?storeKey=maven:group:test1" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 3 ) )
               .body( "items[0].key", is( "maven:remote:test2" ) )
               .body( concreteRepoMatcher );

        given().when()
               .get( QUERY_BASE + "/concretes/inGroup/?storeKey=maven:group:test3" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 5 ) )
               .body( "items[0].key", is( "maven:hosted:test1" ) )
               .body( concreteRepoMatcher );

    }

    @Test
    public void runForDisabled()
    {
        final StoreListingCheckMatcher concreteRepoMatcher =
                new StoreListingCheckMatcher( prepareCustomizedMapper(), ( listing ) -> {
                    for ( ArtifactStore store : listing.getItems() )
                    {
                        if ( store.getType() == StoreType.group )
                        {
                            return false;
                        }
                    }
                    return true;
                } );
        given().when()
               .get( QUERY_BASE + "/concretes/inGroup/?storeKey=maven:group:test1&enabled=false" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 1 ) )
               .body( "items[0].key", is( "maven:remote:test1" ) )
               .body( concreteRepoMatcher );

        given().when()
               .get( QUERY_BASE + "/concretes/inGroup/?storeKey=maven:group:test3&enabled=false" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "size()", is( 1 ) )
               .body( "items.size()", is( 1 ) )
               .body( "items[0].key", is( "maven:remote:test1" ) )
               .body( concreteRepoMatcher );

    }
}
