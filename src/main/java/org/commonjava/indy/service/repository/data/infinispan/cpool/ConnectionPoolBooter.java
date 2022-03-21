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
package org.commonjava.indy.service.repository.data.infinispan.cpool;

import io.quarkus.runtime.Startup;
import org.commonjava.indy.service.repository.config.IndyRepositoryConfiguration;
import org.commonjava.indy.service.repository.exception.IndyLifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
@Startup
public class ConnectionPoolBooter
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    Instance<ConnectionPoolProvider> connectionPoolProvider;

    @Inject
    IndyRepositoryConfiguration repositoryConfiguration;

    @PostConstruct
    public void init()
            throws IndyLifecycleException
    {
        if ( IndyRepositoryConfiguration.STORAGE_INFINISPAN.equals( repositoryConfiguration.getStorageType() ) )
        {
            logger.info( "\n\n\n\nStarting JNDI Connection Pools\n\n\n\n" );
            connectionPoolProvider.get().init();
            logger.info( "Connection pools started." );
        }
        else
        {
            logger.info(
                    "Connection pool will not initialize because not using infinispan as data store for repository data, " );
        }
    }

    //    @Override
    //    public int getBootPriority()
    //    {
    //        return 100;
    //    }

    //    @Override
    //    public String getId()
    //    {
    //        return "JNDI Connection Pools";
    //    }
}
