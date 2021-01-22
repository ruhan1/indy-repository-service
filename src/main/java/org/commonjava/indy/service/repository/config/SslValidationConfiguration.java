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
package org.commonjava.indy.service.repository.config;

import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Startup
@ApplicationScoped
public class SslValidationConfiguration
{
    @Inject
    @ConfigProperty( name = "repository.remote.sslRequired", defaultValue = "false" )
    private Boolean sslRequired;

    @Inject
    @ConfigProperty( name = "repository.remote.nosslHosts" )
    private Optional<List<String>> remoteNoSSLHosts;

//    @PostConstruct
//    public void testProperties()
//    {
//        final Logger logger = LoggerFactory.getLogger( this.getClass() );
//        logger.info( "remoteNoSSLHosts: {}", getRemoteNoSSLHosts() );
//    }

    public Boolean isSSLRequired()
    {
        return sslRequired;
    }

    public void setSslRequired( Boolean sslRequired )
    {
        this.sslRequired = sslRequired;
    }

    public List<String> getRemoteNoSSLHosts()
    {
        return remoteNoSSLHosts.orElse( Collections.emptyList() );
    }

    public void setRemoteNoSSLHosts( List<String> remoteNoSSLHosts )
    {
        this.remoteNoSSLHosts = Optional.of( remoteNoSSLHosts );
    }
}
