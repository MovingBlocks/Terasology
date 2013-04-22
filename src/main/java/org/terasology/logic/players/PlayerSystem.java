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

package org.terasology.logic.players;

import com.google.common.collect.Lists;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.entityFactory.PlayerFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.event.RespawnRequestEvent;
import org.terasology.math.Vector3i;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class PlayerSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private NetworkSystem networkSystem;

    @In
    private InventoryManager inventoryManager;

    private ChunkProvider chunkProvider;

    private List<EntityRef> clientsPreparingToSpawn = Lists.newArrayList();

    @Override
    public void initialise() {
        chunkProvider = worldRenderer.getChunkProvider();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        Iterator<EntityRef> i = clientsPreparingToSpawn.iterator();
        while (i.hasNext()) {
            EntityRef spawning = i.next();
            if (chunkProvider.getChunk(Vector3i.zero()) != null) {
                spawnPlayer(spawning, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
                i.remove();
            }
        }
    }

    private void spawnPlayer(EntityRef clientEntity, Vector3i spawnPos) {
        while (worldRenderer.getWorldProvider().getBlock(spawnPos) == BlockManager.getAir() && spawnPos.y > 0) {
            spawnPos.y--;
        }

        ClientComponent client = clientEntity.getComponent(ClientComponent.class);
        if (client != null) {
            PlayerFactory playerFactory = new PlayerFactory(entityManager, inventoryManager);
            EntityRef playerCharacter = playerFactory.newInstance(new Vector3f(spawnPos.x, spawnPos.y + 1.5f, spawnPos.z), clientEntity);
            Location.attachChild(playerCharacter, clientEntity);
            LocationComponent clientLoc = clientEntity.getComponent(LocationComponent.class);
            clientLoc.setLocalPosition(new Vector3f());
            clientLoc.setLocalRotation(new Quat4f(0,0,0,1));
            clientEntity.saveComponent(clientLoc);

            NetworkComponent netComp = playerCharacter.getComponent(NetworkComponent.class);
            if (netComp != null) {
                netComp.owner = clientEntity;
                playerCharacter.saveComponent(netComp);
            }
            Client clientListener = networkSystem.getOwner(clientEntity);
            int distance = clientListener.getViewDistance();
            if (!clientListener.isLocal()) {
                distance += ChunkConstants.REMOTE_GENERATION_DISTANCE;
            }
            worldRenderer.getChunkProvider().updateRelevanceEntity(playerCharacter, distance);
            client.character = playerCharacter;
            clientEntity.saveComponent(client);
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onConnect(ConnectedEvent connected, EntityRef entity) {
        Vector3i pos = Vector3i.zero();
        worldRenderer.getChunkProvider().addRelevanceEntity(entity, 4, networkSystem.getOwner(entity));
        if (chunkProvider.getChunk(pos) != null) {
            spawnPlayer(entity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
        } else {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            location.setWorldPosition(new Vector3f(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2));
            entity.saveComponent(location);

            clientsPreparingToSpawn.add(entity);
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onDisconnect(DisconnectedEvent connected, EntityRef entity) {
        entity.getComponent(ClientComponent.class).character.destroy();
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void onRespawnRequest(RespawnRequestEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (!client.character.exists()) {
            Vector3i pos = Vector3i.zero();
            if (chunkProvider.getChunk(pos) != null) {
                spawnPlayer(entity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
            } else {
                LocationComponent loc = entity.getComponent(LocationComponent.class);
                loc.setWorldPosition(new Vector3f(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2));
                entity.saveComponent(loc);
                worldRenderer.getChunkProvider().updateRelevanceEntity(entity, 4);

                clientsPreparingToSpawn.add(entity);
            }
        }
    }
}
