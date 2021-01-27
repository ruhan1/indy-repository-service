/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.repository.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.indy.service.repository.controller.AdminController;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.dto.StoreListingDTO;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Tag( name = "Store Querying APIs", description = "Resource for querying artifact store definitions" )
@Path( "/stores/query" )
@ApplicationScoped
public class RepositoryQueryResources
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private AdminController adminController;

    @Inject
    private ResponseHelper responseHelper;

    public RepositoryQueryResources()
    {
        logger.info( "\n\n\n\nStarted Store Querying resources\n\n\n\n" );
    }

    @Path( "/all" )
    @GET
    @Produces( APPLICATION_JSON )
    public Response getAll()
    {

        Response response;
        try
        {
            final List<ArtifactStore> stores = adminController.getAllStores();

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

}