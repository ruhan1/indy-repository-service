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
package org.commonjava.indy.service.repository.ftests.maint;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.AbstractStoreManagementTest;
import org.commonjava.indy.service.repository.ftests.matchers.RepoEqualMatcher;
import org.commonjava.indy.service.repository.ftests.profile.MemoryFunctionProfile;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.commonjava.indy.service.repository.jaxrs.RepositoryMaintenanceResources.MEDIATYPE_APPLICATION_ZIP;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestProfile( MemoryFunctionProfile.class )
@Tag( "function" )
public class ImportBundleTest
        extends AbstractStoreManagementTest
{
    @BeforeEach
    public void setupSkipped()
            throws Exception
    {
        final HostedRepository repo = new HostedRepository( MAVEN_PKG_KEY, "skipped" );
        final String json = mapper.writeValueAsString( repo );

        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( repo.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, repo, HostedRepository.class ) );
    }

    @Test
    public void run()
            throws Exception
    {
        try (InputStream in = Thread.currentThread()
                                    .getContextClassLoader()
                                    .getResourceAsStream( "indy-repo-bundle.zip" ))
        {
            given().when()
                   .body( in )
                   .contentType( MEDIATYPE_APPLICATION_ZIP )
                   .post( "/api/admin/stores/maint/import" )
                   .then()
                   .statusCode( 200 )
                   .contentType( APPLICATION_JSON )
                   .body( "skipped.size()", is( 1 ) )
                   .body( "skipped[0]", is( "repos/maven/hosted/skipped.json" ) )
                   .body( "failed.size()", is( 1 ) )
                   .body( "failed[0]", is( "repos/wrong.json" ) );

            given().when()
                   .get( "api/admin/stores/maven/remote/test1" )
                   .then()
                   .statusCode( OK.getStatusCode() )
                   .contentType( APPLICATION_JSON )
                   .body( "key", is( "maven:remote:test1" ) );

            given().when()
                   .get( "api/admin/stores/npm/hosted/test2" )
                   .then()
                   .statusCode( OK.getStatusCode() )
                   .contentType( APPLICATION_JSON )
                   .body( "key", is( "npm:hosted:test2" ) );
        }
    }

}
