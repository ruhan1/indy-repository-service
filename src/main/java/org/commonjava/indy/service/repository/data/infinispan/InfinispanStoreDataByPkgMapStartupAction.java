/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
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

import io.quarkus.runtime.StartupEvent;
import org.commonjava.indy.service.repository.data.StoreDataManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class InfinispanStoreDataByPkgMapStartupAction
{

    @Inject
    private StoreDataManager storeDataManager;

    public void onStart( @Observes StartupEvent start )
    {
        if ( storeDataManager instanceof InfinispanStoreDataManager )
        {
            ( (InfinispanStoreDataManager) storeDataManager ).initByPkgMap();
        }
    }

    //    public int getStartupPriority()
    //    {
    //        return 11;
    //    }

}
