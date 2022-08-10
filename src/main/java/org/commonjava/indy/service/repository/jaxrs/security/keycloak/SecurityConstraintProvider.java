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
package org.commonjava.indy.service.repository.jaxrs.security.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.commonjava.indy.service.repository.config.KeycloakConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

@ApplicationScoped
public class SecurityConstraintProvider
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String DEFAULT_SECURITY_BINDING_YAML = "security-bindings.yaml";

    @Inject
    KeycloakConfiguration config;

    private KeycloakSecurityBindings constraintSet;

    protected SecurityConstraintProvider()
    {
    }

    public SecurityConstraintProvider( final KeycloakConfiguration config )
    {
        this.config = config;
        init();
    }

    @PostConstruct
    public void init()
    {
        if ( config.enabled() )
        {
            InputStream input = null;
            if ( config.securityBindingsYaml().isPresent() )
            {
                final File constraintFile = new File( config.securityBindingsYaml().get() );
                if ( !constraintFile.exists() )
                {
                    logger.warn( "Cannot load keycloak security constraints: {}, will try to load from classpath.",
                                 constraintFile );
                }
                else
                {
                    logger.info( "Will parse security bindings from: {}.", constraintFile );
                    try
                    {
                        input = new FileInputStream( constraintFile );
                    }
                    catch ( FileNotFoundException e )
                    {
                        logger.warn( "Cannot load keycloak security constraints: {}, will try to load from classpath.",
                                     constraintFile );
                    }
                }

            }

            if ( input == null )
            {
                logger.info( "Load security bindings config from classpath resource, {}",
                             DEFAULT_SECURITY_BINDING_YAML );
                input = this.getClass().getClassLoader().getResourceAsStream( DEFAULT_SECURITY_BINDING_YAML );
            }

            if ( input == null )
            {
                logger.error( "Cannot load keycloak security constraints because missing " );
                return;
            }

            try
            {
                parseBindings( input );
            }
            catch ( IOException e )
            {
                logger.error( "Cannot load keycloak security constraints due to error: {}", e.getMessage() );
            }

        }
        else
        {
            logger.info( "Keycloak is not enabled, so bypassed all keycloak configurations." );
        }
    }

    private void parseBindings( InputStream input )
            throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper( new YAMLFactory() );
        mapper.findAndRegisterModules();
        constraintSet = mapper.readValue( input, KeycloakSecurityBindings.class );
        if ( constraintSet != null )
        {
            constraintSet.setConstraints( constraintSet.getConstraints().stream().sorted( ( c1, c2 ) -> {
                final String[] arg1 = c1.getUrlPattern().split( "/" );
                final String[] arg2 = c2.getUrlPattern().split( "/" );
                return arg2.length - arg1.length;
            } ).collect( Collectors.toList() ) );
        }
    }

    @Produces
    public KeycloakSecurityBindings getConstraints()
    {
        return constraintSet;
    }

}
