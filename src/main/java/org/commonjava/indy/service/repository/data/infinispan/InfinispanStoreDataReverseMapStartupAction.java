/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy-repository-service)
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
package org.commonjava.indy.service.repository.data.infinispan;

import io.quarkus.runtime.Startup;
import org.commonjava.indy.service.repository.data.StoreDataManager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Startup
@Deprecated
public class InfinispanStoreDataReverseMapStartupAction

{
    @Inject
    StoreDataManager storeDataManager;

    @PostConstruct
    public void onStart()
    {
        if ( storeDataManager instanceof InfinispanStoreDataManager )
        {
            ( (InfinispanStoreDataManager) storeDataManager ).initAffectedBy();
        }
    }

    //    public int getStartupPriority()
    //    {
    //        return 10;
    //    }
}
