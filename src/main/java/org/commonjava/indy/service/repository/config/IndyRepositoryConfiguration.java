/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy-repository-service)
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
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Startup
@ConfigMapping( prefix = "repository" )
@ApplicationScoped
public interface IndyRepositoryConfiguration
{
    String STORAGE_INFINISPAN = "infinispan";

    String STORAGE_CASSANDRA = "cassandra";

    @WithName( "affectedGroupsExclude" )
    Optional<String> affectedGroupsExcludeFilter();

    @WithName( "disposableStorePattern" )
    Optional<String> disposableStorePattern();

    @WithName( "data-storage" )
    Optional<String> storageType();

    @WithName( "query.cache.enabled" )
    @WithDefault( "false" )
    Boolean queryCacheEnabled();

    @WithName( "storeValidationEnabled" )
    @WithDefault( "false" )
    Boolean storeValidationEnabled();

    @WithName( "remote.sslRequired" )
    @WithDefault( "false" )
    Boolean sslRequired();

    @WithName( "remote.nosslHosts" )
    Optional<List<String>> remoteNoSSLHosts();
}
