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
package org.commonjava.indy.service.repository.util.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MapperUtil
{
    private static final Logger logger = LoggerFactory.getLogger( MapperUtil.class );

    public static String patchLegacyStoreJson( ObjectMapper mapper, final String json )
            throws IOException
    {
        final JsonNode tree = mapper.readTree( json );
        logger.debug( "Patching JSON tree: {}", tree );

        final JsonNode keyNode = tree.get( ArtifactStore.KEY_ATTR );
        StoreKey key;
        try
        {
            key = StoreKey.fromString( keyNode.textValue() );
        }
        catch ( IllegalArgumentException e )
        {
            throw new IndySerializationException(
                    "Cannot patch store JSON. StoreKey 'key' attribute has invalid packageType (first segment)!", null,
                    e );
        }

        boolean changed = false;
        if ( !keyNode.textValue().equals( key.toString() ) )
        {
            logger.trace( "Patching key field in JSON for: {}", key );
            ( (ObjectNode) tree ).put( ArtifactStore.KEY_ATTR, key.toString() );
            changed = true;
        }

        JsonNode field = tree.get( ArtifactStore.TYPE_ATTR );
        if ( field == null )
        {
            logger.trace( "Patching type field in JSON for: {}", key );
            ( (ObjectNode) tree ).put( ArtifactStore.TYPE_ATTR, key.getType().singularEndpointName() );
            changed = true;
        }

        field = tree.get( ArtifactStore.PKG_TYPE_ATTR );
        if ( field == null )
        {
            logger.trace( "Patching packageType field in JSON for: {}", key );
            ( (ObjectNode) tree ).put( ArtifactStore.PKG_TYPE_ATTR, key.getPackageType() );
            changed = true;
        }

        if ( changed )
        {
            String patched = mapper.writeValueAsString( tree );
            logger.trace( "PATCHED store definition:\n\n{}\n\n", patched );
            return patched;
        }

        return json;
    }
}
