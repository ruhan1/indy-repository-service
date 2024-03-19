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
package org.commonjava.indy.service.repository.controller;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commonjava.event.common.EventMetadata;
import org.commonjava.indy.service.repository.audit.ChangeSummary;
import org.commonjava.indy.service.repository.data.StoreDataManager;
import org.commonjava.indy.service.repository.exception.IndyDataException;
import org.commonjava.indy.service.repository.model.ArtifactStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Map.of;
import static org.apache.commons.io.IOUtils.toInputStream;

@ApplicationScoped
public class MaintenanceController
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    public static final String REPOS_DIR = "repos";

    @Inject
    StoreDataManager storeDataManager;

    @Inject
    ObjectMapper serializer;

    public File getRepoBundle()
            throws IOException
    {
        File out = createTempFile();
        logger.info( "Writing repo bundle to: '{}'", out );

        try (ZipOutputStream zip = new ZipOutputStream( new FileOutputStream( out ) ))
        {
            final Set<ArtifactStore> stores;
            try
            {
                stores = storeDataManager.getAllArtifactStores();
            }
            catch ( IndyDataException e )
            {
                logger.error( "Failed to get stores definition", e );
                throw new IOException( e );
            }

            for ( ArtifactStore store : stores )
            {
                String path = Paths.get( REPOS_DIR, store.getPackageType(), store.getType().singularEndpointName(),
                                         store.getName() ).toString() + ".json";
                logger.debug( "Adding {} to repo zip", path );
                zip.putNextEntry( new ZipEntry( path ) );
                String json = serializer.writeValueAsString( store );
                IOUtils.copy( toInputStream( json, Charset.defaultCharset() ), zip );
            }
        }
        return out;
    }

    public Map<String, List<String>> importRepoBundle( final InputStream zipStream )
            throws IOException
    {
        File tempRepoZip = createTempFile();
        logger.info( "Saving repo file to {}", tempRepoZip.getPath() );
        try (zipStream)
        {
            try (OutputStream out = new FileOutputStream( tempRepoZip ))
            {
                IOUtils.copy( zipStream, out );
            }
        }

        final List<String> skipped = new ArrayList<>();
        final List<String> failed = new ArrayList<>();
        final Map<String, String> payload = new HashMap<>();

        try (ZipFile zipFile = ZipFile.builder().setFile( tempRepoZip ).get())
        {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while ( entries.hasMoreElements() )
            {
                ZipArchiveEntry entry = entries.nextElement();
                if ( !entry.isDirectory() )
                {
                    try (InputStream in = zipFile.getInputStream( entry ))
                    {
                        payload.put( entry.getName(), IOUtils.toString( in, Charset.defaultCharset() ) );
                    }
                }
            }
        }
        finally
        {
            FileUtils.deleteQuietly( tempRepoZip );
        }

        logger.info( "Repos definitions extraction from bundle finished.\n\n" );
        logger.info( "Start importing repos definitions to data store." );
        for ( Map.Entry<String, String> entry : payload.entrySet() )
        {
            try
            {
                ArtifactStore store = serializer.readerFor( ArtifactStore.class )
                                                .with( JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS )
                                                .readValue( entry.getValue() );
                if ( storeDataManager.hasArtifactStore( store.getKey() ) )
                {
                    skipped.add( entry.getKey() );
                }
                else
                {
                    storeDataManager.storeArtifactStore( store, new ChangeSummary( ChangeSummary.SYSTEM_USER,
                                                                                   "Import artifact store" ), true,
                                                         false, new EventMetadata() );
                }
            }
            catch ( Exception e )
            {
                logger.warn( "Cannot persist store definition for {}, Reason: {} : {}", entry.getKey(),
                             e.getClass().getName(), e.getMessage() );
                failed.add( entry.getKey() );
            }
        }
        logger.info( "Repos definitions importing finished.\n\n" );

        logger.info( "Repository importing process done. result as below:\n skipped: {}\n\n failed: {}\n\n", skipped,
                     failed );

        return of( "skipped", skipped, "failed", failed );
    }

    private File createTempFile()
            throws IOException
    {
        return File.createTempFile(
                "indy-repos." + new SimpleDateFormat( "yyyy-MM-dd-hh-mm-ss.SSSZ" ).format( new Date() ), ".zip" );

    }
}
