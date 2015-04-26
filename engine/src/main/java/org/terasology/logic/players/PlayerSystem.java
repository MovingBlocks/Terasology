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

package org.terasology.logic.players;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.logic.players.event.RespawnRequestEvent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.SpiralIterable;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.persistence.PlayerStore;
import org.terasology.registry.In;
import org.terasology.rendering.world.ViewDistance;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generation.World;
import org.terasology.world.generator.WorldGenerator;

import com.google.common.collect.Lists;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class PlayerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(PlayerSystem.class);

    private final float playerHeight = 1.5f;

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
            Spawner spawner = worldGenerator.getSpawner();
            World world = worldGenerator.getWorld();
            Vector3f spawnPosition = spawner.getSpawnPosition(world, entity);
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

    private void spawnPlayer(EntityRef clientEntity) {

        LocationComponent location = clientEntity.getComponent(LocationComponent.class);

        Vector3f spawnPosition = findSpawnPos(location.getWorldPosition());
        location.setWorldPosition(spawnPosition);
        clientEntity.saveComponent(location);

        logger.debug("Spawing player at: {}", spawnPosition);

        ClientComponent client = clientEntity.getComponent(ClientComponent.class);

        PlayerFactory playerFactory = new PlayerFactory(entityManager);
        EntityRef playerCharacter = playerFactory.newInstance(spawnPosition, clientEntity);
        Location.attachChild(playerCharacter, clientEntity, new Vector3f(), new Quat4f(0, 0, 0, 1));

        Client clientListener = networkSystem.getOwner(clientEntity);
        Vector3i distance = clientListener.getViewDistance().getChunkDistance();
        updateRelevanceEntity(clientEntity, distance);
        client.character = playerCharacter;
        clientEntity.saveComponent(client);
        playerCharacter.send(new OnPlayerSpawnedEvent());
    }

    private Vector3f findSpawnPos(Vector3f targetPos) {
        int targetBlockX = TeraMath.floorToInt(targetPos.x);
        int targetBlockY = TeraMath.floorToInt(targetPos.y);
        int targetBlockZ = TeraMath.floorToInt(targetPos.z);
        for (BaseVector2i pos : SpiralIterable.clockwise(new Vector2i(targetBlockX, targetBlockZ), 4)) {

            Vector3i testPos = new Vector3i(pos.getX(), targetBlockY, pos.getY());
            Vector3i spawnPos = findOpenVerticalPosition(testPos, playerHeight);
            if (spawnPos != null) {
                return new Vector3f(spawnPos.getX(), spawnPos.getY() + playerHeight, spawnPos.getZ());
            }
        }
        return null;
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
        Spawner spawner = worldGenerator.getSpawner();
        World world = worldGenerator.getWorld();
        Vector3f spawnPosition = spawner.getSpawnPosition(world, entity);
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        loc.setWorldPosition(spawnPosition);
        loc.setLocalRotation(new Quat4f());
        entity.saveComponent(loc);

        if (worldProvider.isBlockRelevant(spawnPosition)) {
            spawnPlayer(entity);
        } else {
            updateRelevanceEntity(entity, ViewDistance.LEGALLY_BLIND.getChunkDistance());
            SpawningClientInfo info = new SpawningClientInfo(entity, spawnPosition);
            clientsPreparingToSpawn.add(info);
        }
    }

    @Command(value = "kill", shortDescription = "Reduce the player's health to zero", runOnServer = true,
            requiredPermission = PermissionManager.NO_PERMISSION)
    public void killCommand(@Sender EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            clientComp.character.send(new DestroyEvent(clientComp.character, EntityRef.NULL, EngineDamageTypes.DIRECT.get()));
        }
    }

    @Command(value = "teleport", shortDescription = "Teleports you to a location", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String teleportCommand(@Sender EntityRef sender, @CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        LocationComponent location = clientComp.character.getComponent(LocationComponent.class);
        if (location != null) {
            // deactivate the character to reset the CharacterPredictionSystem,
            // which would overwrite the character location
            clientComp.character.send(BeforeDeactivateComponent.newInstance());

            location.setWorldPosition(new Vector3f(x, y, z));
            clientComp.character.saveComponent(location);

            // re-active the character
            clientComp.character.send(OnActivatedComponent.newInstance());
        }
        return "Teleported to " + x + " " + y + " " + z;
    }


    /**
     * find a spot above the surface that is big enough for this character
     * @param spawnPos the position to check
     * @param height the height of the entity to spawn
     * @return the found spot or <code>null</code> if none was found
     */
    private Vector3i findOpenVerticalPosition(Vector3i spawnPos, float height) {
        int consecutiveAirBlocks = 0;
        Vector3i newSpawnPos = new Vector3i(spawnPos);

        // TODO: also start looking downwards if initial spawn pos is in the air
        for (int i = 1; i < 20; i++) {
            if (worldProvider.isBlockRelevant(newSpawnPos)) {
                if (worldProvider.getBlock(newSpawnPos) == BlockManager.getAir()) {
                    consecutiveAirBlocks++;
                } else {
                    consecutiveAirBlocks = 0;
                }

                if (consecutiveAirBlocks >= height) {
                    newSpawnPos.subY(consecutiveAirBlocks - 1);
                    return newSpawnPos;
                }
                newSpawnPos.add(0, 1, 0);
            }
        }

        return null;
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
