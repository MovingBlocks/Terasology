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

package org.terasology.network.internal;

import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.world.BlockEntityRegistry;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class NetworkOwnershipTest extends TerasologyTestingEnvironment {

    private static EngineEntityManager entityManager;
    private NetworkSystemImpl networkSystem;
    private NetClient client;
    private EntityRef clientEntity;


    @Before
    public void setup() throws Exception {
        super.setup();
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);
        EngineTime mockTime = mock(EngineTime.class);
        networkSystem = new NetworkSystemImpl(mockTime, context);
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
    }

    private void connectClient() {
        networkSystem.addClient(client);
        networkSystem.update();
        verify(client).setNetInitial(clientEntity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void clientSentNetInitialForNewNetworkEntity() {
        connectClient();
        EntityRef entity = entityManager.create(new NetworkComponent());
        networkSystem.registerNetworkEntity(entity);
        assertTrue(entity.getComponent(NetworkComponent.class).getNetworkId() != 0);
        verify(client).setNetInitial(entity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void clientSentNetInitialForExistingNetworkEntityOnConnect() {
        EntityRef entity = entityManager.create(new NetworkComponent());
        networkSystem.registerNetworkEntity(entity);
        connectClient();
        assertTrue(entity.getComponent(NetworkComponent.class).getNetworkId() != 0);
        verify(client).setNetInitial(entity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void clientNoInitialEntityIfNotOwnedAndReplicateToOwner() {
        connectClient();
        NetworkComponent netComp = new NetworkComponent();
        netComp.replicateMode = NetworkComponent.ReplicateMode.OWNER;
        EntityRef entity = entityManager.create(netComp);

        networkSystem.registerNetworkEntity(entity);

        assertTrue(entity.getComponent(NetworkComponent.class).getNetworkId() != 0);

        verify(client, times(0)).setNetInitial(entity.getComponent(NetworkComponent.class).getNetworkId());
    }

    @Test
    public void clientSentInitialIfOwnedEntityRegistered() {
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
    public void clientSentInitialOnlyOnce() {
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
    public void clientSentInitialForOwnershipChain() {
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
    public void clientSendInitialForRelevantOwnedItems() {
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
