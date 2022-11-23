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
package org.commonjava.indy.service.repository.ftests.admin.event;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySink;
import org.commonjava.event.store.IndyStoreEvent;
import org.commonjava.event.store.StoreEventType;
import org.commonjava.event.store.StorePostDeleteEvent;
import org.commonjava.event.store.StorePreDeleteEvent;
import org.commonjava.indy.service.repository.change.event.kafka.KafkaEventUtils;
import org.commonjava.indy.service.repository.ftests.matchers.RepoEqualMatcher;
import org.commonjava.indy.service.repository.ftests.profile.ISPNFunctionProfile;
import org.commonjava.indy.service.repository.model.Group;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.awaitility.Awaitility.await;
import static org.commonjava.indy.service.repository.model.pkg.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * This case test if the repo creation will trigger the delete event correctly
 * when: <br />
 * <ul>
 *      <li>creates a repo and then deletes the repo</li>
 * </ul>
 * then: <br />
 * <ul>
 *     <li>There will be 4 events generated with type of PreUpdate and PostUpdate</li>
 *     <li>These last two events will be with UpdateType of UPDATE, and will have correct info with updating</li>
 * </ul>
 */
@QuarkusTest
@TestProfile( ISPNFunctionProfile.class )
@Tag( "function" )
public class CreateThenDeleteRepoWithEventTest
        extends AbstractStoreEventTest
{
    @Test
    public void run()
            throws Exception
    {
        final String name = newName();
        final Group repo = new Group( MAVEN_PKG_KEY, name );
        String json = mapper.writeValueAsString( repo );

        given().body( json )
               .contentType( APPLICATION_JSON )
               .post( getRepoTypeUrl( repo.getKey() ) )
               .then()
               .body( new RepoEqualMatcher<>( mapper, repo, Group.class ) );

        delete(  getRepoUrl( repo.getKey() ) );

        final InMemorySink<IndyStoreEvent> eventsChannel = connector.sink( KafkaEventUtils.CHANNEL_STORE );
        await().<List<? extends Message<IndyStoreEvent>>>until( eventsChannel::received,
                                                                t -> t.size() >= 4 );  // to wait for event sending

        List<? extends Message<IndyStoreEvent>> events = eventsChannel.received();
        assertThat( events.size(), is( 4 ) );

        IndyStoreEvent storeEvent = eventsChannel.received().get( 2 ).getPayload();
        assertThat( storeEvent.getEventType(), is( StoreEventType.PreDelete ) );
        StorePreDeleteEvent preDelete = (StorePreDeleteEvent) storeEvent;
        assertThat( preDelete.getKeys(), contains( repo.getKey().toEventStoreKey() ) );

        storeEvent = eventsChannel.received().get( 3 ).getPayload();
        assertThat( storeEvent.getEventType(), is( StoreEventType.PostDelete ) );
        StorePostDeleteEvent postDelete = (StorePostDeleteEvent) storeEvent;
        assertThat( postDelete.getKeys(), contains( repo.getKey().toEventStoreKey() ) );
    }
}
