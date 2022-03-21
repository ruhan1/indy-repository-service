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

import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.indy.service.repository.controller.QueryController;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.dto.StoreListingDTO;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

@Tag( name = "Store Querying APIs", description = "Resource for querying artifact store definitions" )
@Path( "/stores/query" )
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
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definition" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @Path( "/all" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getAll()
    {
        return generateStoreListingResponse( () -> queryController.getAllArtifactStores() );
    }

    @Operation( description = "Retrieve all remote repository definitions by specified package type" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @GET
    @Path( "/allRemotes" )
    @Produces( APPLICATION_JSON )
    public Response getAllRemoteRepositories( @Parameter( description = "package type for the remotes", required = true,
                                                          example = "maven" )
                                              @QueryParam( "packageType" ) final String packageType )
    {
        return generateStoreListingResponse( () -> queryController.getAllRemoteRepositories( packageType ) );
    }

    @Operation( description = "Retrieve all hosted repository definitions by specified package type" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @GET
    @Path( "/allHosteds" )
    @Produces( APPLICATION_JSON )
    public Response getAllHostedRepositories(
            @Parameter( description = "package type for the hosted repos", required = true, example = "maven|npm" )
            @QueryParam( "packageType" ) final String packageType )
    {
        return generateStoreListingResponse( () -> queryController.getAllHostedRepositories( packageType ) );
    }

    @Operation( description = "Retrieve all group definitions by specified package type" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @GET
    @Path( "/allGroups" )
    @Produces( APPLICATION_JSON )
    public Response getAllGroups(
            @Parameter( description = "package type for the groups", required = true, example = "maven|npm" )
            @QueryParam( "packageType" ) final String packageType )
    {
        return generateStoreListingResponse( () -> queryController.getAllGroups( packageType ) );
    }

    @Operation( description = "Retrieve all default package types" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
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
    @APIResponse( responseCode = "404", description = "The store not found" )
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
            logger.error( e.getMessage(), e );
            return responseHelper.formatResponse( e );
        }
    }

    @Operation( description = "Retrieve the enabled groups whose constituents contains the specified store" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @GET
    @Path( "/groups/contains" )
    @Produces( APPLICATION_JSON )
    public Response getEnabledGroupsContaining(
            @Parameter( description = "Key of the repository contained in the groups", required = true,
                        example = "maven:remote:central" ) @QueryParam( "storeKey" ) final String storeKey )
    {
        return generateStoreListingResponse( () -> queryController.getEnabledGroupsContaining( storeKey ) );
    }

    @Operation( description = "Retrieve the enabled concrete stores which are constituents of the specified group" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The store definitions" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @GET
    @Path( "/concretes/inGroup" )
    @Produces( APPLICATION_JSON )
    public Response getOrderedConcreteEnabledStoresInGroup(
            @Parameter( description = "Key of the group whom the repositories are contained in", required = true,
                        example = "maven:group:public" ) @QueryParam( "storeKey" ) final String storeKey )
    {
        return generateStoreListingResponse( () -> queryController.getOrderedConcreteStoresInGroup( storeKey ) );
    }

    @Operation( description = "Retrieve the enabled stores which are constituents of the specified group" )
    @APIResponse( responseCode = "200",
                  content = @Content( schema = @Schema( implementation = StoreListingDTO.class ) ),
                  description = "The stores definitions, include the master group itself" )
    @APIResponse( responseCode = "404", description = "The store doesn't exist" )
    @GET
    @Path( "/inGroup" )
    @Produces( APPLICATION_JSON )
    public Response getOrderedEnabledStoresInGroup(
            @Parameter( description = "Key of the group whom the repositories are contained in", required = true,
                        example = "maven:group:public" ) @QueryParam( "storeKey" ) final String storeKey )
    {
        return generateStoreListingResponse( () -> queryController.getOrderedStoresInGroup( storeKey ) );
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
                        example = "maven:remote:central,maven:hosted:local" ) @QueryParam( "keys" ) final String keys )
    {

        return generateStoreListingResponse( () -> {
            if ( keys == null )
            {
                throw new IndyWorkflowException( BAD_REQUEST.getStatusCode(), "Illegal storeKeys: can not be null" );
            }
            String[] keysArr = keys.split( "," );
            if ( keys.length() == 0 )
            {
                throw new IndyWorkflowException( BAD_REQUEST.getStatusCode(), "Illegal storeKeys: can not be empty" );
            }
            return queryController.getGroupsAffectedBy( keysArr );
        } );
    }

    //    @GET
    //    @Produces( APPLICATION_JSON )
    //    public Response fitlerByKey()
    //    {
    //        TODO:
    //                queryController.getAll(filter);
    //        return null;
    //    }

    private Response generateStoreListingResponse( ArtifactStoreListSupplier supplier )
    {
        try
        {
            final List<? extends ArtifactStore> stores = supplier.get();
            logger.info( "Returning listing containing stores:\n\t{}", new JoinString( "\n\t", stores ) );
            if ( stores == null || stores.isEmpty() )
            {
                return Response.status( NOT_FOUND ).build();
            }
            final StoreListingDTO<ArtifactStore> dto = new StoreListingDTO( stores );
            return responseHelper.formatOkResponseWithJsonEntity( dto );
        }
        catch ( IndyWorkflowException e )
        {
            logger.error( e.getMessage(), e );
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