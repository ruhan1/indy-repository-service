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
import java.util.List;

@Startup
@ApplicationScoped
public class SslValidationConfiguration
{
    @ConfigProperty( name = "repository.remote.sslRequired", defaultValue = "false" )
    private Boolean sslRequired;

    @ConfigProperty( name = "repository.remote.nosslHosts", defaultValue = " " )
    private List<String> remoteNoSSLHosts;

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
        return remoteNoSSLHosts;
    }

    public void setRemoteNoSSLHosts( List<String> remoteNoSSLHosts )
    {
        this.remoteNoSSLHosts = remoteNoSSLHosts;
    }
}
