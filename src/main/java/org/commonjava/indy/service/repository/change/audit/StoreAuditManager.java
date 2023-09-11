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
package org.commonjava.indy.service.repository.change.audit;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.apache.commons.lang3.StringUtils;
import org.commonjava.indy.service.repository.config.IndyRepositoryConfiguration;
import org.commonjava.indy.service.repository.data.cassandra.CassandraClient;
import org.commonjava.indy.service.repository.data.cassandra.CassandraConfiguration;
import org.commonjava.indy.service.repository.data.cassandra.SchemaUtils;
import org.commonjava.indy.service.repository.model.StoreKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.lang.String.format;

@ApplicationScoped
//@Startup
public class StoreAuditManager
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public static final String TABLE_AUDIT = "repo_audit";

    @Inject
    CassandraClient client;

    @Inject
    CassandraConfiguration config;

    @Inject
    IndyRepositoryConfiguration repoConfig;

    private Mapper<DtxRepoOpsAuditRecord> auditMapper;

    private Session session;

    private PreparedStatement preparedStoreAuditQueryByRepo;

    private PreparedStatement preparedStoreAuditQueryByRepoAndOps;

    public StoreAuditManager()
    {
    }

    public StoreAuditManager( CassandraClient client, CassandraConfiguration config )
    {
        this.client = client;
        this.config = config;
        init();
    }

    @PostConstruct
    public void init()
    {
        if ( repoConfig.repoAuditEnabled() )
        {
            String keySpace = config.getKeyspace();
            session = client.getSession( keySpace );
            session.execute( SchemaUtils.getSchemaCreateKeyspace( keySpace, config.getKeyspaceReplicas() ) );
            session.execute( getSchemaCreateTableStore( keySpace ) );

            MappingManager manager = new MappingManager( session );

            auditMapper = manager.mapper( DtxRepoOpsAuditRecord.class, keySpace );
            preparedStoreAuditQueryByRepo = session.prepare(
                    format( "SELECT time, reponame, operation, changecontent FROM %s.%s WHERE reponame=? ORDER BY time DESC limit ?",
                            keySpace, TABLE_AUDIT ) );

            preparedStoreAuditQueryByRepoAndOps = session.prepare(
                    format( "SELECT time, reponame, operation, changecontent FROM %s.%s WHERE reponame=? and operation=? ORDER BY time DESC limit ? ALLOW FILTERING",
                            keySpace, TABLE_AUDIT ) );
        }
    }

    private static String getSchemaCreateTableStore( String keySpace )
    {
        // @formatter:off
        return format("CREATE TABLE IF NOT EXISTS %s.%s ("
                          + "time varchar,"
                          + "reponame varchar,"
                          + "operation varchar,"
                          + "changecontent varchar,"
                          + "PRIMARY KEY ((reponame), time)"
                          + ") WITH CLUSTERING ORDER BY (time DESC);",
                      keySpace, TABLE_AUDIT);
        // @formatter:on
    }

    public void recordLog( final StoreKey storeKey, final String ops, final String content )
    {
        if ( repoConfig.repoAuditEnabled() )
        {
            auditMapper.save( toDtxRepoOpsAuditRecord( storeKey, ops, content ) );
        }
        else
        {
            logger.warn( "Warning: Repository audit log is not enabled, will not record audit log" );
        }

    }

    public List<DtxRepoOpsAuditRecord> getAuditLogByRepo( final String key, final int limit )
    {
        BoundStatement bound = preparedStoreAuditQueryByRepo.bind( key, limit );
        ResultSet result = session.execute( bound );

        List<DtxRepoOpsAuditRecord> records = new ArrayList<>();
        result.forEach( row -> records.add( toDtxRepoOpsAuditRecord( row ) ) );

        return records;
    }

    public List<DtxRepoOpsAuditRecord> getAuditLogByRepoAndOps( final String key, final String ops, final int limit )
    {
        BoundStatement bound = preparedStoreAuditQueryByRepoAndOps.bind( key, ops, limit );
        ResultSet result = session.execute( bound );

        List<DtxRepoOpsAuditRecord> records = new ArrayList<>();
        result.forEach( row -> records.add( toDtxRepoOpsAuditRecord( row ) ) );

        return records;
    }

    private String getAuditTime( Date date )
    {
        final SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
        format.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return format.format( date );
    }

    private DtxRepoOpsAuditRecord toDtxRepoOpsAuditRecord( final StoreKey storeKey, final String ops,
                                                           final String changeContent )
    {
        final long currentMillis = System.currentTimeMillis();
        DtxRepoOpsAuditRecord record = new DtxRepoOpsAuditRecord();
        record.setTime( getAuditTime( new Date( currentMillis ) ) );
        record.setRepoName( storeKey.toString() );
        record.setOperation( ops );
        if ( StringUtils.isNotBlank( changeContent ) )
        {
            record.setChangeContent( changeContent );
        }

        return record;
    }

    private DtxRepoOpsAuditRecord toDtxRepoOpsAuditRecord( final Row row )
    {
        if ( row == null )
        {
            return null;
        }
        DtxRepoOpsAuditRecord record = new DtxRepoOpsAuditRecord();
        record.setTime( row.getString( "time" ) );
        record.setRepoName( row.getString( "reponame" ) );
        record.setOperation( row.getString( "operation" ) );
        record.setChangeContent( row.getString( "changecontent" ) );
        return record;
    }
}
