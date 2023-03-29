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

import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.indy.service.repository.controller.QueryController;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.dto.SimpleBooleanResultDTO;
import org.commonjava.indy.service.repository.model.dto.StoreListingDTO;
import org.commonjava.indy.service.repository.util.UrlUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Tag( name = "Store Querying APIs", description = "Resource for querying artifact store definitions" )
@Path( "/api/admin/stores/query" )
@ApplicationScoped
public class RepositoryQueryResources
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    ResponseHelper responseHelper;

    @Inject
    QueryController queryController;

    public RepositoryQueryResources()
    {
        logger.info( "\n\n\n\nStarted Store Querying resources\n\n\n\n" );
    }

    @Operation( description = "Retrieve all repository definitions" )
    @Parameters( value = {
            @Parameter( name = "packageType", in = QUERY, description = "The package type of the repository.",
                        example = "maven, npm, generic-http" ),
            @Parameter( name = "types", in = QUERY, description = "The types of the repository. Split by comma",
                        example = "\"remote, hosted\"" ), @Parameter( name = "enabled", in = QUERY,
                                                                      description = "If the repositories retrieved are enabled, default is true if not specified",
                                                                      example = "true|false" ) } )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @Path( "/all" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getAll( @QueryParam( "packageType" ) final String packageType,
                            @QueryParam( "types" ) final String repoTypes,
                            @QueryParam( "enabled" ) final String enabled )
    {
        return generateStoreListingResponse(
                () -> queryController.getAllArtifactStores( packageType, repoTypes, enabled ) );
    }

    @Operation( description = "Retrieve all remote repository definitions by specified package type" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @GET
    @Path( "/remotes/all" )
    @Produces( APPLICATION_JSON )
    public Response getAllRemoteRepositories(
            @Parameter( description = "package type for the remotes, default is maven if not specified",
                        example = "maven|npm" ) @QueryParam( "packageType" ) final String packageType,
            @Parameter( description = "If the repositories retrieved are enabled, default is true if not specified",
                        example = "true" ) @QueryParam( "enabled" ) final String enabled )
    {
        return generateStoreListingResponse( () -> queryController.getAllRemoteRepositories( packageType, enabled ) );
    }

    @Operation( description = "Retrieve all hosted repository definitions by specified package type" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @GET
    @Path( "/hosteds/all" )
    @Produces( APPLICATION_JSON )
    public Response getAllHostedRepositories(
            @Parameter( description = "package type for the hosted repos, default is maven if not specified",
                        example = "maven|npm" ) @QueryParam( "packageType" ) final String packageType,
            @Parameter( description = "If the repositories retrieved are enabled, default is true if not specified",
                        example = "true" ) @QueryParam( "enabled" ) final String enabled )
    {
        return generateStoreListingResponse( () -> queryController.getAllHostedRepositories( packageType, enabled ) );
    }

    @Operation( description = "Retrieve all group definitions by specified package type" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @GET
    @Path( "/groups/all" )
    @Produces( APPLICATION_JSON )
    public Response getAllGroups(
            @Parameter( description = "package type for the groups,  default is maven if not specified",
                        example = "maven|npm" ) @QueryParam( "packageType" ) final String packageType,
            @Parameter( description = "If the repositories retrieved are enabled, default is true if not specified",
                        example = "true" ) @QueryParam( "enabled" ) final String enabled )
    {
        return generateStoreListingResponse( () -> queryController.getAllGroups( packageType, enabled ) );
    }

    @Operation( description = "Retrieve all default package types" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @GET
    @Path( "/byDefaultPkgTypes" )
    @Produces( APPLICATION_JSON )
    public Response getAllByDefaultPackageTypes()
    {
        return generateStoreListingResponse( () -> queryController.getAllByDefaultPackageTypes() );
    }

    @Operation( description = "Retrieve the first matched store with the given store name" )
    @APIResponse( responseCode = "200", content = @Content( schema = @Schema( implementation = ArtifactStore.class ) ),
                  description = "The store definition" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @GET
    @Path( "/byName/{name}" )
    @Produces( APPLICATION_JSON )
    public Response getByName(
            @Parameter( description = "Name of the repository", required = true ) @PathParam( "name" ) String name )
    {
        try
        {
            return ok( queryController.getByName( name ) ).build();
        }
        catch ( IndyWorkflowException e )
        {
            logger.error( e.getMessage() );
            return responseHelper.formatResponse( e );
        }
    }

    @Operation( description = "Retrieve the enabled groups whose constituents contains the specified store" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @GET
    @Path( "/groups/contains" )
    @Produces( APPLICATION_JSON )
    public Response getGroupsContaining(
            @Parameter( description = "Key of the repository contained in the groups", required = true,
                        example = "maven:remote:central" ) @QueryParam( "storeKey" ) @Encoded final String storeKey,
            @Parameter( description = "If the repositories retrieved are enabled, default is true if not specified",
                        example = "true" ) @QueryParam( "enabled" ) final String enabled )
    {
        final String storeKeyDecoded = UrlUtils.uriDecode( storeKey );
        return generateStoreListingResponse( () -> queryController.getGroupsContaining( storeKeyDecoded, enabled ) );
    }

    @Operation( description = "Retrieve the concrete stores which are constituents of the specified group" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @GET
    @Path( "/concretes/inGroup" )
    @Produces( APPLICATION_JSON )
    public Response getOrderedConcreteStoresInGroup(
            @Parameter( description = "Key of the group whom the repositories are contained in", required = true,
                        example = "maven:group:public" ) @QueryParam( "storeKey" ) @Encoded final String storeKey,
            @Parameter( description = "If the repositories retrieved are enabled, default is true if not specified",
                        example = "true" ) @QueryParam( "enabled" ) final String enabled )
    {
        logger.debug( "StoreKey is {}", storeKey );
        final String storeKeyDecoded = UrlUtils.uriDecode( storeKey );
        logger.debug( "StoreKey decoded is {}", storeKeyDecoded );
        return generateStoreListingResponse(
                () -> queryController.getOrderedConcreteStoresInGroup( storeKeyDecoded, enabled ) );
    }

    @Operation( description = "Retrieve the stores which are constituents of the specified group" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The stores definitions, include the master group itself" )
    @APIResponse( responseCode = "404", description = "The stores are not found" )
    @GET
    @Path( "/inGroup" )
    @Produces( APPLICATION_JSON )
    public Response getOrderedStoresInGroup(
            @Parameter( description = "Key of the group whom the repositories are contained in", required = true,
                        example = "maven:group:public" ) @QueryParam( "storeKey" ) @Encoded final String storeKey,
            @Parameter( description = "If the repositories retrieved are enabled, default is true if not specified",
                        example = "true" ) @QueryParam( "enabled" ) final String enabled )
    {
        final String storeKeyDecoded = UrlUtils.uriDecode( storeKey );
        return generateStoreListingResponse(
                () -> queryController.getOrderedStoresInGroup( storeKeyDecoded, enabled ) );
    }

    @Operation( description = "Retrieve the groups which are affected by the specified store keys" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The group definitions" )
    @APIResponse( responseCode = "404", description = "The groups don't exist" )
    @GET
    @Path( "/affectedBy" )
    @Produces( APPLICATION_JSON )
    public Response getGroupsAffectedBy(
            @Parameter( description = "Store keys whom the groups are affected by, use \",\" to split", required = true,
                        example = "maven:remote:central,maven:hosted:local" ) @QueryParam( "keys" ) @Encoded
            final String keys )
    {
        if ( keys == null )
        {
            IndyWorkflowException e =
                    new IndyWorkflowException( BAD_REQUEST.getStatusCode(), "Illegal storeKeys: can not be null" );
            logger.error( e.getMessage() );
            return responseHelper.formatResponse( e );
        }
        final String keysDecoded = UrlUtils.uriDecode( keys );
        return generateStoreListingResponse( () -> {
            String[] keysArr = keysDecoded.split( "," );
            if ( keysArr.length == 0 )
            {
                throw new IndyWorkflowException( BAD_REQUEST.getStatusCode(), "Illegal storeKeys: can not be empty" );
            }
            return queryController.getGroupsAffectedBy( keysArr );
        } );
    }

    @Operation( description = "Retrieve the remote repositories by package type and urls." )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The remote repository definitions" )
    @APIResponse( responseCode = "404", description = "The remote repositories don't exist" )
    @GET
    @Path( "/remotes" )
    public Response getRemoteRepositoryByUrl( @QueryParam( "packageType" ) final String packageType,
                                              @QueryParam( "byUrl" ) final String url,
                                              @QueryParam( "enabled" ) final String enabled )
    {
        return generateStoreListingResponse(
                () -> queryController.queryRemotesByPackageTypeAndUrl( packageType, url, enabled ) );
    }

    @Operation( description = "Check if there are no repository definitions." )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = SimpleBooleanResultDTO.class ) ),
                  description = "If there are repository definitions or not." )
    @GET
    @Path( "/isEmpty" )
    public Response getStoreEmpty()
    {
        Boolean result = queryController.isStoreDataEmpty();
        SimpleBooleanResultDTO dto = new SimpleBooleanResultDTO();
        dto.setDescription( "Check if there are no repository definitions." );
        dto.setResult( result );
        return responseHelper.formatOkResponseWithJsonEntity( dto );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private Response generateStoreListingResponse( ArtifactStoreListSupplier supplier )
    {
        try
        {
            final List<? extends ArtifactStore> stores = supplier.get();
            logger.debug( "Returning listing containing stores:\n\t{}", new JoinString( "\n\t", stores ) );
            if ( stores == null || stores.isEmpty() )
            {
                return Response.status( NOT_FOUND ).build();
            }
            final StoreListingDTO<ArtifactStore> dto = new StoreListingDTO( stores );
            return responseHelper.formatOkResponseWithJsonEntity( dto );
        }
        catch ( IndyWorkflowException e )
        {
            logger.error( e.getMessage() );
            return responseHelper.formatResponse( e );
        }
    }

    @FunctionalInterface
    private interface ArtifactStoreListSupplier
    {
        List<? extends ArtifactStore> get()
                throws IndyWorkflowException;
    }
}
