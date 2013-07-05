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
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.persistence.PlayerEntityStore;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
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
import java.util.Map;

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

    private List<SpawningClientInfo> clientsPreparingToSpawn = Lists.newArrayList();

    @Override
    public void initialise() {
        chunkProvider = worldRenderer.getChunkProvider();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        Iterator<SpawningClientInfo> i = clientsPreparingToSpawn.iterator();
        while (i.hasNext()) {
            SpawningClientInfo spawning = i.next();
            if (worldRenderer.getWorldProvider().isBlockRelevant(spawning.position)) {
                if (spawning.entityStore == null) {
                    spawnPlayer(spawning.clientEntity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
                } else if (!spawning.entityStore.hasCharacter()) {
                    spawning.entityStore.restoreAll();
                    spawnPlayer(spawning.clientEntity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
                } else {
                    restoreCharacter(spawning.clientEntity, spawning.entityStore);
                }
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
            Location.attachChild(playerCharacter, clientEntity, new Vector3f(), new Quat4f(0, 0, 0, 1));

            Client clientListener = networkSystem.getOwner(clientEntity);
            int distance = clientListener.getViewDistance();
            if (!clientListener.isLocal()) {
                distance += ChunkConstants.REMOTE_GENERATION_DISTANCE;
            }
            worldRenderer.getChunkProvider().updateRelevanceEntity(clientEntity, distance);
            client.character = playerCharacter;
            clientEntity.saveComponent(client);
            playerCharacter.send(new OnPlayerSpawnedEvent());
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onConnect(ConnectedEvent connected, EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        loc.setWorldPosition(connected.getEntityStore().getRelevanceLocation());
        entity.saveComponent(loc);
        worldRenderer.getChunkProvider().addRelevanceEntity(entity, 4, networkSystem.getOwner(entity));
        if (connected.getEntityStore().hasCharacter()) {
            if (worldRenderer.getWorldProvider().isBlockRelevant(connected.getEntityStore().getRelevanceLocation())) {
                restoreCharacter(entity, connected.getEntityStore());
            } else {
                SpawningClientInfo spawningClientInfo = new SpawningClientInfo(entity, connected.getEntityStore().getRelevanceLocation(), connected.getEntityStore());
                clientsPreparingToSpawn.add(spawningClientInfo);
            }
        } else {
            Vector3i pos = Vector3i.zero();
            if (chunkProvider.getChunk(pos) != null) {
                spawnPlayer(entity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
            } else {
                SpawningClientInfo spawningClientInfo = new SpawningClientInfo(entity, new Vector3f(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2));
                clientsPreparingToSpawn.add(spawningClientInfo);
            }
        }
    }

    private void restoreCharacter(EntityRef entity, PlayerEntityStore playerEntityStore) {
        Map<String, EntityRef> restoredEntities = playerEntityStore.restoreAll();
        EntityRef character = restoredEntities.get("character");
        // TODO: adjust location to safe spot
        if (character == null) {
            spawnPlayer(entity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
        } else {
            Client clientListener = networkSystem.getOwner(entity);
            int distance = clientListener.getViewDistance();
            if (!clientListener.isLocal()) {
                distance += ChunkConstants.REMOTE_GENERATION_DISTANCE;
            }
            worldRenderer.getChunkProvider().updateRelevanceEntity(entity, distance);
            ClientComponent client = entity.getComponent(ClientComponent.class);
            client.character = character;
            entity.saveComponent(client);

            CharacterComponent characterComp = character.getComponent(CharacterComponent.class);
            if (characterComp != null) {
                characterComp.controller = entity;
                character.saveComponent(characterComp);
                NetworkComponent netComp = character.getComponent(NetworkComponent.class);
                netComp.owner = entity;
                character.saveComponent(netComp);
                Location.attachChild(character, entity, new Vector3f(), new Quat4f(0, 0, 0, 1));
            } else {
                character.destroy();
                spawnPlayer(entity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
            }
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onDisconnect(DisconnectedEvent event, EntityRef entity) {
        EntityRef character = entity.getComponent(ClientComponent.class).character;
        if (character.exists()) {
            event.getEntityStore().setHasCharacter(true);
            event.getEntityStore().setRelevanceLocation(character.getComponent(LocationComponent.class).getWorldPosition());
            event.getEntityStore().store(character, "character");
        }
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

                SpawningClientInfo info = new SpawningClientInfo(entity, new Vector3f(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2));
                clientsPreparingToSpawn.add(info);
            }
        }
    }

    private static class SpawningClientInfo {
        public EntityRef clientEntity;
        public PlayerEntityStore entityStore;
        public Vector3f position;

        public SpawningClientInfo(EntityRef client, Vector3f position) {
            this.clientEntity = client;
            this.position = position;
        }

        public SpawningClientInfo(EntityRef client, Vector3f position, PlayerEntityStore entityStore) {
            this(client, position);
            this.entityStore = entityStore;
        }
    }
}
