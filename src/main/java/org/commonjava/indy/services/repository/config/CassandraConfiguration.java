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

import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@Startup
@ApplicationScoped
public class CassandraConfiguration
{
    @ConfigProperty( name = "cassandra.enabled", defaultValue = "false" )
    private Boolean enabled;

    @ConfigProperty( name = "cassandra.host", defaultValue = "localhost" )
    private String cassandraHost;

    @ConfigProperty( name = "cassandra.port", defaultValue = "9402" )
    private Integer cassandraPort;

    @ConfigProperty( name = "cassandra.user", defaultValue = " " )
    private String cassandraUser;

    @ConfigProperty( name = "cassandra.pass", defaultValue = " " )
    private String cassandraPass;

    @ConfigProperty( name = "cassandra.timeoutMillis.connect", defaultValue = "60000" )
    private int connectTimeoutMillis;

    @ConfigProperty( name = "cassandra.timeoutMillis.read", defaultValue = "60000" )
    private int readTimeoutMillis;

    @ConfigProperty( name = "cassandra.retries.read", defaultValue = "3" )
    private int readRetries;

    @ConfigProperty( name = "cassandra.retries.write", defaultValue = "3" )
    private int writeRetries;

    @ConfigProperty( name = "cassandra.keyspace", defaultValue = " " )
    private String keyspace;

    @ConfigProperty( name = "cassandra.replica", defaultValue = "0" )
    private int replicationFactor;

    public CassandraConfiguration()
    {
    }

    public Boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( Boolean enabled )
    {
        this.enabled = enabled;
    }

    public void setCassandraHost( String host )
    {
        cassandraHost = host;
    }

    public void setCassandraPort( Integer port )
    {
        cassandraPort = port;
    }

    public void setCassandraUser( String cassandraUser )
    {
        this.cassandraUser = cassandraUser;
    }

    public void setCassandraPass( String cassandraPass )
    {
        this.cassandraPass = cassandraPass;
    }

    public String getCassandraHost()
    {
        return cassandraHost;
    }

    public Integer getCassandraPort()
    {
        return cassandraPort;
    }

    public String getCassandraUser()
    {
        return cassandraUser;
    }

    public String getCassandraPass()
    {
        return cassandraPass;
    }

    public int getConnectTimeoutMillis()
    {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis( int connectTimeoutMillis )
    {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis()
    {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis( int readTimeoutMillis )
    {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public int getReadRetries()
    {
        return readRetries;
    }

    public void setReadRetries( int readRetries )
    {
        this.readRetries = readRetries;
    }

    public int getWriteRetries()
    {
        return writeRetries;
    }

    public void setWriteRetries( int writeRetries )
    {
        this.writeRetries = writeRetries;
    }

    public String getKeyspace()
    {
        return keyspace;
    }

    public void setKeyspace( String keyspace )
    {
        this.keyspace = keyspace;
    }

    public int getReplicationFactor()
    {
        return replicationFactor;
    }

    public void setReplicationFactor( int replicationFactor )
    {
        this.replicationFactor = replicationFactor;
    }
}
