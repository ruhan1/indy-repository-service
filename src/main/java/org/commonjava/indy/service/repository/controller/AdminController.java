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
package org.commonjava.indy.service.repository.controller;

import org.apache.commons.lang3.StringUtils;
import org.commonjava.event.common.EventMetadata;
import org.commonjava.indy.service.repository.audit.ChangeSummary;
import org.commonjava.indy.service.repository.client.storage.StorageService;
import org.commonjava.indy.service.repository.config.IndyRepositoryConfiguration;
import org.commonjava.indy.service.repository.data.ArtifactStoreQuery;
import org.commonjava.indy.service.repository.data.ArtifactStoreValidateData;
import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.data.StoreValidator;
import org.commonjava.indy.service.repository.exception.IndyDataException;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.exception.InvalidArtifactStoreException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@ApplicationScoped
public class AdminController
{
    public static final String ALL_PACKAGE_TYPES = "_all";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    StoreDataManager storeManager;

    @Inject
    IndyRepositoryConfiguration indyConfiguration;

    @Inject
    StoreValidator storeValidator;

    @Inject
    @RestClient
    StorageService storageService;

    protected AdminController()
    {
    }

    public AdminController( final StoreDataManager storeManager )
    {
        this.storeManager = storeManager;
    }

    public boolean store( final ArtifactStore store, final String user, final boolean skipExisting )
            throws IndyWorkflowException
    {
        try
        {
            String changelog = store.getMetadata( ArtifactStore.METADATA_CHANGELOG );
            if ( changelog == null )
            {
                changelog = "Changelog not provided";
            }

            final ChangeSummary summary = new ChangeSummary( user, changelog );

            logger.info( "Persisting artifact store: {} using: {}", store, storeManager );
            return storeManager.storeArtifactStore( store, summary, skipExisting, true, new EventMetadata() );
        }
        catch ( final IndyDataException e )
        {
            throw new IndyWorkflowException( INTERNAL_SERVER_ERROR.getStatusCode(), "Failed to store: {}. Reason: {}",
                                             e, store.getKey(), e.getMessage() );
        }
    }

    public List<ArtifactStore> getAllOfType( final StoreType type )
            throws IndyWorkflowException
    {
        try
        {
            return storeManager.query().noPackageType().storeTypes( type ).getAll();
        }
        catch ( final IndyDataException e )
        {
            throw new IndyWorkflowException( INTERNAL_SERVER_ERROR.getStatusCode(), "Failed to list: {}. Reason: {}", e,
                                             type, e.getMessage() );
        }
    }

    public List<ArtifactStore> getAllOfType( final String packageType, final StoreType type )
            throws IndyWorkflowException
    {
        try
        {
            ArtifactStoreQuery<ArtifactStore> query = storeManager.query().storeTypes( type );
            if ( !ALL_PACKAGE_TYPES.equals( packageType ) )
            {
                return new ArrayList<>( storeManager.getArtifactStoresByPkgAndType( packageType, type ) );
            }
            else
            {
                return query.getAllByDefaultPackageTypes();
            }
        }
        catch ( final IndyDataException e )
        {
            throw new IndyWorkflowException( INTERNAL_SERVER_ERROR.getStatusCode(), "Failed to list: {}. Reason: {}", e,
                                             type, e.getMessage() );
        }
    }

    public List<ArtifactStore> getAllStores()
            throws IndyWorkflowException
    {
        try
        {
            return new ArrayList<>( storeManager.getAllArtifactStores() );
        }
        catch ( IndyDataException e )
        {
            throw new IndyWorkflowException( INTERNAL_SERVER_ERROR.getStatusCode(),
                                             "Failed to list all stores. Reason: {}", e, e.getMessage() );
        }
    }

    public ArtifactStore get( final StoreKey key )
            throws IndyWorkflowException
    {
        try
        {
            return storeManager.getArtifactStore( key ).orElse( null );
        }
        catch ( final IndyDataException e )
        {
            throw new IndyWorkflowException( INTERNAL_SERVER_ERROR.getStatusCode(),
                                             "Failed to retrieve: {}. Reason: {}", e, key, e.getMessage() );
        }
    }

    public List<RemoteRepository> getRemoteByUrl( final String url, final String packageType )
            throws IndyWorkflowException
    {
        try
        {
            return storeManager.query().getRemoteRepositoryByUrl( packageType, url );
        }
        catch ( IndyDataException e )
        {
            throw new IndyWorkflowException( INTERNAL_SERVER_ERROR.getStatusCode(),
                                             "Failed to retrieve remote by url: {}. Reason: {}", e, url,
                                             e.getMessage() );
        }
    }

    public void delete( final StoreKey key, final String user, final String changelog, final boolean deleteContent )
            throws IndyWorkflowException
    {
        // safe check
        if ( deleteContent )
        {
            final String disposablePattern = indyConfiguration.disposableStorePattern().orElse( "" );
            if ( StringUtils.isNotBlank( disposablePattern ) && !key.getName().matches( disposablePattern ) )
            {
                throw new IndyWorkflowException( FORBIDDEN.getStatusCode(), "Content deletion not allowed" );
            }
        }

        try
        {
            ArtifactStore store = storeManager.getArtifactStore( key ).orElse( null );
            if ( store != null && deleteContent )
            {
                logger.info( "Delete content of {}", key );
                purgeFilesystem( key.toString() );
            }
            storeManager.deleteArtifactStore( key, new ChangeSummary( user, changelog ),
                    new EventMetadata().set( "deleteContent", deleteContent ) );
        }
        catch ( final IndyDataException e )
        {
            int status = INTERNAL_SERVER_ERROR.getStatusCode();
            if ( e.getStatus() > 0 )
            {
                status = e.getStatus();
            }
            throw new IndyWorkflowException( status, "Failed to delete: {}. Reason: {}", e, key, e.getMessage() );
        }
    }

    private void purgeFilesystem(String filesystem)
    {
        try( Response resp = storageService.purge(filesystem) )
        {
            logger.info( "Purge filesystem done, code: {}, result: {}", resp.getStatus(), resp.getEntity() );
        }
        catch ( Exception e )
        {
            logger.warn( "Purge filesystem failed", e );
        }
    }

    public boolean exists( final StoreKey key )
    {
        return storeManager.hasArtifactStore( key );
    }

    public ArtifactStoreValidateData validateStore( ArtifactStore artifactStore )
    {
        return storeValidator.validate( artifactStore );
    }

    public List<ArtifactStore> getDisabledRemoteRepositories()
    {
        ArrayList<ArtifactStore> disabledArtifactStores = new ArrayList<>();
        try
        {
            List<ArtifactStore> allRepositories = storeManager.query().getAll();
            for ( ArtifactStore as : allRepositories )
            {
                if ( as.getType() == StoreType.remote && as.isDisabled() )
                {
                    disabledArtifactStores.add( as );
                }
            }
            return disabledArtifactStores;
        }
        catch ( IndyDataException e )
        {
            logger.error( e.getMessage() );
        }
        return disabledArtifactStores;
    }

}
