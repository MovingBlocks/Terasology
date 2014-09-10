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
package org.terasology.logic.console.internal;

import com.bulletphysics.linearmath.QuaternionUtil;
import com.google.common.base.Function;

import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Direction;
import org.terasology.network.ClientComponent;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.WorldDumper;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.nui.layers.mainMenu.WaitPopup;
import org.terasology.rendering.nui.skin.UISkinData;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.chunks.localChunkProvider.LocalChunkProvider;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * @author Immortius
 */
@RegisterSystem
public class CoreCommands extends BaseComponentSystem {

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
        pickupBuilder = new PickupBuilder(entityManager);
    }

    @Command(shortDescription = "Reloads a skin")
    public String reloadSkin(@CommandParam("skin") String skin) {
        AssetUri uri = new AssetUri(AssetType.UI_SKIN, skin);
        UISkinData uiSkinData = CoreRegistry.get(AssetManager.class).loadAssetData(uri, UISkinData.class);
        if (uiSkinData != null) {
            CoreRegistry.get(AssetManager.class).generateAsset(uri, uiSkinData);
            return "Success";
        } else {
            return "Unable to resolve skin '" + skin + "'";
        }
    }

    @Command(shortDescription = "Reloads a shader")
    public String reloadShader(@CommandParam("shader") String shader) {
        AssetUri uri = new AssetUri(AssetType.SHADER, shader);
        ShaderData shaderData = CoreRegistry.get(AssetManager.class).loadAssetData(uri, ShaderData.class);
        if (shaderData != null) {
            CoreRegistry.get(AssetManager.class).generateAsset(uri, shaderData);
            return "Success";
        } else {
            return "Unable to resolve shader '" + shader + "'";
        }
    }

    @Command(shortDescription = "Reloads a material")
    public String reloadMaterial(@CommandParam("material") String material) {
        AssetUri uri = new AssetUri(AssetType.MATERIAL, material);
        MaterialData materialData = CoreRegistry.get(AssetManager.class).loadAssetData(uri, MaterialData.class);
        if (materialData != null) {
            CoreRegistry.get(AssetManager.class).generateAsset(uri, materialData);
            return "Success";
        } else {
            return "Unable to resolve material '" + material + "'";
        }
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
            clientComp.character.send(new DestroyEvent(clientComp.character, EntityRef.NULL, EngineDamageTypes.DIRECT.get()));
        }
    }

    @Command(shortDescription = "Removes all entities of the given prefab", runOnServer = true)
    public void destroyEntitiesUsingPrefab(@CommandParam("prefabName") String prefabName) {
        Prefab prefab = entityManager.getPrefabManager().getPrefab(prefabName);
        if (prefab != null) {
            for (EntityRef entity : entityManager.getAllEntities()) {
                if (prefab.getURI().equals(entity.getPrefabURI())) {
                    entity.destroy();
                }
            }
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

    @Command(shortDescription = "Join a game using the default port " + TerasologyConstants.DEFAULT_PORT)
    public void join(@CommandParam("address") final String address) {
        join(address, TerasologyConstants.DEFAULT_PORT);
    }
    
    @Command(shortDescription = "Join a game")
    public void join(@CommandParam("address") final String address, @CommandParam("port") final int port) {

        Callable<JoinStatus> operation = new Callable<JoinStatus>() {

            @Override
            public JoinStatus call() throws InterruptedException {
                NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
                JoinStatus joinStatus = networkSystem.join(address, port);
                return joinStatus;
            }
        };

        final NUIManager manager = CoreRegistry.get(NUIManager.class);
        final WaitPopup<JoinStatus> popup = manager.pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.setMessage("Join Game", "Connecting to '" + address + ":" + port + "' - please wait ...");
        popup.onSuccess(new Function<JoinStatus, Void>() {
            
            @Override
            public Void apply(JoinStatus result) {
                GameEngine engine = CoreRegistry.get(GameEngine.class);
                if (result.getStatus() != JoinStatus.Status.FAILED) {
                    engine.changeState(new StateLoading(result));               
                } else {
                    MessagePopup screen = manager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                    screen.setMessage("Failed to Join", "Could not connect to server - " + result.getErrorMessage());
                }
                
                return null;
            }
        });
        popup.startOperation(operation, true);
    }

    @Command(shortDescription = "Leaves the current game and returns to main menu")
    public String leave() {
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        if (networkSystem.getMode() != NetworkMode.NONE) {
            CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu());
            return "Leaving..";
        } else {
            return "Not connected";
        }
    }
    
    @Command(shortDescription = "Displays debug information on the target entity")
    public String debugTarget() {
        EntityRef cameraTarget = cameraTargetSystem.getTarget();
        return cameraTarget.toFullDescription();
    }

    @Command(shortDescription = "Writes out information on all entities to a text file for debugging",
            helpText = "Writes entity information out into a file named \"entityDump.txt\".")
    public void dumpEntities() throws IOException {
        EngineEntityManager engineEntityManager = (EngineEntityManager) entityManager;
        PrefabSerializer prefabSerializer = new PrefabSerializer(engineEntityManager.getComponentLibrary(), engineEntityManager.getTypeSerializerLibrary());
        WorldDumper worldDumper = new WorldDumper(engineEntityManager, prefabSerializer);
        worldDumper.save(PathManager.getInstance().getHomePath().resolve("entityDump.txt"));
    }

    // TODO: Fix this up for multiplayer (cannot at the moment due to the use of the camera)
    @Command(shortDescription = "Spawns an instance of a prefab in the world")
    public String spawnPrefab(@CommandParam("prefabId") String prefabName, EntityRef entity) {
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

        Prefab prefab = Assets.getPrefab(prefabName);
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            entityManager.create(prefab, spawnPos, rotation);
            return "Done";
        } else if (prefab == null) {
            return "Unknown prefab";
        } else {
            return "Prefab cannot be spawned (no location component)";
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
