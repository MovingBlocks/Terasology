/*
 * Copyright 2013 Moving Blocks
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.EngineTime;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityBuilder;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkComponent;
import org.terasology.world.BlockEntityRegistry;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Immortius
 */
public class NetworkOwnershipTest extends TerasologyTestingEnvironment {

    private static EngineEntityManager entityManager;
    private NetworkSystemImpl networkSystem;
    private NetClient client;
    private EntityRef clientEntity;

    @BeforeClass
    public static void initialise() {
        ModManager modManager = new ModManager();
        CoreRegistry.put(ModManager.class, modManager);
    }

    @Before
    public void setup() {
        EngineTime mockTime = mock(EngineTime.class);
        networkSystem = new NetworkSystemImpl(mockTime);

        entityManager = new EntitySystemBuilder().build(CoreRegistry.get(ModManager.class), networkSystem);
        CoreRegistry.put(ComponentSystemManager.class, new ComponentSystemManager());
        entityManager.clear();
        client = mock(NetClient.class);
        NetworkComponent clientNetComp = new NetworkComponent();
        clientNetComp.replicateMode = NetworkComponent.ReplicateMode.OWNER;
        clientEntity = entityManager.create(clientNetComp);
        when(client.getEntity()).thenReturn(clientEntity);
        networkSystem.mockHost();
        networkSystem.connectToEntitySystem(entityManager, CoreRegistry.get(EntitySystemLibrary.class), mock(BlockEntityRegistry.class));
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
