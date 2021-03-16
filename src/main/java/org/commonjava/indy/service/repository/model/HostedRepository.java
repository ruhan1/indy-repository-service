/**
 * Copyright (C) 2011-2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
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

import org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Schema( description = "Hosts artifact content on the local system" )
public class HostedRepository
        extends AbstractRepository
        implements Externalizable
{

    private static final int STORE_VERSION = 1;

    private String storage;

    private int snapshotTimeoutSeconds;

    // if readonly, default is not
    @Schema( type = SchemaType.BOOLEAN, description = "identify if the hoste repo is readonly" )
    private boolean readonly = false;

    public HostedRepository()
    {
        super();
    }

    public HostedRepository( final String packageType, final String name )
    {
        super( packageType, StoreType.hosted, name );
    }

    @Deprecated
    public HostedRepository( final String name )
    {
        super( MavenPackageTypeDescriptor.MAVEN_PKG_KEY, StoreType.hosted, name );
    }

    @Override
    public String toString()
    {
        return String.format( "HostedRepository [%s]", getName() );
    }

    public int getSnapshotTimeoutSeconds()
    {
        return snapshotTimeoutSeconds;
    }

    public void setSnapshotTimeoutSeconds( final int snapshotTimeoutSeconds )
    {
        this.snapshotTimeoutSeconds = snapshotTimeoutSeconds;
    }

    public String getStorage()
    {
        return storage;
    }

    public void setStorage( final String storage )
    {
        this.storage = storage;
    }

    public boolean isReadonly()
    {
        return readonly;
    }

    public void setReadonly( boolean readonly )
    {
        this.readonly = readonly;
    }

    @Override
    public boolean isAuthoritativeIndex()
    {
        return super.isAuthoritativeIndex() || this.isReadonly();
    }

    @Override
    public void setAuthoritativeIndex( boolean authoritativeIndex )
    {
        super.setAuthoritativeIndex( authoritativeIndex || this.isReadonly() );
    }

    @Override
    public HostedRepository copyOf()
    {
        return copyOf( getPackageType(), getName() );
    }

    @Override
    public HostedRepository copyOf( final String packageType, final String name )
    {
        HostedRepository repo = new HostedRepository( packageType, name );
        repo.setStorage( getStorage() );
        repo.setSnapshotTimeoutSeconds( getSnapshotTimeoutSeconds() );
        repo.setReadonly( isReadonly() );
        copyRestrictions( repo );
        copyBase( repo );

        return repo;
    }

    @Override
    public void writeExternal( final ObjectOutput out )
            throws IOException
    {
        super.writeExternal( out );

        out.writeInt( STORE_VERSION );

        out.writeObject( storage );
        out.writeInt( snapshotTimeoutSeconds );
        out.writeBoolean( readonly );
    }

    @Override
    public void readExternal( final ObjectInput in )
            throws IOException, ClassNotFoundException
    {
        super.readExternal( in );

        int storeVersion = in.readInt();
        if ( storeVersion > STORE_VERSION )
        {
            throw new IOException( "Cannot deserialize. HostedRepository version in data stream is: " + storeVersion
                                           + " but this class can only deserialize up to version: " + STORE_VERSION );
        }

        this.storage = (String) in.readObject();
        this.snapshotTimeoutSeconds = in.readInt();
        this.readonly = in.readBoolean();
    }

}
