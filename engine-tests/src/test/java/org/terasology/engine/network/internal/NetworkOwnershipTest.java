// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.metadata.EventLibrary;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.world.BlockEntityRegistry;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("TteTest")
public class NetworkOwnershipTest extends TerasologyTestingEnvironment {

    private static EngineEntityManager entityManager;
    private NetworkSystemImpl networkSystem;
    private NetClient client;
    private EntityRef clientEntity;


    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);
        EngineTime mockTime = mock(EngineTime.class);
        networkSystem = new NetworkSystemImpl(mockTime, context);
        networkSystem.setContext(context);
        context.put(NetworkSystem.class, networkSystem);

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = (PojoEntityManager) context.get(EntityManager.class);
        context.put(ComponentSystemManager.class, new ComponentSystemManager(context));
        entityManager.clear();
        client = mock(NetClient.class);
        NetworkComponent clientNetComp = new NetworkComponent();
        clientNetComp.replicateMode = NetworkComponent.ReplicateMode.OWNER;
        clientEntity = entityManager.create(clientNetComp);
        when(client.getEntity()).thenReturn(clientEntity);
        when(client.getId()).thenReturn("dummyID");
        networkSystem.mockHost();
        networkSystem.connectToEntitySystem(entityManager, context.get(EventLibrary.class), mock(BlockEntityRegistry.class));
        networkSystem.registerNetworkEntity(clientEntity);
        context.put(ServerConnectListManager.class, new ServerConnectListManager(context));
    }

    private void connectClient() {
        networkSystem.addClient(client);
        networkSystem.update();
        verify(client).setNetInitial(clientEntity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void testClientSentNetInitialForNewNetworkEntity() {
        connectClient();
        EntityRef entity = entityManager.create(new NetworkComponent());
        networkSystem.registerNetworkEntity(entity);
        assertTrue(entity.getComponent(NetworkComponent.class).getNetworkId() != 0);
        verify(client).setNetInitial(entity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void testClientSentNetInitialForExistingNetworkEntityOnConnect() {
        EntityRef entity = entityManager.create(new NetworkComponent());
        networkSystem.registerNetworkEntity(entity);
        connectClient();
        assertTrue(entity.getComponent(NetworkComponent.class).getNetworkId() != 0);
        verify(client).setNetInitial(entity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void testClientNoInitialEntityIfNotOwnedAndReplicateToOwner() {
        connectClient();
        NetworkComponent netComp = new NetworkComponent();
        netComp.replicateMode = NetworkComponent.ReplicateMode.OWNER;
        EntityRef entity = entityManager.create(netComp);

        networkSystem.registerNetworkEntity(entity);

        assertTrue(entity.getComponent(NetworkComponent.class).getNetworkId() != 0);

        verify(client, times(0)).setNetInitial(entity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void testClientSentInitialIfOwnedEntityRegistered() {
        connectClient();
        EntityBuilder builder = entityManager.newBuilder();
        NetworkComponent netComp = builder.addComponent(new NetworkComponent());
        netComp.replicateMode = NetworkComponent.ReplicateMode.OWNER;
        builder.setOwner(clientEntity);
        EntityRef entity = builder.build();

        networkSystem.registerNetworkEntity(entity);

        assertTrue(entity.getComponent(NetworkComponent.class).getNetworkId() != 0);
        verify(client).setNetInitial(entity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void testClientSentInitialOnlyOnce() {
        EntityBuilder builder = entityManager.newBuilder();
        NetworkComponent netComp = builder.addComponent(new NetworkComponent());
        netComp.replicateMode = NetworkComponent.ReplicateMode.OWNER;
        builder.setOwner(clientEntity);
        EntityRef entity = builder.build();

        networkSystem.registerNetworkEntity(entity);
        connectClient();
        networkSystem.updateOwnership(entity);

        verify(client, times(1)).setNetInitial(entity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void testClientSentInitialForOwnershipChain() {
        NetworkComponent netCompA = new NetworkComponent();
        netCompA.replicateMode = NetworkComponent.ReplicateMode.OWNER;
        EntityRef entityA = entityManager.create(netCompA);

        EntityBuilder builder = entityManager.newBuilder();
        NetworkComponent netCompB = builder.addComponent(new NetworkComponent());
        netCompB.replicateMode = NetworkComponent.ReplicateMode.OWNER;
        builder.setOwner(entityA);
        EntityRef entityB = builder.build();

        networkSystem.registerNetworkEntity(entityA);
        networkSystem.registerNetworkEntity(entityB);
        connectClient();
        verify(client, times(0)).setNetInitial(entityA.getComponent(NetworkComponent.class).getNetworkId());
        verify(client, times(0)).setNetInitial(entityB.getComponent(NetworkComponent.class).getNetworkId());
        entityA.setOwner(clientEntity);
        networkSystem.updateOwnership(entityA);

        verify(client, times(1)).setNetInitial(entityA.getComponent(NetworkComponent.class).getNetworkId());
        verify(client, times(1)).setNetInitial(entityB.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void testClientSendInitialForRelevantOwnedItems() {
        EntityBuilder builder = entityManager.newBuilder();
        NetworkComponent netCompA = builder.addComponent(new NetworkComponent());
        netCompA.replicateMode = NetworkComponent.ReplicateMode.RELEVANT;
        builder.setOwner(clientEntity);
        EntityRef entityA = builder.build();

        networkSystem.registerNetworkEntity(entityA);
        connectClient();
        verify(client, times(1)).setNetInitial(entityA.getComponent(NetworkComponent.class).getNetworkId());

    }
}
