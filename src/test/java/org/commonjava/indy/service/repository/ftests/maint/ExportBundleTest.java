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
package org.commonjava.indy.service.repository.ftests.maint;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static org.commonjava.indy.service.repository.jaxrs.RepositoryMaintenanceResources.MEDIATYPE_APPLICATION_ZIP;

@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class ExportBundleTest
{

    @Test
    public void run()
            throws Exception
    {
        given()
                   .when()
                   .get( "/api/admin/stores/maint/export" )
                   .then()
                   .statusCode( 200 )
                   .contentType( MEDIATYPE_APPLICATION_ZIP )
                   .header( CONTENT_DISPOSITION,
                            CoreMatchers.containsString( "attachment; filename=indy-repo-bundle-" ) );
    }

}

