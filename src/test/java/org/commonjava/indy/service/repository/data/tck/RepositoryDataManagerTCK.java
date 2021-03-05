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
package org.commonjava.indy.service.repository.data.tck;

import org.commonjava.event.common.EventMetadata;
import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class RepositoryDataManagerTCK
        extends AbstractProxyDataManagerTCK
{

    private StoreDataManager manager;

    @BeforeEach
    public void setup()
    {
        doSetup();
        seedRepositoriesForGroupTests();
    }

    protected void doSetup()
    {
    }

    protected void seedRepositoriesForGroupTests()
    {
        manager = getFixtureProvider().getDataManager();
    }

    @Test
    public void createAndRetrieveCentralRepoProxy()
            throws Exception
    {
        final StoreDataManager manager = getFixtureProvider().getDataManager();

        final RemoteRepository repo =
                new RemoteRepository( MAVEN_PKG_KEY, "central", "http://repo1.maven.apache.org/maven2/" );
        storeRemoteRepository( repo, false );

        final RemoteRepository result = manager.query().storeType( RemoteRepository.class ).getByName( repo.getName() );

        assertThat( result.getName(), equalTo( repo.getName() ) );
        assertThat( result.getUrl(), equalTo( repo.getUrl() ) );
        assertThat( result.getUser(), nullValue() );
        assertThat( result.getPassword(), nullValue() );
    }

    @Test
    public void createCentralRepoProxyTwiceAndRetrieveOne()
            throws Exception
    {
        final StoreDataManager manager = getFixtureProvider().getDataManager();

        final RemoteRepository repo =
                new RemoteRepository( MAVEN_PKG_KEY, "central", "http://repo1.maven.apache.org/maven2/" );
        storeRemoteRepository( repo, true );

        List<RemoteRepository> result = manager.query().getAllRemoteRepositories( MAVEN_PKG_KEY );

        assertThat( result, notNullValue() );
        assertThat( result.size(), equalTo( 1 ) );

        storeRemoteRepository( repo, true );

        result = manager.query().getAllRemoteRepositories( MAVEN_PKG_KEY );

        assertThat( result, notNullValue() );
        assertThat( result.size(), equalTo( 1 ) );
    }

    @Test
    public void createAndDeleteCentralRepoProxy()
            throws Exception
    {
        final StoreDataManager manager = getFixtureProvider().getDataManager();

        final RemoteRepository repo =
                new RemoteRepository( MAVEN_PKG_KEY, "central", "http://repo1.maven.apache.org/maven2/" );
        storeRemoteRepository( repo, false );

        manager.deleteArtifactStore( repo.getKey(), summary, new EventMetadata() );

        final ArtifactStore result = manager.query().getRemoteRepository( MAVEN_PKG_KEY, repo.getName() );

        assertThat( result, nullValue() );
    }

    @Test
    public void createTwoReposAndRetrieveAll()
            throws Exception
    {
        final StoreDataManager manager = getFixtureProvider().getDataManager();

        final RemoteRepository repo =
                new RemoteRepository( MAVEN_PKG_KEY, "central", "http://repo1.maven.apache.org/maven2/" );
        storeRemoteRepository( repo );

        final RemoteRepository repo2 = new RemoteRepository( MAVEN_PKG_KEY, "test", "http://www.google.com" );
        storeRemoteRepository( repo2 );

        final List<RemoteRepository> repositories = manager.query().getAllRemoteRepositories( MAVEN_PKG_KEY );

        assertThat( repositories, notNullValue() );
        assertThat( repositories.size(), equalTo( 2 ) );

        repositories.sort( Comparator.comparing( ArtifactStore::getName ) );

        ArtifactStore r = repositories.get( 0 );
        assertThat( r.getName(), equalTo( repo.getName() ) );

        r = repositories.get( 1 );
        assertThat( r.getName(), equalTo( repo2.getName() ) );
    }

    private void storeRemoteRepository( final RemoteRepository repo )
            throws Exception
    {
        manager.storeArtifactStore( repo, summary, false, false, new EventMetadata() );
    }

    private void storeRemoteRepository( final RemoteRepository repo, final boolean skipIfExists )
            throws Exception
    {
        manager.storeArtifactStore( repo, summary, skipIfExists, false, new EventMetadata() );
    }

}
