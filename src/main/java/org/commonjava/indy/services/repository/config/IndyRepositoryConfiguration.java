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
package org.commonjava.indy.services.repository.config;

import io.quarkus.arc.config.ConfigProperties;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@Startup
@ApplicationScoped
public class IndyRepositoryConfiguration
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @ConfigProperty( name = "repository.affectedGroupsExclude", defaultValue = " " )
    private String affectedGroupsExcludeFilter;

    @ConfigProperty( name = "repository.disposableStorePattern", defaultValue = " " )
    private String disposableStorePattern;

    @ConfigProperty(name="repository.storeValidationEnabled", defaultValue = "false")
    private Boolean storeValidation;

    public String getAffectedGroupsExcludeFilter()
    {
        return affectedGroupsExcludeFilter;
    }

    public void setAffectedGroupsExcludeFilter( String affectedGroupsExcludeFilter )
    {
        this.affectedGroupsExcludeFilter = affectedGroupsExcludeFilter;
    }

    public String getDisposableStorePattern()
    {
        return disposableStorePattern;
    }

    public void setDisposableStorePattern( String disposableStorePattern )
    {
        this.disposableStorePattern = disposableStorePattern;
    }

    public Boolean getStoreValidation()
    {
        return storeValidation;
    }

    public void setStoreValidation( Boolean storeValidation )
    {
        this.storeValidation = storeValidation;
    }
}
