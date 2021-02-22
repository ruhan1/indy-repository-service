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
package org.commonjava.indy.service.repository.jaxrs.mock;

import org.commonjava.indy.service.repository.controller.AdminController;
import org.commonjava.indy.service.repository.exception.IndyWorkflowException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.GenericPackageTypeDescriptor;
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

@ApplicationScoped
@Alternative
@Priority( 1 )
public class MockAdminController
        extends AdminController
{
    @Override
    public boolean exists( StoreKey key )
    {
        return "maven:remote:exists".equals( key.toString() );
    }

    @Override
    public List<ArtifactStore> getAllOfType( final String packageType, final StoreType type )
            throws IndyWorkflowException
    {
        if ( MAVEN_PKG_KEY.equals( packageType ) )
        {
            if ( type == StoreType.remote )
            {
                RemoteRepository repo1 = new RemoteRepository( MAVEN_PKG_KEY, "test1", "http://repo.test1" );
                RemoteRepository repo2 = new RemoteRepository( MAVEN_PKG_KEY, "test2", "http://repo.test2" );
                return Arrays.asList( repo1, repo2 );
            }
            if ( type == StoreType.hosted )
            {
                return Collections.emptyList();
            }
        }
        throw new IndyWorkflowException( Response.Status.NOT_FOUND.getStatusCode(), "Not found" );
    }

    @Override
    public ArtifactStore get( final StoreKey key )
    {
        if ( StoreKey.fromString( "maven:remote:exists" ).equals( key ) )
        {
            return new RemoteRepository( MAVEN_PKG_KEY, "exists", "http://repo.test1" );
        }

        return null;
    }

    @Override
    public List<RemoteRepository> getRemoteByUrl( final String url, final String packageType )
            throws IndyWorkflowException
    {
        if ( MAVEN_PKG_KEY.equals( packageType ) )
        {
            if ( "http://repo.test".equals( url ) )
            {
                return Arrays.asList( new RemoteRepository( MAVEN_PKG_KEY, "exists1", "http://repo.test" ),
                                      new RemoteRepository( MAVEN_PKG_KEY, "exists2", "http://repo.test" ) );
            }
        }
        if ( GenericPackageTypeDescriptor.GENERIC_PKG_KEY.equals( packageType ) )
        {
            throw new IndyWorkflowException( "Not support generic" );
        }

        return Collections.emptyList();
    }

    @Override
    public boolean store( final ArtifactStore store, final String user, final boolean skipExisting )
            throws IndyWorkflowException
    {
        if ( store.getKey().equals( StoreKey.fromString( "maven:remote:success" ) ) )
        {
            RemoteRepository repo = (RemoteRepository) store;
            return repo.getUrl().equals( "http://repo.success" );
        }
        if ( store.getKey().equals( StoreKey.fromString( "maven:remote:error" ) ) )
        {
            throw new IndyWorkflowException( "error happened" );
        }

        return false;
    }

    @Override
    public void delete( final StoreKey key, final String user, final String changelog, final boolean deleteContent )
            throws IndyWorkflowException
    {
        if ( key.equals( StoreKey.fromString( "maven:remote:error" ) ) )
        {
            throw new IndyWorkflowException( "error happened" );
        }

    }

    @Override
    public List<ArtifactStore> getDisabledRemoteRepositories()
    {
        RemoteRepository repo1 = new RemoteRepository( MAVEN_PKG_KEY, "test1", "http://repo.test1" );
        RemoteRepository repo2 = new RemoteRepository( MAVEN_PKG_KEY, "test2", "http://repo.test2" );
        return Arrays.asList( repo1, repo2 );
    }

    @Test
    public List<ArtifactStore> getAllStores()
    {
        RemoteRepository repo1 = new RemoteRepository( MAVEN_PKG_KEY, "test1", "http://repo.test1" );
        HostedRepository repo2 = new HostedRepository( MAVEN_PKG_KEY, "test2" );
        Group group = new Group( MAVEN_PKG_KEY, "test3", repo1.getKey(), repo2.getKey() );
        return Arrays.asList( repo1, repo2, group );
    }
}
