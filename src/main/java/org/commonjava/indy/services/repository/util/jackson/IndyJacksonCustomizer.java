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
package org.commonjava.indy.services.repository.util.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.jackson.ObjectMapperCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class IndyJacksonCustomizer
        implements ObjectMapperCustomizer
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private Instance<Module> injectedModules;

    @Inject
    private Instance<ModuleSet> injectedModuleSets;

    //    private final Iterable<Module> ctorModules;

    //    private final Iterable<ModuleSet> ctorModuleSets;

    private final Set<String> registeredModules = new HashSet<>();

    @PostConstruct
    public void init()
    {

        //        inject( injectedModules, injectedModuleSets );
        //        inject( ctorModules, ctorModuleSets );
    }

    public void customize( ObjectMapper mapper )
    {
        mapper.setSerializationInclusion( JsonInclude.Include.NON_EMPTY );
        mapper.configure( JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true );
        mapper.configure( DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true );

        mapper.enable( SerializationFeature.INDENT_OUTPUT, SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID );

        mapper.enable( MapperFeature.AUTO_DETECT_FIELDS );
        //        disable( MapperFeature.AUTO_DETECT_GETTERS );

        mapper.disable( SerializationFeature.WRITE_NULL_MAP_VALUES, SerializationFeature.WRITE_EMPTY_JSON_ARRAYS );

        mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );

        inject( mapper, injectedModules, injectedModuleSets );

    }

    private void inject( ObjectMapper mapper, Iterable<Module> modules, Iterable<ModuleSet> moduleSets )
    {
        Set<Module> injected = new HashSet<>();

        Logger logger = LoggerFactory.getLogger( getClass() );
        if ( modules != null )
        {
            for ( final Module module : modules )
            {
                injected.add( module );
            }
        }

        if ( moduleSets != null )
        {
            for ( ModuleSet moduleSet : moduleSets )
            {
                logger.trace( "Adding module-set to object mapper..." );

                Set<Module> set = moduleSet.getModules();
                if ( set != null )
                {
                    injected.addAll( set );
                }
            }
        }

        for ( Module module : injected )
        {
            injectSingle(mapper, module );
        }

    }

    private void injectSingle( ObjectMapper mapper, Module module )
    {
        Logger logger = LoggerFactory.getLogger( getClass() );
        logger.info( "Registering object-mapper module: {}", module );

        mapper.registerModule( module );
        registeredModules.add( module.getClass().getSimpleName() );

        if ( module instanceof IndySerializerModule )
        {
            ( (IndySerializerModule) module ).register( mapper );
        }
    }

    public Set<String> getRegisteredModuleNames()
    {
        return registeredModules;
    }

//    public String patchLegacyStoreJson( final String json )
//            throws IOException
//    {
//        final JsonNode tree = readTree( json );
//        logger.debug( "Patching JSON tree: {}", tree );
//
//        final JsonNode keyNode = tree.get( ArtifactStore.KEY_ATTR );
//        StoreKey key;
//        try
//        {
//            key = StoreKey.fromString( keyNode.textValue() );
//        }
//        catch ( IllegalArgumentException e )
//        {
//            throw new IndySerializationException(
//                    "Cannot patch store JSON. StoreKey 'key' attribute has invalid packageType (first segment)!", null,
//                    e );
//        }
//
//        boolean changed = false;
//        if ( !keyNode.textValue().equals( key.toString() ) )
//        {
//            logger.trace( "Patching key field in JSON for: {}", key );
//            ( (ObjectNode) tree ).put( ArtifactStore.KEY_ATTR, key.toString() );
//            changed = true;
//        }
//
//        JsonNode field = tree.get( ArtifactStore.TYPE_ATTR );
//        if ( field == null )
//        {
//            logger.trace( "Patching type field in JSON for: {}", key );
//            ( (ObjectNode) tree ).put( ArtifactStore.TYPE_ATTR, key.getType().singularEndpointName() );
//            changed = true;
//        }
//
//        field = tree.get( ArtifactStore.PKG_TYPE_ATTR );
//        if ( field == null )
//        {
//            logger.trace( "Patching packageType field in JSON for: {}", key );
//            ( (ObjectNode) tree ).put( ArtifactStore.PKG_TYPE_ATTR, key.getPackageType() );
//            changed = true;
//        }
//
//        if ( changed )
//        {
//            String patched = writeValueAsString( tree );
//            logger.trace( "PATCHED store definition:\n\n{}\n\n", patched );
//            return patched;
//        }
//
//        return json;
//    }
}
