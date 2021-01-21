// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.network;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

/**
 */
public class TestNetwork extends TerasologyTestingEnvironment {

    private List<NetworkSystem> netSystems = Lists.newArrayList();

    @BeforeEach
    public void setup() throws Exception {
        CertificateGenerator generator = new CertificateGenerator();
        CertificatePair serverIdentiy = generator.generateSelfSigned();
        context.get(Config.class).getSecurity().setServerCredentials(serverIdentiy.getPublicCert(), serverIdentiy.getPrivateCert());
    }

    @AfterEach
    public void cleanUp() {
        netSystems.forEach(NetworkSystem::shutdown);

    }

    @Test
    @ResourceLock("Net_7777")
    public void testNetwork() throws Exception {
        EngineEntityManager entityManager = getEntityManager();
        EngineTime time = mock(EngineTime.class);
        NetworkSystem server = new NetworkSystemImpl(time, context);
        server.setContext(context);
        netSystems.add(server);
        server.connectToEntitySystem(entityManager, context.get(EventLibrary.class), null);
        server.host(7777, true);

        Thread.sleep(500);

        NetworkSystem client = new NetworkSystemImpl(time, context);
        client.setContext(context);
        netSystems.add(client);
        client.join("localhost", 7777);

        Thread.sleep(500);

        server.shutdown();
        client.shutdown();
    }


    @Test
    @ResourceLock("Net_7777")
    public void testEntityNetworkIdChangedOnServerStart() throws HostingFailedException {
        EngineEntityManager entityManager = getEntityManager();
        NetworkComponent netComp = new NetworkComponent();
        netComp.setNetworkId(122);
        EntityRef entity = entityManager.create(netComp);
        EngineTime time = mock(EngineTime.class);
        NetworkSystem server = new NetworkSystemImpl(time, context);
        server.setContext(context);
        netSystems.add(server);
        server.connectToEntitySystem(entityManager, context.get(EventLibrary.class), null);
        server.host(7777, true);

        assertFalse(122 == entity.getComponent(NetworkComponent.class).getNetworkId());
        server.shutdown();
    }
}
