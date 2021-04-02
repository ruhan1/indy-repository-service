/**
 * Copyright (C) 2011-2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
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
package org.commonjava.indy.service.repository.controller;

import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.exception.IndyDataException;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.dto.EndpointView;
import org.commonjava.indy.service.repository.model.dto.EndpointViewListing;
import org.commonjava.indy.service.repository.model.version.Versioning;
import org.commonjava.indy.service.repository.util.JaxRsUriFormatter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@ApplicationScoped
public class StatsController
{

    private static final String ADDONS_LOGIC = "addonsLogic";

    @Inject
    Versioning versioning;

    @Inject
    StoreDataManager dataManager;

    protected StatsController()
    {
    }

    public StatsController( final StoreDataManager dataManager, final Versioning versioning )
    {
        this.dataManager = dataManager;
        this.versioning = versioning;
    }

    @PostConstruct
    public void init()
    {
    }

    public Versioning getVersionInfo()
    {
        return versioning;
    }

    public EndpointViewListing getEndpointsListing( final String baseUri, final JaxRsUriFormatter uriFormatter )
            throws IndyWorkflowException
    {
        final List<ArtifactStore> stores = new ArrayList<>();
        try
        {
            stores.addAll( dataManager.getAllArtifactStores() );
        }
        catch ( final IndyDataException e )
        {
            throw new IndyWorkflowException( INTERNAL_SERVER_ERROR.getStatusCode(),
                                             "Failed to retrieve all endpoints: {}", e, e.getMessage() );
        }

        final List<EndpointView> points = new ArrayList<>();
        for ( final ArtifactStore store : stores )
        {
            final StoreKey key = store.getKey();
            final String resourceUri = uriFormatter.formatAbsolutePathTo( baseUri, "content", key.getPackageType(),
                                                                          key.getType().singularEndpointName(),
                                                                          key.getName() );

            final EndpointView point = new EndpointView( store, resourceUri );
            if ( !points.contains( point ) )
            {
                points.add( point );
            }
        }

        return new EndpointViewListing( points );
    }

}
