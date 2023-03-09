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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.indy.service.repository.controller.AdminController;
import org.commonjava.indy.service.repository.data.ArtifactStoreValidateData;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.commonjava.indy.service.repository.model.dto.StoreListingDTO;
import org.commonjava.indy.service.repository.util.jackson.MapperUtil;
import org.commonjava.indy.service.security.common.SecurityManager;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.notModified;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.commonjava.indy.service.repository.model.ArtifactStore.METADATA_CHANGELOG;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.PATH;

@Tag( name = "Store Administration", description = "Resource for accessing and managing artifact store definitions" )
@Path( "/api/admin/stores/{packageType}/{type: (hosted|group|remote)}" )
@ApplicationScoped
public class RepositoryAdminResources
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    AdminController adminController;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SecurityManager securityManager;

    @Inject
    ResponseHelper responseHelper;

    public RepositoryAdminResources()
    {
        logger.info( "\n\n\n\nStarted Store Administration resources\n\n\n\n" );
    }

    @Operation( description = "Check if a given store exists" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository.",
                        content = @Content( schema = @Schema( implementation = StoreType.class ) ),
                        required = true ) } )
    @APIResponse( responseCode = "200", description = "The store exists" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @Path( "/{name}" )
    @HEAD
    public Response exists( final @PathParam( "packageType" ) String packageType, @PathParam( "type" ) String type,
                            @Parameter( in = PATH, required = true ) @PathParam( "name" ) final String name )
    {
        Response response;
        final StoreType st = StoreType.get( type );

        logger.info( "Checking for existence of: {}:{}:{}", packageType, st, name );

        if ( adminController.exists( new StoreKey( packageType, st, name ) ) )
        {

            logger.info( "returning OK" );
            response = Response.ok().build();
        }
        else
        {
            logger.info( "Returning NOT FOUND" );
            response = Response.status( Status.NOT_FOUND ).build();
        }
        return response;
    }

    @Operation( description = "Create a new store" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository.",
                        content = @Content( schema = @Schema( implementation = StoreType.class ) ),
                        required = true ) } )
    @APIResponse( responseCode = "201", content = @Content( schema = @Schema( implementation = ArtifactStore.class ) ),
                  description = "The store was created" )
    @APIResponse( responseCode = "409", description = "A store with the specified type and name already exists" )
    @RequestBody( description = "The artifact store definition JSON", name = "body", required = true,
                  content = @Content( schema = @Schema( implementation = ArtifactStore.class ) ) )
    @POST
    @Consumes( APPLICATION_JSON )
    @Produces( APPLICATION_JSON )
    public Response create( final @PathParam( "packageType" ) String packageType, @PathParam( "type" ) String type,
                            final @Context UriInfo uriInfo, final @Context HttpRequest request )
    {
        final StoreType st = StoreType.get( type );

        Response response;
        String json;
        try
        {
            json = IOUtils.toString( request.getInputStream(), Charset.defaultCharset() );

            //            logger.warn("=> JSON: " + json);

            json = MapperUtil.patchLegacyStoreJson( objectMapper, json );
        }
        catch ( final IOException e )
        {
            final String message = "Failed to read " + st.getStoreClass().getSimpleName() + " from request body.";

            logger.error( message, e );
            response = responseHelper.formatResponse( e, message );
            return response;
        }

        ArtifactStore store;
        try
        {
            store = objectMapper.readValue( json, st.getStoreClass() );
        }
        catch ( final IOException e )
        {
            final String message = "Failed to parse " + st.getStoreClass().getSimpleName() + " from request body.";

            logger.error( message, e );
            response = responseHelper.formatResponse( e, message );
            return response;
        }

        logger.info( "\n\nGot artifact store: {}\n\n", store );

        try
        {
            String user = securityManager.getUser( request );

            if ( adminController.store( store, user, false ) )
            {
                final URI uri = uriInfo.getBaseUriBuilder()
                                       .path( "/api/admin/stores" )
                                       .path( store.getPackageType() )
                                       .path( store.getType().singularEndpointName() )
                                       .build( store.getName() );

                response = responseHelper.formatCreatedResponseWithJsonEntity( uri, store );
            }
            else
            {
                response = status( CONFLICT ).entity( "{\"error\": \"Store already exists.\"}" )
                                             .type( APPLICATION_JSON )
                                             .build();
            }
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( e.getMessage(), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @Operation( description = "Update an existing store" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository.",
                        content = @Content( schema = @Schema( implementation = StoreType.class ) ),
                        required = true ) } )
    @RequestBody( description = "The artifact store definition JSON", name = "body", required = true,
                  content = @Content( schema = @Schema( implementation = ArtifactStore.class ) ) )
    @APIResponse( responseCode = "200", content = @Content( schema = @Schema( implementation = ArtifactStore.class ) ),
                  description = "The store was updated" )
    @APIResponse( responseCode = "400",
                  description = "The store specified in the body JSON didn't match the URL parameters" )
    @Path( "/{name}" )
    @PUT
    @Consumes( APPLICATION_JSON )
    public Response store( final @PathParam( "packageType" ) String packageType, @PathParam( "type" ) String type,
                           final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
                           final @Context HttpRequest request )
    {
        final StoreType st = StoreType.get( type );

        Response response = null;
        String json = null;
        try
        {
            json = IOUtils.toString( request.getInputStream(), Charset.defaultCharset() );
            logger.info( "{}", json );
            json = MapperUtil.patchLegacyStoreJson( objectMapper, json );

        }
        catch ( final IOException e )
        {
            final String message = "Failed to read " + st.getStoreClass().getSimpleName() + " from request body.";

            logger.error( message, e );
            response = responseHelper.formatResponse( e, message );
        }

        if ( response != null )
        {
            return response;
        }

        ArtifactStore store;
        try
        {
            store = objectMapper.readValue( json, st.getStoreClass() );
        }
        catch ( final IOException e )
        {
            final String message = "Failed to parse " + st.getStoreClass().getSimpleName() + " from request body.";

            logger.error( message, e );
            response = responseHelper.formatResponse( e, message );
            return response;
        }

        if ( !packageType.equals( store.getPackageType() ) || st != store.getType() || !name.equals( store.getName() ) )
        {
            return Response.status( Status.BAD_REQUEST )
                           .entity( String.format( "Store in URL path is: '%s' but in JSON it is: '%s'",
                                                   new StoreKey( packageType, st, name ), store.getKey() ) )
                           .build();
        }

        try
        {
            final String user = securityManager.getUser( request );

            logger.info( "Storing: {}", store );
            if ( adminController.store( store, user, false ) )
            {
                response = ok().build();
            }
            else
            {
                logger.warn( "{} NOT modified!", store );
                response = notModified().build();
            }
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( e.getMessage(), e );
            response = responseHelper.formatResponse( e );
        }

        return response;
    }

    @Operation( description = "Retrieve the definitions of all artifact stores of a given type on the system" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getAll( final @Parameter(
            description = "Filter only stores that support the package type (eg. maven, npm). NOTE: '_all' returns all." )
                            @PathParam( "packageType" ) String packageType,
                            @Parameter( name = "type", in = PATH, description = "The type of the repository.",
                                        content = @Content( schema = @Schema( implementation = StoreType.class ) ),
                                        required = true ) @PathParam( "type" ) String type )
    {

        final StoreType st = StoreType.get( type );

        Response response;
        try
        {
            final List<ArtifactStore> stores = adminController.getAllOfType( packageType, st );

            logger.info( "Returning listing containing stores:\n\t{}", new JoinString( "\n\t", stores ) );

            final StoreListingDTO<ArtifactStore> dto = new StoreListingDTO<>( stores );

            response = responseHelper.formatOkResponseWithJsonEntity( dto );
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( e.getMessage(), e );
            response = responseHelper.formatResponse( e );
        }

        return response;
    }

    @Operation( description = "Retrieve the definition of a specific artifact store" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository.",
                        content = @Content( schema = @Schema( implementation = StoreType.class ) ),
                        required = true ) } )
    @APIResponse( responseCode = "200", content = @Content( schema = @Schema( implementation = ArtifactStore.class ) ),
                  description = "The store definition" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @Path( "/{name}" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response get( final @PathParam( "packageType" ) String packageType, @PathParam( "type" ) String type,
                         final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name )
    {
        logger.info( "{}:{}:{}", packageType, type, name );
        final StoreType st = StoreType.get( type );
        final StoreKey key = new StoreKey( packageType, st, name );

        Response response;
        try
        {
            final ArtifactStore store = adminController.get( key );
            logger.info( "Returning repository: {}", store );

            if ( store == null )
            {
                response = Response.status( Status.NOT_FOUND ).build();
            }
            else
            {
                response = responseHelper.formatOkResponseWithJsonEntity( store );
            }
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( e.getMessage(), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @Operation( description = "Delete an artifact store" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository.",
                        content = @Content( schema = @Schema( implementation = StoreType.class ) ),
                        required = true ) } )
    @APIResponse( responseCode = "204", content = @Content( schema = @Schema( implementation = ArtifactStore.class ) ),
                  description = "The store was deleted (or didn't exist in the first place)" )
    @Path( "/{name}" )
    @DELETE
    public Response delete( final @PathParam( "packageType" ) String packageType, @PathParam( "type" ) String type,
                            final @Parameter( in = PATH, required = true ) @PathParam( "name" ) String name,
                            final @QueryParam( "deleteContent" ) boolean deleteContent,
                            @Context final HttpRequest request )
    {
        final StoreType st = StoreType.get( type );
        final StoreKey key = new StoreKey( packageType, st, name );

        logger.info( "Deleting: {}, deleteContent: {}", key, deleteContent );
        Response response;
        try
        {
            String summary = null;
            try
            {
                summary = IOUtils.toString( request.getInputStream(), Charset.defaultCharset() );
            }
            catch ( final IOException e )
            {
                // no problem, try to get the summary from a header instead.
                logger.info( "store-deletion change summary not in request body, checking headers." );
            }

            if ( isEmpty( summary ) )
            {
                summary = request.getHttpHeaders().getHeaderString( METADATA_CHANGELOG );
            }

            if ( isEmpty( summary ) )
            {
                summary = "Changelog not provided";
            }
            summary += ( ", deleteContent:" + deleteContent );

            String user = securityManager.getUser( request );

            adminController.delete( key, user, summary, deleteContent );

            response = noContent().build();
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( e.getMessage(), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @Operation( description = "Retrieve the definition of a remote by specific url" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository. Must be remote.",
                        schema = @Schema( enumeration = { "remote" } ), required = true ) } )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The remote store definitions" )
    @APIResponse( responseCode = "404", description = "The remote repository doesn't exist" )
    @Path( "/query/byUrl" )
    @GET
    public Response getRemoteByUrl( final @PathParam( "packageType" ) String packageType,
                                    final @Parameter( in = PATH, schema = @Schema( enumeration = { "remote" } ),
                                                      required = true ) @PathParam( "type" ) String type,
                                    final @QueryParam( "url" ) String url, @Context final HttpRequest request )
    {
        if ( !"remote".equals( type ) )
        {
            return responseHelper.formatBadRequestResponse(
                    String.format( "Not supporte repository type of %s", type ) );
        }

        logger.info( "Get remote repository by url: {}", url );
        Response response;
        try
        {
            final List<RemoteRepository> remotes = adminController.getRemoteByUrl( url, packageType );
            logger.info( "According to url {}, Returning remote listing remote repositories: {}", url, remotes );

            if ( remotes == null || remotes.isEmpty() )
            {
                response = Response.status( Status.NOT_FOUND ).build();
            }
            else
            {
                final StoreListingDTO<RemoteRepository> dto = new StoreListingDTO<>( remotes );
                response = responseHelper.formatOkResponseWithJsonEntity( dto );
            }
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( e.getMessage(), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @Operation( description = "Revalidation of Artifacts Stored on demand" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository.",
                        content = @Content( schema = @Schema( implementation = StoreType.class ) ),
                        required = true ) } )
    @APIResponse( responseCode = "200", content = @Content( schema = @Schema( implementation = Map.class ) ),
                  description = "Revalidation for Remote Repositories was successfull" )
    @APIResponse( responseCode = "404", description = "Revalidation is not successfull" )
    @Path( "/revalidate/all/" )
    @POST
    public Response revalidateArtifactStores( @PathParam( "packageType" ) String packageType,
                                              @PathParam( "type" ) String type )
    {

        ArtifactStoreValidateData result;
        Map<String, ArtifactStoreValidateData> results = new HashMap<>();
        Response response;

        try
        {
            StoreType storeType = StoreType.get( type );

            List<ArtifactStore> allArtifactStores = adminController.getAllOfType( packageType, storeType );

            for ( ArtifactStore artifactStore : allArtifactStores )
            {
                // Validate this Store
                result = adminController.validateStore( artifactStore );
                results.put( artifactStore.getKey().toString(), result );
            }
            response = responseHelper.formatOkResponseWithJsonEntity( results );

        }
        catch ( IndyWorkflowException iwe )
        {
            logger.warn( "=> [IndyWorkflowException] exception message: " + iwe.getMessage() );
            response = responseHelper.formatResponse( iwe );

        }
        return response;
    }

    @Operation( description = "Revalidation of Artifact Stored on demand based on package, type and name" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository.",
                        content = @Content( schema = @Schema( implementation = StoreType.class ) ),
                        required = true ) } )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = ArtifactStoreValidateData.class ) ),
                  description = "Revalidation for Remote Repository was successful" )
    @APIResponse( responseCode = "404", description = "Revalidation is not successful" )
    @Path( "/{name}/revalidate" )
    @POST
    public Response revalidateArtifactStore( final @PathParam( "packageType" ) String packageType,
                                             @PathParam( "type" ) String type,
                                             final @Parameter( in = PATH, required = true )
                                             @PathParam( "name" ) String name )
    {

        ArtifactStoreValidateData result;

        Response response;

        try
        {
            final StoreType st = StoreType.get( type );
            final StoreKey key = new StoreKey( packageType, st, name );
            final ArtifactStore store = adminController.get( key );
            logger.info( "=> Returning repository: {}", store );

            // Validate this Store
            result = adminController.validateStore( store );

            logger.warn( "=> Result from Validating Store: " + result );
            if ( result == null )
            {
                response = Response.status( Status.NOT_FOUND ).build();
            }
            else
            {
                response = responseHelper.formatOkResponseWithJsonEntity( result );
            }

        }
        catch ( IndyWorkflowException iwe )
        {
            logger.warn( "=> [IndyWorkflowException] exception message: " + iwe.getMessage() );
            response = responseHelper.formatResponse( iwe );
        }

        return response;
    }

    @Operation( description = "Return All Invalidated Remote Repositories" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = PATH, description = "The package type of the repository.",
                        example = "maven, npm, generic-http", required = true ),
            @Parameter( name = "type", in = PATH, description = "The type of the repository. Must be remote.",
                        schema = @Schema( enumeration = { "remote" } ), required = true ) } )
    @APIResponse( responseCode = "200", description = "Return All Invalidated Remote Repositories" )
    @Path( "/invalid/all" )
    @GET
    public Response returnDisabledStores( @PathParam( "packageType" ) String packageType,
                                          @PathParam( "type" ) String type )
    {
        if ( !"remote".equals( type ) )
        {
            return responseHelper.formatBadRequestResponse(
                    String.format( "Not supporte repository type of %s", type ) );
        }
        return responseHelper.formatOkResponseWithJsonEntity( adminController.getDisabledRemoteRepositories() );
    }

}