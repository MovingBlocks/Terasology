/*
 * Copyright 2016 MovingBlocks
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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.logic.players.event.RespawnRequestEvent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.persistence.PlayerStore;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.WorldProvider;
import org.terasology.world.generator.WorldGenerator;

import java.util.Iterator;
import java.util.List;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PlayerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private WorldGenerator worldGenerator;

    @In
    private WorldProvider worldProvider;

    @In
    private NetworkSystem networkSystem;

    private List<SpawningClientInfo> clientsPreparingToSpawn = Lists.newArrayList();

    @Override
    public void initialise() {
    }

    @Override
    public void update(float delta) {
        Iterator<SpawningClientInfo> i = clientsPreparingToSpawn.iterator();
        while (i.hasNext()) {
            SpawningClientInfo spawning = i.next();
            if (worldProvider.isBlockRelevant(spawning.position)) {
                PlayerStore playerStore = spawning.playerStore;
                if (playerStore == null) {
                    spawnPlayer(spawning.clientEntity);
                } else {
                    playerStore.restoreEntities();
                    EntityRef character = playerStore.getCharacter();
                    restoreCharacter(spawning.clientEntity, character);
                }
                i.remove();
            }
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onConnect(ConnectedEvent connected, EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);

        // for new clients, the player store will return default values
        PlayerStore playerStore = connected.getPlayerStore();

        Client owner = networkSystem.getOwner(entity);
        Vector3i minViewDist = ViewDistance.LEGALLY_BLIND.getChunkDistance();

        if (playerStore.hasCharacter()) {

            Vector3f storedLocation = playerStore.getRelevanceLocation();
            loc.setWorldPosition(storedLocation);
            entity.saveComponent(loc);

            if (worldProvider.isBlockRelevant(storedLocation)) {
                // chunk for spawning location is ready, so spawn right now
                playerStore.restoreEntities();
                EntityRef character = playerStore.getCharacter();
                Vector3i viewDist = owner.getViewDistance().getChunkDistance();
                addRelevanceEntity(entity, viewDist, owner);
                restoreCharacter(entity, character);
            } else {
                // otherwise wait until chunk is ready
                addRelevanceEntity(entity, minViewDist, owner);
                clientsPreparingToSpawn.add(new SpawningClientInfo(entity, storedLocation, playerStore));
            }
        } else {
            Vector3f spawnPosition = worldGenerator.getSpawnPosition(entity);
            loc.setWorldPosition(spawnPosition);
            entity.saveComponent(loc);

            addRelevanceEntity(entity, minViewDist, owner);
            if (worldProvider.isBlockRelevant(spawnPosition)) {
                spawnPlayer(entity);
            } else {
                clientsPreparingToSpawn.add(new SpawningClientInfo(entity, spawnPosition));
            }
        }
    }

    private void restoreCharacter(EntityRef entity, EntityRef character) {

        Client clientListener = networkSystem.getOwner(entity);
        updateRelevanceEntity(entity, clientListener.getViewDistance().getChunkDistance());

        ClientComponent client = entity.getComponent(ClientComponent.class);
        client.character = character;
        entity.saveComponent(client);

        CharacterComponent characterComp = character.getComponent(CharacterComponent.class);
        if (characterComp != null) {
            characterComp.controller = entity;
            character.saveComponent(characterComp);
            character.setOwner(entity);
            Location.attachChild(character, entity, new Vector3f(), new Quat4f(0, 0, 0, 1));
        } else {
            character.destroy();
            spawnPlayer(entity);
        }
    }

    private void updateRelevanceEntity(EntityRef entity, Vector3i chunkDistance) {
        //RelevanceRegionComponent relevanceRegion = new RelevanceRegionComponent();
        //relevanceRegion.distance = chunkDistance;
        //entity.saveComponent(relevanceRegion);
        worldRenderer.getChunkProvider().updateRelevanceEntity(entity, chunkDistance);
    }

    private void removeRelevanceEntity(EntityRef entity) {
        //entity.removeComponent(RelevanceRegionComponent.class);
        worldRenderer.getChunkProvider().removeRelevanceEntity(entity);
    }


    private void addRelevanceEntity(EntityRef entity, Vector3i chunkDistance, Client owner) {
        //RelevanceRegionComponent relevanceRegion = new RelevanceRegionComponent();
        //relevanceRegion.distance = chunkDistance;
        //entity.addComponent(relevanceRegion);
        worldRenderer.getChunkProvider().addRelevanceEntity(entity, chunkDistance, owner);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onDisconnect(DisconnectedEvent event, EntityRef entity) {
        removeRelevanceEntity(entity);
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void onRespawnRequest(RespawnRequestEvent event, EntityRef entity) {
        Vector3f spawnPosition = worldGenerator.getSpawnPosition(entity);
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        loc.setWorldPosition(spawnPosition);
        loc.setLocalRotation(new Quat4f());  // reset rotation
        entity.saveComponent(loc);

        if (worldProvider.isBlockRelevant(spawnPosition)) {
            spawnPlayer(entity);
        } else {
            updateRelevanceEntity(entity, ViewDistance.LEGALLY_BLIND.getChunkDistance());
            SpawningClientInfo info = new SpawningClientInfo(entity, spawnPosition);
            clientsPreparingToSpawn.add(info);
        }
    }

    private void spawnPlayer(EntityRef clientEntity) {

        ClientComponent client = clientEntity.getComponent(ClientComponent.class);

        PlayerFactory playerFactory = new PlayerFactory(entityManager, worldProvider);
        EntityRef playerCharacter = playerFactory.newInstance(clientEntity);

        Client clientListener = networkSystem.getOwner(clientEntity);
        Vector3i distance = clientListener.getViewDistance().getChunkDistance();
        updateRelevanceEntity(clientEntity, distance);
        client.character = playerCharacter;
        clientEntity.saveComponent(client);
        playerCharacter.send(new OnPlayerSpawnedEvent());
    }


    private static class SpawningClientInfo {
        public EntityRef clientEntity;
        public PlayerStore playerStore;
        public Vector3f position;

        public SpawningClientInfo(EntityRef client, Vector3f position) {
            this.clientEntity = client;
            this.position = position;
        }

        public SpawningClientInfo(EntityRef client, Vector3f position, PlayerStore playerStore) {
            this(client, position);
            this.playerStore = playerStore;
        }
    }
}
