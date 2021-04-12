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
package org.commonjava.indy.service.repository.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

public final class StoreDiffer
{
    //TODO: This differ is a little heavy weight and hard to extend. Thinking about to refactor with observable pattern in future.

    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    private static final StoreDiffer instance = new StoreDiffer();

    private StoreDiffer()
    {
    }

    public static StoreDiffer instance()
    {
        return instance;
    }

    public Map<String, List<Object>> diffArtifactStores( final ArtifactStore store, final ArtifactStore original )
    {
        if ( store == null )
        {
            logger.warn( "Error for updated store event: changed store should not be null!" );
            return emptyMap();
        }
        if ( original == null )
        {
            return emptyMap();
        }
        final Map<String, List<Object>> result = new HashMap<>();
        if ( isDiff( store.getDescription(), original.getDescription() ) )
        {
            result.put( "description", asList( store.getDescription(), original.getDescription() ) );
        }
        if ( isDiff( store.getCreateTime(), original.getCreateTime() ) )
        {
            result.put( "create_time", asList( store.getCreateTime(), original.getCreateTime() ) );
        }
        if ( isDiff( store.getName(), original.getName() ) )
        {
            result.put( "name", asList( store.getName(), original.getName() ) );
        }
        if ( isDiff( store.getPathStyle(), original.getPathStyle() ) )
        {
            result.put( "path_style", asList( store.getPathStyle(), original.getPathStyle() ) );
        }
        if ( store.getDisableTimeout() != original.getDisableTimeout() )
        {
            result.put( "disable_timeout", asList( store.getDisableTimeout(), original.getDisableTimeout() ) );
        }
        if ( isDiff( store.getPathMaskPatterns(), original.getPathMaskPatterns() ) )
        {
            result.put( "path_mask_patterns", asList( store.getPathMaskPatterns(), original.getPathMaskPatterns() ) );
        }
        if ( store.isAuthoritativeIndex() != original.isAuthoritativeIndex() )
        {
            result.put( "authoritative_index",
                        asList( store.isAuthoritativeIndex(), original.isAuthoritativeIndex() ) );
        }
        if ( isDiff( store.getMetadata(), original.getMetadata() ) )
        {
            result.put( "metadata", asList( store.getMetadata(), original.getMetadata() ) );
        }

        switch ( store.getKey().getType() )
        {
            case remote:
                Map<String, List<Object>> m = diffRemote( (RemoteRepository) store, (RemoteRepository) original );
                result.putAll( m );
                break;
            case hosted:
                m = diffHosted( (HostedRepository) store, (HostedRepository) original );
                result.putAll( m );
                break;
            case group:
                m = diffGroup( (Group) store, (Group) original );
                result.putAll( m );
                break;
        }

        return result;
    }

    private boolean isDiff( final Object current, final Object original )
    {
        if ( current == null && original == null )
        {
            return false;
        }
        return current == null || !current.equals( original );
    }

