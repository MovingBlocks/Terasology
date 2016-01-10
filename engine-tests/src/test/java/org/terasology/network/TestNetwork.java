/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.network;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.config.Config;
import org.terasology.engine.EngineTime;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.identity.CertificateGenerator;
import org.terasology.identity.CertificatePair;
import org.terasology.network.exceptions.HostingFailedException;
import org.terasology.network.internal.NetworkSystemImpl;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

/**
 */
public class TestNetwork extends TerasologyTestingEnvironment {

    private List<NetworkSystem> netSystems = Lists.newArrayList();

    @Before
    public void setup() throws Exception {
        super.setup();
        CertificateGenerator generator = new CertificateGenerator();
        CertificatePair serverIdentiy = generator.generateSelfSigned();
        context.get(Config.class).getSecurity().setServerCredentials(serverIdentiy.getPublicCert(), serverIdentiy.getPrivateCert());
    }

    @After
    public void cleanUp() {
        netSystems.forEach(NetworkSystem::shutdown);

    }

    @Test
    public void testNetwork() throws Exception {
        EngineEntityManager entityManager = getEntityManager();
        EngineTime time = mock(EngineTime.class);
        NetworkSystem server = new NetworkSystemImpl(time, context);
        netSystems.add(server);
        server.connectToEntitySystem(entityManager, context.get(EventLibrary.class), null);
        server.host(7777, true);

        Thread.sleep(500);

        NetworkSystem client = new NetworkSystemImpl(time, context);
        netSystems.add(client);
        client.join("localhost", 7777);

        Thread.sleep(500);

        server.shutdown();
        client.shutdown();
    }


    @Test
    public void entityNetworkIdChangedOnServerStart() throws HostingFailedException {
        EngineEntityManager entityManager = getEntityManager();
        NetworkComponent netComp = new NetworkComponent();
        netComp.setNetworkId(122);
        EntityRef entity = entityManager.create(netComp);
        EngineTime time = mock(EngineTime.class);
        NetworkSystem server = new NetworkSystemImpl(time, context);
        netSystems.add(server);
        server.connectToEntitySystem(entityManager, context.get(EventLibrary.class), null);
        server.host(7777, true);

        assertFalse(122 == entity.getComponent(NetworkComponent.class).getNetworkId());
        server.shutdown();
    }
}
