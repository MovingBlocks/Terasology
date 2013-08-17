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
package org.terasology.logic.console;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.CameraTargetSystem;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.health.NoHealthEvent;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Direction;
import org.terasology.network.ClientComponent;
import org.terasology.persistence.WorldDumper;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.localChunkProvider.LocalChunkProvider;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.IOException;

/**
 * @author Immortius
 */
@RegisterSystem
public class CoreCommands implements ComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private CameraTargetSystem cameraTargetSystem;

    @In
    private WorldRenderer worldRenderer;

    @In
    private PrefabManager prefabManager;

    @In
    private BlockManager blockManager;

    private PickupBuilder pickupBuilder;

    @Override
    public void initialise() {
        pickupBuilder = new PickupBuilder();
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Toggles Fullscreen Mode")
    public String fullscreen() {
        TerasologyEngine te = (TerasologyEngine) CoreRegistry.get(GameEngine.class);

        te.setFullscreen(!te.isFullscreen());

        if (te.isFullscreen()) {
            return "Switched to fullscreen mode";
        } else {
            return "Switched to windowed mode";
        }

    }

    @Command(shortDescription = "Forces the chunk manager to purge unused chunks")
    public void clearChunkCache() {
        ((LocalChunkProvider) worldRenderer.getChunkProvider()).requestCleanup();
    }

    @Command(shortDescription = "Reduce the player's health to zero", runOnServer = true)
    public void kill(EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        HealthComponent health = clientComp.character.getComponent(HealthComponent.class);
        if (health != null) {
            clientComp.character.send(new NoHealthEvent(clientComp.character, EngineDamageTypes.DIRECT.get()));
        }
    }

    @Command(shortDescription = "Teleports you to a location", runOnServer = true)
    public void teleport(@CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z, EntityRef client) {
        ClientComponent clientComp = client.getComponent(ClientComponent.class);
        LocationComponent location = clientComp.character.getComponent(LocationComponent.class);
        if (location != null) {
            location.setWorldPosition(new Vector3f(x, y, z));
            clientComp.character.saveComponent(location);
        }
    }

    @Command(shortDescription = "Exits the game")
    public void exit() {
        CoreRegistry.get(GameEngine.class).shutdown();
    }

    @Command(shortDescription = "Displays debug information on the target entity")
    public String debugTarget() {
        EntityRef cameraTarget = cameraTargetSystem.getTarget();
        return cameraTarget.toFullDescription();
    }

    @Command(shortDescription = "Writes out information on all entities to a text file for debugging",
            helpText = "Writes entity information out into a file named \"entityDump.txt\".")
    public void dumpEntities() throws IOException {
        WorldDumper worldDumper = new WorldDumper((EngineEntityManager) entityManager);
        worldDumper.save(PathManager.getInstance().getHomePath().resolve("entityDump.txt"));
    }

    // TODO: Fix this up for multiplayer (cannot at the moment due to the use of the camera)
    @Command(shortDescription = "Spawns an instance of a prefab in the world")
    public void spawnPrefab(@CommandParam("prefabId") String prefabName, EntityRef entity) {
        Camera camera = worldRenderer.getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = new Vector3f(camera.getViewingDirection());
        offset.scale(2);
        spawnPos.add(offset);
        Vector3f dir = new Vector3f(camera.getViewingDirection());
        dir.y = 0;
        if (dir.lengthSquared() > 0.001f) {
            dir.normalize();
        } else {
            dir.set(Direction.FORWARD.getVector3f());
        }
        Quat4f rotation = QuaternionUtil.shortestArcQuat(Direction.FORWARD.getVector3f(), dir, new Quat4f());

        Prefab prefab = prefabManager.getPrefab(prefabName);
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            entityManager.create(prefab, spawnPos, rotation);
        }
    }

    // TODO: Fix this up for multiplayer (cannot at the moment due to the use of the camera)
    @Command(shortDescription = "Spawns a block in front of the player", helpText = "Spawns the specified block as a " +
            "item in front of the player. You can simply pick it up.")
    public String spawnBlock(@CommandParam("blockName") String blockName) {
        Camera camera = worldRenderer.getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        BlockFamily block = blockManager.getBlockFamily(blockName);
        if (block == null) {
            return "";
        }

        BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
        EntityRef blockItem = blockItemFactory.newInstance(block);

        pickupBuilder.createPickupFor(blockItem, spawnPos, 60);
        return "Spawned block.";
    }

}
