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
package org.commonjava.indy.service.repository.jaxrs.mock;

import io.quarkus.test.Mock;
import org.commonjava.indy.model.core.BatchDeleteResult;
import org.commonjava.indy.service.repository.client.storage.StorageService;
import org.commonjava.indy.service.repository.jaxrs.ResponseHelper;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

@Mock
@RestClient
public class MockStorageService implements StorageService
{
    @Inject ResponseHelper helper;

    @Override
    public Response purge(String filesystem) throws Exception
    {
        BatchDeleteResult result = new BatchDeleteResult();
        result.setFilesystem( filesystem );
        Set<String> succeeded = new HashSet<>();
        succeeded.add( "foo/bar/1.0/bar-1.0.jar" );
        result.setSucceeded( succeeded );
        return helper.formatOkResponseWithJsonEntity( result );
    }
}
