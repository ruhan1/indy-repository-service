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
package org.commonjava.indy.service.repository.ftests.query;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.is;

/**
 * <b>GIVEN:</b>
 * <ul>
 *     <li>3 remote repos, 3 hosted repos and 3 groups, and each has one named with "test1"</li>
 * </ul>
 *
 * <br/>
 * <b>WHEN:</b>
 * <ul>
 *     <li>Client request query api of "get by name" with query parameter "test1"</li>
 * </ul>
 *
 * <br/>
 * <b>THEN:</b>
 * <ul>
 *     <li>There will be only 1 repo returned with the name "test1"</li>
 * </ul>
 */
@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class GetByNameTest
        extends AbstractQueryFuncTest
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    /**
     *
     * <br/>
     * <b>WHEN:</b>
     * <ul>
     *     <li>Client request query api of "get by name" with a name not in system</li>
     * </ul>
     *
     * <br/>
     * <b>THEN:</b>
     * <ul>
     *     <li>Will return 404 to notify no such repo </li>
     * </ul>
     */
    @Test
    public void runWithNoNotFound()
    {
        given().when().get( QUERY_BASE + "/byName/nosuchrepo/" ).then().statusCode( NOT_FOUND.getStatusCode() );
    }

    /**
     *
     * <br/>
     * <b>WHEN:</b>
     * <ul>
     *     <li>Client request query api of "get by name" with query parameter "test1"</li>
     * </ul>
     *
     * <br/>
     * <b>THEN:</b>
     * <ul>
     *     <li>There will be only 1 repo returned with the name "test1"</li>
     * </ul>
     */
    @Test
    public void runWithContent()
    {
        given().when()
               .get( QUERY_BASE + "/byName/test1/" )
               .then()
               .statusCode( OK.getStatusCode() )
               .contentType( APPLICATION_JSON )
               .body( "name", is( "test1" ) );
    }
}