    private Map<String, List<Object>> diffRemote( final RemoteRepository store, final RemoteRepository original )
    {
        final Map<String, List<Object>> result = new HashMap<>();
        if ( store.isAllowReleases() != original.isAllowReleases() )
        {
            result.put( "allow_releases", asList( store.isAllowReleases(), original.isAllowReleases() ) );
        }
        if ( store.isAllowSnapshots() != original.isAllowSnapshots() )
        {
            result.put( "allow_snapshots", asList( store.isAllowSnapshots(), original.isAllowSnapshots() ) );
        }
        if ( store.getNfcTimeoutSeconds() != original.getNfcTimeoutSeconds() )
        {
            result.put( "nfc_timeout_seconds",
                        asList( store.getNfcTimeoutSeconds(), original.getNfcTimeoutSeconds() ) );
        }
        if ( store.getMaxConnections() != original.getMaxConnections() )
        {
            result.put( "max_connections", asList( store.getMaxConnections(), original.getMaxConnections() ) );
        }
        if ( store.isIgnoreHostnameVerification() != original.isIgnoreHostnameVerification() )
        {
            result.put( "ignore_hostname_verification",
                        asList( store.isIgnoreHostnameVerification(), original.isIgnoreHostnameVerification() ) );
        }
        if ( store.getCacheTimeoutSeconds() != original.getCacheTimeoutSeconds() )
        {
            result.put( "cache_timeout_seconds",
                        asList( store.getCacheTimeoutSeconds(), original.getCacheTimeoutSeconds() ) );
        }
        if ( store.getMetadataTimeoutSeconds() != original.getMetadataTimeoutSeconds() )
        {
            result.put( "metadata_timeout_seconds",
                        asList( store.getMetadataTimeoutSeconds(), original.getMetadataTimeoutSeconds() ) );
        }
        if ( store.isPassthrough() != original.isPassthrough() )
        {
            result.put( "is_passthrough", asList( store.isPassthrough(), original.isPassthrough() ) );
        }
        if ( isDiff( store.getPrefetchPriority(), original.getPrefetchPriority() ) )
        {
            result.put( "prefetch_priority", asList( store.getPrefetchPriority(), original.getPrefetchPriority() ) );
        }
        if ( store.isPrefetchRescan() != original.isPrefetchRescan() )
        {
            result.put( "prefetch_rescan", asList( store.isPrefetchRescan(), original.isPrefetchRescan() ) );
        }
        if ( isDiff( store.getPrefetchListingType(), original.getPrefetchListingType() ) )
        {
            result.put( "prefetch_listing_type",
                        asList( store.getPrefetchListingType(), original.getPrefetchListingType() ) );
        }
        if ( isDiff( store.getPrefetchRescanTimestamp(), original.getPrefetchRescanTimestamp() ) )
        {
            result.put( "prefetch_rescan_time",
                        asList( store.getPrefetchRescanTimestamp(), original.getPrefetchRescanTimestamp() ) );
        }
        if ( isDiff( store.getUrl(), original.getUrl() ) )
        {
            result.put( "url", asList( store.getUrl(), original.getUrl() ) );
        }
        if ( isDiff( store.getKeyPassword(), original.getKeyPassword() ) )
        {
            result.put( "key_password", asList( store.getKeyPassword(), original.getKeyPassword() ) );
        }
        if ( isDiff( store.getServerCertPem(), original.getServerCertPem() ) )
        {
            result.put( "server_certificate_pem", asList( store.getServerCertPem(), original.getServerCertPem() ) );
        }
        if ( isDiff( store.getProxyHost(), original.getProxyHost() ) )
        {
            result.put( "proxy_host", asList( store.getProxyHost(), original.getProxyHost() ) );
        }
        if ( store.getProxyPort() != original.getProxyPort() )
        {
            result.put( "proxy_port", asList( store.getProxyPort(), original.getProxyPort() ) );
        }
        if ( isDiff( store.getProxyUser(), original.getProxyUser() ) )
        {
            result.put( "proxy_user", asList( store.getProxyUser(), original.getProxyUser() ) );
        }
        if ( isDiff( store.getProxyPassword(), original.getProxyPassword() ) )
        {
            result.put( "proxy_password", asList( store.getProxyPassword(), original.getProxyPassword() ) );
        }
        if ( isDiff( store.getServerTrustPolicy(), original.getServerTrustPolicy() ) )
        {
            result.put( "server_trust_policy",
                        asList( store.getServerTrustPolicy(), original.getServerTrustPolicy() ) );
        }
        return result;
    }

    private Map<String, List<Object>> diffHosted( final HostedRepository store, final HostedRepository original )
    {
        final Map<String, List<Object>> result = new HashMap<>();
        if ( store.isAllowReleases() != original.isAllowReleases() )
        {
            result.put( "allow_releases", asList( store.isAllowReleases(), original.isAllowReleases() ) );
        }
        if ( store.isAllowSnapshots() != original.isAllowSnapshots() )
        {
            result.put( "allow_snapshots", asList( store.isAllowSnapshots(), original.isAllowSnapshots() ) );
        }
        if ( isDiff( store.getStorage(), original.getStorage() ) )
        {
            result.put( "storage", asList( store.getStorage(), original.getStorage() ) );
        }
        if ( store.getSnapshotTimeoutSeconds() != original.getSnapshotTimeoutSeconds() )
        {
            result.put( "snapshotTimeoutSeconds",
                        asList( store.getSnapshotTimeoutSeconds(), original.getSnapshotTimeoutSeconds() ) );
        }
        if ( store.isReadonly() != original.isReadonly() )
        {
            result.put( "readonly", asList( store.isReadonly(), original.isReadonly() ) );
        }

        return result;
    }

    private Map<String, List<Object>> diffGroup( final Group store, final Group original )
    {
        final Map<String, List<Object>> result = new HashMap<>();
        if ( store.isPrependConstituent() != original.isPrependConstituent() )
        {
            result.put( "prepend_constituent",
                        asList( store.isPrependConstituent(), original.isPrependConstituent() ) );
        }
        if ( isDiff( new HashSet<>( store.getConstituents() ), new HashSet<>( original.getConstituents() ) ) )
        {
            result.put( "constituents", asList( store.getConstituents(), original.getConstituents() ) );
        }

        return result;
    }
}
