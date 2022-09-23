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
package org.commonjava.indy.service.repository.data;

import io.quarkus.runtime.Startup;
import org.commonjava.event.common.EventMetadata;
import org.commonjava.indy.service.repository.audit.ChangeSummary;
import org.commonjava.indy.service.repository.config.IndyRepositoryConfiguration;
import org.commonjava.indy.service.repository.data.cassandra.CassandraStoreDataManager;
import org.commonjava.indy.service.repository.exception.IndyDataException;
import org.commonjava.indy.service.repository.exception.IndyLifecycleException;
import org.commonjava.indy.service.repository.model.Group;
import org.commonjava.indy.service.repository.model.HostedRepository;
import org.commonjava.indy.service.repository.model.RemoteRepository;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.commonjava.indy.service.repository.model.StoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

@ApplicationScoped
@Startup
public class StoreDataSetupAction
{
    private static final String DEFAULT_SETUP = "default-setup";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    StoreDataManager storeManager;

    @Inject
    IndyRepositoryConfiguration repoConfig;

    @PostConstruct
    public void start()
            throws IndyLifecycleException
    {
        final ChangeSummary summary = new ChangeSummary( ChangeSummary.SYSTEM_USER, "Initializing default data." );

        if ( !repoConfig.queryCacheEnabled() )
        {
            logger.info(
                    "The query cache is not enabled, all query result will be directly retrieved from underlying data store." );
        }
        else
        {
            logger.info(
                    "The query cache is enabled, some query results will be cached for performance consideration." );
        }

        try
        {
            logger.info( "Verfiying that Indy basic stores are installed..." );
            storeManager.install();

            if ( storeManager instanceof CassandraStoreDataManager )
            {
                logger.info( "Init the cache of remote stores based on the store data" );

                ( (CassandraStoreDataManager) storeManager ).initRemoteStoresCache();
            }

            if ( storeManager.query().getRemoteRepository( MAVEN_PKG_KEY, "central" ) == null )
            {
                final RemoteRepository central =
                        new RemoteRepository( MAVEN_PKG_KEY, "central", "https://repo.maven.apache.org/maven2/" );
                central.setCacheTimeoutSeconds( 86400 );
                storeManager.storeArtifactStore( central, summary, true, false,
                                                 new EventMetadata().set( StoreDataManager.EVENT_ORIGIN,
                                                                          DEFAULT_SETUP ) );

            }

            if ( storeManager.query().getHostedRepository( MAVEN_PKG_KEY, "local-deployments" ) == null )
            {
                final HostedRepository local = new HostedRepository( MAVEN_PKG_KEY, "local-deployments" );
                local.setAllowReleases( true );
                local.setAllowSnapshots( true );
                local.setSnapshotTimeoutSeconds( 86400 );

                storeManager.storeArtifactStore( local, summary, true, false,
                                                 new EventMetadata().set( StoreDataManager.EVENT_ORIGIN,
                                                                          DEFAULT_SETUP ) );
            }

            if ( storeManager.query().getGroup( MAVEN_PKG_KEY, "public" ) == null )
            {
                final Group pub = new Group( MAVEN_PKG_KEY, "public" );
                pub.addConstituent( new StoreKey( MAVEN_PKG_KEY, StoreType.remote, "central" ) );
                pub.addConstituent( new StoreKey( MAVEN_PKG_KEY, StoreType.hosted, "local-deployments" ) );

                storeManager.storeArtifactStore( pub, summary, true, false,
                                                 new EventMetadata().set( StoreDataManager.EVENT_ORIGIN,
                                                                          DEFAULT_SETUP ) );
            }
        }
        catch ( final IndyDataException e )
        {
            throw new RuntimeException( "Failed to boot indy components: " + e.getMessage(), e );
        }
    }

    //    public String getId()
    //    {
    //        return "Default artifact store initialization";
    //    }

    //    public int getStartupPriority()
    //    {
    //        return 0;
    //    }
}
