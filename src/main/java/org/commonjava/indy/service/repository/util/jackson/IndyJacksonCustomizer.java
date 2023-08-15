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
package org.commonjava.indy.service.repository.util.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonInclude.Value.construct;

/**
 * This customizer is used to init the jackson object mapper with indy customized features and modules
 */
@Singleton
public class IndyJacksonCustomizer
        implements ObjectMapperCustomizer
{
    @Inject
    Instance<Module> injectedModules;

    @Override
    public void customize( ObjectMapper mapper )
    {
        mapper.setSerializationInclusion( JsonInclude.Include.NON_EMPTY );
        mapper.configure( JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true );
        mapper.configure( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true );

        mapper.enable( SerializationFeature.INDENT_OUTPUT, SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID );
        mapper.enable( MapperFeature.AUTO_DETECT_FIELDS );

        mapper.disable( SerializationFeature.WRITE_EMPTY_JSON_ARRAYS );
        mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );

        // This is used to replace builder.disable(SerializationFeature.WRITE_NULL_MAP_VALUES) for deprecation reason
        mapper.configOverride( Map.class ).setInclude( construct( NON_NULL, NON_NULL ) );

        injectSingle( mapper, new RepoApiSerializerModule() );

        inject( mapper, injectedModules );

    }

    private void inject( ObjectMapper mapper, Iterable<Module> modules )
    {
        Set<Module> injected = new HashSet<>();

        if ( modules != null )
        {
            for ( final Module module : modules )
            {
                injected.add( module );
            }
        }

        for ( Module module : injected )
        {
            injectSingle( mapper, module );
        }

    }

    private void injectSingle( ObjectMapper mapper, Module module )
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Registering object-mapper module: {}", module );

        mapper.registerModule( module );
    }

}
