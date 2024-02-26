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
package org.commonjava.indy.service.repository.jaxrs;

import org.apache.commons.lang3.StringUtils;
import org.commonjava.indy.service.repository.change.audit.DtxRepoOpsAuditRecord;
import org.commonjava.indy.service.repository.change.audit.StoreAuditManager;
import org.commonjava.indy.service.repository.config.IndyRepositoryConfiguration;
import org.commonjava.indy.service.repository.controller.MaintenanceController;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.ok;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Tag( name = "Store Maintenance APIs", description = "Resource for maintain artifact repositories" )
@Path( "/api/admin/stores/maint" )
@ApplicationScoped
public class RepositoryMaintenanceResources
{
    public static final String MEDIATYPE_APPLICATION_ZIP = "application/zip";

    @Inject
    MaintenanceController maintController;

    @Inject
    StoreAuditManager auditManager;

    @Inject
    IndyRepositoryConfiguration repoConfig;

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

    @Operation(
            description = "Import a ZIP-compressed file containing repository definitions into the repository management database." )
    @APIResponse( responseCode = "200", description = "All repository definitions which are imported successfully." )
    @POST
    @Path( "/import" )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    @Produces( APPLICATION_JSON )
    public Response importRepoBundle( InputStream input )
    {
        try
        {
            Map<String, List<String>> results = maintController.importRepoBundle( input );
            return ok( results ).build();
        }
        catch ( IOException e )
        {
            throw new WebApplicationException( "Cannot processing the imported bundle.", e );
        }
    }

    @Operation( description = "Check the audit log of the a specified repo changes." )
    @APIResponse( responseCode = "200", description = "The audit log returned" )
    @GET
    @Path( "/audit/{repo}" )
    @Consumes( MEDIATYPE_APPLICATION_ZIP )
    @Produces( APPLICATION_JSON )
    public Response getStoreAuditLogs( final @PathParam( "repo" ) String repoName,
                                       final @QueryParam( "ops" ) String ops,
                                       final @QueryParam( "limit" ) String limit )
    {
        if ( repoConfig.repoAuditEnabled() )
        {
            List<DtxRepoOpsAuditRecord> records;
            if ( isBlank( repoName ) )
            {
                return Response.status( BAD_REQUEST ).entity( "The repository name cannot be null" ).build();
            }
            int limitRecords;
            try
            {
                limitRecords = StringUtils.isNotBlank( limit ) && Integer.parseInt( limit ) > 0 ?
                        Integer.parseInt( limit ) :
                        1000;
            }
            catch ( NumberFormatException e )
            {
                limitRecords = 1000;
            }
            if ( isNotBlank( ops ) )
            {
                records = auditManager.getAuditLogByRepoAndOps( repoName, ops, limitRecords );
            }
            else
            {
                records = auditManager.getAuditLogByRepo( repoName, limitRecords );
            }

            if ( records != null && !records.isEmpty() )
            {
                return Response.ok( records ).build();
            }
            return Response.status( NOT_FOUND ).build();
        }
        else
        {
            return Response.status( FORBIDDEN )
                           .entity( "{\"error\" : \"Repository audit log is not enabled!\"}" )
                           .build();
        }
    }
}
