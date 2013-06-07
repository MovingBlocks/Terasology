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

package org.terasology.logic.console;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.lwjgl.input.Keyboard;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.InputSystem;
import org.terasology.logic.ai.HierarchicalAIComponent;
import org.terasology.logic.ai.SimpleAIComponent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.health.FullHealthEvent;
import org.terasology.logic.health.HealthChangedEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.health.NoHealthEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Direction;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.StringConstants;
import org.terasology.world.block.Block;
import org.terasology.world.block.entity.BlockPickupComponent;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * The controller class for all commands which can be executed through the in-game chat. To add a command there needs to be a public method
 * in this class.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @author Tobias 'skaldarnar' Nett <skaldarnar@googlemail.com>
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
@RegisterSystem
public class EngineCommands implements ComponentSystem {

    @In
    private LocalPlayer localPlayer;

    @In
    private InputSystem inputSystem;

    @In
    private PrefabManager prefabManager;

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private BlockManager blockManager;

    @In
    private Console console;


    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    //TODO  Add multiplayer commands, when ready for that
    //TODO  change develop commands so that they can be run only by admins
    //==============================
    //          Commands
    //==============================


    @Command(shortDescription = "Restores your health to max", runOnServer = true)
    public void health(EntityRef clientEntity) {
        ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
        if (clientComp != null) {
            EntityRef character = clientComp.character;
            HealthComponent health = character.getComponent(HealthComponent.class);
            if (health != null) {
                health.currentHealth = health.maxHealth;
                character.send(new FullHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
                character.saveComponent(health);
            }
        }
    }

    @Command(shortDescription = "Restores your health by an amount")
    public void health(@CommandParam("amount") int amount) {
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        health.currentHealth = amount;
        if (health.currentHealth >= health.maxHealth) {
            health.currentHealth = health.maxHealth;
            localPlayer.getCharacterEntity().send(new FullHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
        } else if (health.currentHealth <= 0) {
            health.currentHealth = 0;
            localPlayer.getCharacterEntity().send(new NoHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
        } else {
            localPlayer.getCharacterEntity().send(new HealthChangedEvent(localPlayer.getCharacterEntity(), health.currentHealth, health.maxHealth));
        }

        localPlayer.getCharacterEntity().saveComponent(health);
    }

    @Command(shortDescription = "Set max health")
    public void setMaxHealth(@CommandParam("max") int max) {
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        health.maxHealth = max;
        health.currentHealth = health.maxHealth;
        localPlayer.getCharacterEntity().send(new FullHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
        localPlayer.getCharacterEntity().saveComponent(health);
    }

    @Command(shortDescription = "Set regen rate")
    public void setRegenRaterate(@CommandParam("rate") float rate) {
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        health.regenRate = rate;
        localPlayer.getCharacterEntity().saveComponent(health);
    }

    @Command(shortDescription = "Show your health")
    public String showHealth() {
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        return "Your health:" + health.currentHealth + " max:" + health.maxHealth + " regen:" + health.regenRate + " partRegen:" + health.partialRegen;
    }

    @Command(shortDescription = "Set ground friction")
    public void setGroundFriction(@CommandParam("amount") float amount) {
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.groundFriction = amount;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Set max ground speed", helpText = "Set maxGroundSpeed")
    public void setMaxGroundSpeed(@CommandParam("amount") float amount) {
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.maxGroundSpeed = amount;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Set max ghost speed")
    public void setMaxGhostSpeed(@CommandParam("amount") float amount) {
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.maxGhostSpeed = amount;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Set jump speed")
    public void setJumpSpeed(@CommandParam("amount") float amount) {
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.jumpSpeed = amount;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Show your Movement stats")
    public String showMovement() {
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        return "Your groundFriction:" + move.groundFriction + " maxGroudspeed:" + move.maxGroundSpeed + " JumpSpeed:"
                + move.jumpSpeed + " maxWaterSpeed:" + move.maxWaterSpeed + " maxGhostSpeed:" + move.maxGhostSpeed + " SlopeFactor:"
                + move.slopeFactor + " runFactor:" + move.runFactor;
    }

    @Command(shortDescription = "Go really fast")
    public void hspeed() {
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.maxGhostSpeed = 50f;
        move.jumpSpeed = 24f;
        move.maxGroundSpeed = 20f;
        move.maxWaterSpeed = 12f;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Jump really high")
    public void hjump() {
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.jumpSpeed = 75f;
        health.fallingDamageSpeedThreshold = 85f;
        health.excessSpeedDamageMultiplier = 2f;
        localPlayer.getCharacterEntity().saveComponent(health);
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Restore normal speed values")
    public void restoreSpeed() {
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.maxGhostSpeed = 3f;
        move.jumpSpeed = 12f;
        move.maxGroundSpeed = 5f;
        move.maxWaterSpeed = 2f;
        move.runFactor = 1.5f;
        move.stepHeight = 0.35f;
        move.slopeFactor = 0.6f;
        move.groundFriction = 8.0f;
        move.distanceBetweenFootsteps = 1f;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Reduce the player's health to zero")
    public void kill() {
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        localPlayer.getCharacterEntity().send(new NoHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
    }

    @Command(shortDescription = "Reduce the player's health by an amount")
    public void damage(@CommandParam("amount") int amount) {
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        health.currentHealth -= amount;
        if (health.currentHealth >= health.maxHealth) {
            health.currentHealth = health.maxHealth;
            localPlayer.getCharacterEntity().send(new FullHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
        } else if (health.currentHealth <= 0) {
            health.currentHealth = 0;
            localPlayer.getCharacterEntity().send(new NoHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
        } else {
            localPlayer.getCharacterEntity().send(new HealthChangedEvent(localPlayer.getCharacterEntity(), health.currentHealth, health.maxHealth));
        }

        localPlayer.getCharacterEntity().saveComponent(health);
    }

    @Command(shortDescription = "Teleports you to a location")
    public void teleport(@CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        if (localPlayer != null) {
            LocationComponent location = localPlayer.getCharacterEntity().getComponent(LocationComponent.class);
            if (location != null) {
                location.setWorldPosition(new Vector3f(x, y, z));
            }
        }
    }

    @Command(shortDescription = "Writes out information on all entities to a text file for debugging",
            helpText = "Writes entity information out into a file named \"entityDump.txt\".")
    public void dumpEntities() throws IOException {
        WorldPersister worldPersister = new WorldPersister((EngineEntityManager) entityManager);
        worldPersister.save(new File(PathManager.getInstance().getHomePath(), "entityDump.txt"), WorldPersister.SaveFormat.JSON);
    }

    @Command(shortDescription = "Maps a key to a function")
    public String bindKey(@CommandParam("key") String key, @CommandParam("function") String bind) {
        inputSystem.linkBindButtonToKey(Keyboard.getKeyIndex(key), bind);
        StringBuilder builder = new StringBuilder();
        builder.append("Mapped ").append(Keyboard.getKeyName(Keyboard.getKeyIndex(key))).append(" to action ");
        builder.append(bind);
        return builder.toString();
    }

    @Command(shortDescription = "Switches to typical key binds for AZERTY")
    public String AZERTY() {
        inputSystem.linkBindButtonToKey(Keyboard.KEY_Z, "engine:forwards");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_S, "engine:backwards");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_Q, "engine:left");

        return "Changed key bindings to AZERTY keyboard layout.";
    }

    @Command(shortDescription = "Switches to typical key binds for NEO 2 keyboard layout")
    public String NEO() {
        inputSystem.linkBindButtonToKey(Keyboard.KEY_V, "engine:forwards");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_I, "engine:backwards");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_U, "engine:left");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_A, "engine:right");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_L, "engine:useItem");
        inputSystem.linkBindButtonToKey(Keyboard.KEY_G, "engine:inventory");

        return "Changed key bindings to NEO 2 keyboard layout.";
    }

    @Command(shortDescription = "Spawns an instance of a prefab in the world")
    public void spawnPrefab(@CommandParam("prefabId") String prefabName) {
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

    @Command(shortDescription = "Destroys all AIs in the world")
    public String destroyAI() {
        int simpleAI = 0;
        for (EntityRef ref : entityManager.listEntitiesWith(SimpleAIComponent.class)) {
            ref.destroy();
            simpleAI++;
        }
        int hierarchicalAI = 0;
        for (EntityRef ref : entityManager.listEntitiesWith(HierarchicalAIComponent.class)) {
            ref.destroy();
            hierarchicalAI++;
        }
        return "Simple AIs (" + simpleAI + ") Destroyed, Hierarchical AIs (" + hierarchicalAI + ") Destroyed ";
    }

    @Command(shortDescription = "Count all AIs in the world")
    public String countAI() {
        int simpleAIs = 0;
        for (EntityRef ref : entityManager.listEntitiesWith(SimpleAIComponent.class)) {
            simpleAIs++;
        }
        int hierarchical = 0;
        for (EntityRef ref : entityManager.listEntitiesWith(HierarchicalAIComponent.class)) {
            hierarchical++;
        }
        return "Simple AIs: " + simpleAIs + ", Hierarchical AIs: " + hierarchical;
    }

    @Command(shortDescription = "Sets the height the player can step up")
    public void stepHeight(@CommandParam("height") float amount) {
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        CharacterMovementComponent comp = playerEntity.getComponent(CharacterMovementComponent.class);
        comp.stepHeight = amount;
    }

    @Command(shortDescription = "Spawns a block in front of the player", helpText = "Spawns the specified block as a " +
            "item in front of the player. You can simply pick it up.")
    public String spawnBlock(@CommandParam("blockName") String blockName) {
        Camera camera = worldRenderer.getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        Block block = blockManager.getBlock(blockName);
        if (block == null) {
            return "";
        }

        Prefab prefab = prefabManager.getPrefab("core:droppedBlock");
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            EntityRef blockEntity = entityManager.create(prefab, spawnPos);
            MeshComponent blockMesh = blockEntity.getComponent(MeshComponent.class);
            BlockPickupComponent blockPickup = blockEntity.getComponent(BlockPickupComponent.class);
            blockPickup.blockFamily = block.getBlockFamily();
            blockMesh.mesh = block.getMesh();
            blockEntity.saveComponent(blockMesh);
            blockEntity.saveComponent(blockPickup);

            return "Spawned block.";
        }
        return "";
    }

    @Command(shortDescription = "Toggles the maximum slope the player can walk up")
    public String sleigh() {
        CharacterMovementComponent moveComp = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        if (moveComp.slopeFactor > 0.7f) {
            moveComp.slopeFactor = 0.6f;
        } else {
            moveComp.slopeFactor = 0.9f;
        }
        return "Slope factor is now " + moveComp.slopeFactor;
    }

    @Command(shortDescription = "Sets the spawn position of the player")
    public void setSpawn() {
        EntityRef playerEntity = localPlayer.getCharacterEntity();
        CharacterComponent spawn = playerEntity.getComponent(CharacterComponent.class);
        spawn.spawnPosition = playerEntity.getComponent(LocationComponent.class).getWorldPosition();
        playerEntity.saveComponent(spawn);
    }

    @Command(shortDescription = "General help", helpText = "Prints out short descriptions for all available commands.")
    public String help() {
        StringBuilder msg = new StringBuilder();
        List<CommandInfo> commands = console.getCommandList();
        for (CommandInfo cmd : commands) {
            if (!msg.toString().isEmpty()) {
                msg.append("\n");
            }
            msg.append(cmd.getUsageMessage()).append(" - ").append(cmd.getShortDescription());
        }
        return msg.toString();
    }

    @Command(shortDescription = "Detailed help on a command")
    public String help(@CommandParam("command") String command) {
        Collection<CommandInfo> cmdCollection = console.getCommand(command);
        if (cmdCollection.isEmpty()) {
            return "No help available for command '" + command + "'. Unknown command.";
        } else {
            StringBuilder msg = new StringBuilder();

            for (CommandInfo cmd : cmdCollection) {
                msg.append("=====================================================================================================================");
                msg.append(StringConstants.NEW_LINE);
                msg.append(cmd.getUsageMessage());
                msg.append(StringConstants.NEW_LINE);
                msg.append("=====================================================================================================================");
                msg.append(StringConstants.NEW_LINE);
                if (!cmd.getHelpText().isEmpty()) {
                    msg.append(cmd.getHelpText());
                    msg.append(StringConstants.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(StringConstants.NEW_LINE);
                } else if (!cmd.getShortDescription().isEmpty()) {
                    msg.append(cmd.getShortDescription());
                    msg.append(StringConstants.NEW_LINE);
                    msg.append("=====================================================================================================================");
                    msg.append(StringConstants.NEW_LINE);
                }
                msg.append(StringConstants.NEW_LINE);
            }
            return msg.toString();
        }
    }

    @Command(shortDescription = "Exits the game")
    public void exit() {
        CoreRegistry.get(GameEngine.class).shutdown();
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

}
