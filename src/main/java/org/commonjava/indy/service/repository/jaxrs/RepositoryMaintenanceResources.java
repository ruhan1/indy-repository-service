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

import org.commonjava.indy.service.repository.controller.MaintenanceController;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ok;

@Tag( name = "Store Maintenance APIs", description = "Resource for maintain artifact repositories" )
@Path( "/admin/stores/maint" )
@ApplicationScoped
public class RepositoryMaintenanceResources
{
    public static final String MEDIATYPE_APPLICATION_ZIP = "application/zip";

    @Inject
    MaintenanceController maintController;

    @Operation( description = "Retrieve a ZIP-compressed file containing all repository definitions." )
    @APIResponse( responseCode = "200", description = "The zip file contains all repos definitions" )
    @GET
    @Path( "/export" )
    @Produces( MEDIATYPE_APPLICATION_ZIP )
    public Response getRepoBundle()
    {
        try
        {
            File bundle = maintController.getRepoBundle();
            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.info( "Returning repo bundle: {}", bundle );

            return ok( bundle ).header( CONTENT_DISPOSITION,
                                        "attachment; filename=indy-repo-bundle-" + currentTimeMillis() + ".zip" )
                               .build();
        }
        catch ( IOException e )
        {
            throw new WebApplicationException( "Cannot retrieve repository files, or failed to write to bundle zip.",
                                               e );
        }
    }

    @POST
    @Path( "/import" )
    @Consumes( MEDIATYPE_APPLICATION_ZIP )
    @Produces( APPLICATION_JSON )
    public Response importRepoBundle( @Context final HttpRequest request )
    {
        try
        {
            Map<String, List<String>> results = maintController.importRepoBundle( request.getInputStream() );
            return ok( results ).build();
        }
        catch ( IOException e )
        {
            throw new WebApplicationException( "Cannot processing the imported bundle.", e );
        }
    }
}
