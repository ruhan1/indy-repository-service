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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import static org.commonjava.indy.service.repository.change.audit.StoreAuditManager.TABLE_AUDIT;

@Table( name = TABLE_AUDIT, readConsistency = "QUORUM", writeConsistency = "QUORUM" )
public class DtxRepoOpsAuditRecord
{

    @Column
    @PartitionKey( 0 )
    private String repoName;

    @Column
    private String time;

    @Column
    private String operation;

    @Column
    private String changeContent;

    public String getTime()
    {
        return time;
    }

    public void setTime( String time )
    {
        this.time = time;
    }

    public String getRepoName()
    {
        return repoName;
    }

    public void setRepoName( String repoName )
    {
        this.repoName = repoName;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation( String operation )
    {
        this.operation = operation;
    }

    public String getChangeContent()
    {
        return changeContent;
    }

    public void setChangeContent( String changeContent )
    {
        this.changeContent = changeContent;
    }

    @Override
    public String toString()
    {
        return String.format( "RepoOpsAuditRecord{Time=%s, repoName=%s, operation=%s, changeContent=%s}", time,
                              repoName, operation, changeContent );
    }
}
