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

package org.terasology.logic.commands;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.lwjgl.input.Keyboard;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.logic.health.HealthComponent;
import org.terasology.components.HierarchicalAIComponent;
import org.terasology.components.SimpleAIComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.events.FullHealthEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.TerasologyEngine;
import org.terasology.input.InputSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.console.SimpleMessageEvent;
import org.terasology.logic.manager.CommandManager;
import org.terasology.logic.manager.CommandManager.CommandInfo;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Direction;
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
public class Commands implements CommandProvider {

    //TODO  Add multiplayer commands, when ready for that
    //TODO  change develop commands so that they can be run only by admins
    //==============================
    //          Commands
    //==============================

    @Command(shortDescription = "Toggle muting all sound")
    public void toggleMute() {
        AudioManager audioManager = CoreRegistry.get(AudioManager.class);
        audioManager.setMute(!audioManager.isMute());
        CoreRegistry.get(LocalPlayer.class).getClientEntity().send(new SimpleMessageEvent("All sound is now " + ((audioManager.isMute()) ? "muted." : "unmuted.")));
    }

    @Command(shortDescription = "Plays a test sound")
    public void playTestSound(@CommandParam(name = "xOffset") float xOffset, @CommandParam(name = "yOffset") float zOffset) {
        AudioManager audioManager = CoreRegistry.get(AudioManager.class);
        Vector3f position = new Vector3f(CoreRegistry.get(LocalPlayer.class).getPosition());
        position.x += xOffset;
        position.z += zOffset;
        audioManager.playSound(Assets.getSound("engine:dig"), position);
    }

    @Command(shortDescription = "Restores your health to max")
    public void health() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        health.currentHealth = health.maxHealth;
        localPlayer.getCharacterEntity().send(new FullHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
        localPlayer.getCharacterEntity().saveComponent(health);
    }

    @Command(shortDescription = "Restores your health by an amount")
    public void health(@CommandParam(name = "amount") int amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
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
    public void setMaxHealth(@CommandParam(name = "max") int max) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        health.maxHealth = max;
        health.currentHealth = health.maxHealth;
        localPlayer.getCharacterEntity().send(new FullHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
        localPlayer.getCharacterEntity().saveComponent(health);
    }

    @Command(shortDescription = "Set regen rate")
    public void setRegenRaterate(@CommandParam(name = "rate") float rate) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        health.regenRate = rate;
        localPlayer.getCharacterEntity().saveComponent(health);
    }

    @Command(shortDescription = "Show your health")
    public void showHealth() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        MessageManager.getInstance().addMessage("Your health:" + health.currentHealth + " max:" + health.maxHealth + " regen:" + health.regenRate + " partRegen:" + health.partialRegen);
    }

    @Command(shortDescription = "Set ground friction")
    public void setGroundFriction(@CommandParam(name = "amount") float amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.groundFriction = amount;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Set max ground speed", helpText = "Set maxGroundSpeed")
    public void setMaxGroundSpeed(@CommandParam(name = "amount") float amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.maxGroundSpeed = amount;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Set max ghost speed")
    public void setMaxGhostSpeed(@CommandParam(name = "amount") float amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.maxGhostSpeed = amount;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Set jump speed")
    public void setJumpSpeed(@CommandParam(name = "amount") float amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.jumpSpeed = amount;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Show your Movement stats")
    public void showMovement() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        MessageManager.getInstance().addMessage("Your groundFriction:" + move.groundFriction + " maxGroudspeed:" + move.maxGroundSpeed + " JumpSpeed:"
                + move.jumpSpeed + " maxWaterSpeed:" + move.maxWaterSpeed + " maxGhostSpeed:" + move.maxGhostSpeed + " SlopeFactor:"
                + move.slopeFactor + " runFactor:" + move.runFactor);
    }

    @Command(shortDescription = "Go really fast")
    public void hspeed() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        CharacterMovementComponent move = localPlayer.getCharacterEntity().getComponent(CharacterMovementComponent.class);
        move.maxGhostSpeed = 50f;
        move.jumpSpeed = 24f;
        move.maxGroundSpeed = 20f;
        move.maxWaterSpeed = 12f;
        localPlayer.getCharacterEntity().saveComponent(move);
    }

    @Command(shortDescription = "Jump really high")
    public void hjump() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
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
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
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
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
        localPlayer.getCharacterEntity().send(new NoHealthEvent(localPlayer.getCharacterEntity(), health.maxHealth));
    }

    @Command(shortDescription = "Reduce the player's health by an amount")
    public void damage(@CommandParam(name = "amount") int amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
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
    public void teleport(@CommandParam(name = "x") float x, @CommandParam(name = "y") float y, @CommandParam(name = "z") float z) {
        LocalPlayer player = CoreRegistry.get(LocalPlayer.class);
        if (player != null) {
            LocationComponent location = player.getCharacterEntity().getComponent(LocationComponent.class);
            if (location != null) {
                location.setWorldPosition(new Vector3f(x, y, z));
            }
        }
    }

    @Command(shortDescription = "Writes out information on all entities to a text file for debugging",
            helpText = "Writes entity information out into a file named \"entityDump.txt\".")
    public void dumpEntities() throws IOException {
        WorldPersister worldPersister = new WorldPersister(CoreRegistry.get(EntityManager.class));
        worldPersister.save(new File(PathManager.getInstance().getDataPath(), "entityDump.txt"), WorldPersister.SaveFormat.JSON);
    }

    @Command(shortDescription = "Maps a key to a function")
    public void bindKey(@CommandParam(name = "key") String key, @CommandParam(name = "function") String bind) {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.getKeyIndex(key), bind);
        StringBuilder builder = new StringBuilder();
        builder.append("Mapped ").append(Keyboard.getKeyName(Keyboard.getKeyIndex(key))).append(" to action ");
        builder.append(bind);
        MessageManager.getInstance().addMessage(builder.toString());
    }

    @Command(shortDescription = "Switches to typical key binds for AZERTY")
    public void AZERTY() {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.KEY_Z, "engine:forwards");
        input.linkBindButtonToKey(Keyboard.KEY_S, "engine:backwards");
        input.linkBindButtonToKey(Keyboard.KEY_Q, "engine:left");

        MessageManager.getInstance().addMessage("Changed key bindings to AZERTY keyboard layout.");
    }

    @Command(shortDescription = "Switches to typical key binds for NEO 2 keyboard layout")
    public void NEO() {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.KEY_V, "engine:forwards");
        input.linkBindButtonToKey(Keyboard.KEY_I, "engine:backwards");
        input.linkBindButtonToKey(Keyboard.KEY_U, "engine:left");
        input.linkBindButtonToKey(Keyboard.KEY_A, "engine:right");
        input.linkBindButtonToKey(Keyboard.KEY_L, "engine:useItem");
        input.linkBindButtonToKey(Keyboard.KEY_G, "engine:inventory");

        MessageManager.getInstance().addMessage("Changed key bindings to NEO 2 keyboard layout.");
    }

    @Command(shortDescription = "Spawns an instance of a prefab in the world")
    public void spawnPrefab(@CommandParam(name = "prefabId") String prefabName) {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
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

        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(prefabName);
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            CoreRegistry.get(EntityManager.class).create(prefab, spawnPos, rotation);
        }
    }

    @Command(shortDescription = "Destroys all AIs in the world")
    public void destroyAI() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        int i = 0;
        for (EntityRef ref : entityManager.iteratorEntities(SimpleAIComponent.class)) {
            ref.destroy();
            i++;
        }
        MessageManager.getInstance().addMessage("Simple AIs (" + i + ") Destroyed ");
        i = 0;
        for (EntityRef ref : entityManager.iteratorEntities(HierarchicalAIComponent.class)) {
            ref.destroy();
            i++;
        }
        MessageManager.getInstance().addMessage("Hierarchical AIs (" + i + ") Destroyed ");
    }

    @Command(shortDescription = "Count all AIs in the world")
    public void countAI() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        int i = 0;
        for (EntityRef ref : entityManager.iteratorEntities(SimpleAIComponent.class)) {
            i++;
        }
        MessageManager.getInstance().addMessage("Simple AIs: " + i);
        i = 0;
        for (EntityRef ref : entityManager.iteratorEntities(HierarchicalAIComponent.class)) {
            i++;
        }
        MessageManager.getInstance().addMessage("Hierarchical AIs: " + i);
    }

    @Command(shortDescription = "Sets the height the player can step up")
    public void stepHeight(@CommandParam(name = "height") float amount) {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        CharacterMovementComponent comp = playerEntity.getComponent(CharacterMovementComponent.class);
        comp.stepHeight = amount;
    }

    @Command(shortDescription = "Spawns a block in front of the player", helpText = "Spawns the specified block as a " +
            "item in front of the player. You can simply pick it up.")
    public void spawnBlock(@CommandParam(name = "blockName") String blockName) {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        Block block = blockManager.getBlock(blockName);
        if (block == null) return;

        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab("core:droppedBlock");
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            EntityRef blockEntity = CoreRegistry.get(EntityManager.class).create(prefab, spawnPos);
            MeshComponent blockMesh = blockEntity.getComponent(MeshComponent.class);
            BlockPickupComponent blockPickup = blockEntity.getComponent(BlockPickupComponent.class);
            blockPickup.blockFamily = block.getBlockFamily();
            blockMesh.mesh = block.getMesh();
            blockEntity.saveComponent(blockMesh);
            blockEntity.saveComponent(blockPickup);

            MessageManager.getInstance().addMessage("Spawned block.");
        }
    }

    @Command(shortDescription = "Toggles the maximum slope the player can walk up")
    public void sleigh() {
        LocalPlayer player = CoreRegistry.get(LocalPlayer.class);
        if (player != null) {
            CharacterMovementComponent moveComp = player.getCharacterEntity().getComponent(CharacterMovementComponent.class);
            if (moveComp.slopeFactor > 0.7f) {
                moveComp.slopeFactor = 0.6f;
            } else {
                moveComp.slopeFactor = 0.9f;
            }
            MessageManager.getInstance().addMessage("Slope factor is now " + moveComp.slopeFactor);
        }
    }

    @Command(shortDescription = "Sets the spawn position of the player")
    public void setSpawn() {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        CharacterComponent spawn = playerEntity.getComponent(CharacterComponent.class);
        spawn.spawnPosition = playerEntity.getComponent(LocationComponent.class).getWorldPosition();
        playerEntity.saveComponent(spawn);
    }

    @Command(shortDescription = "General help", helpText = "Prints out short descriptions for all available commands.")
    public void help() {
        StringBuilder msg = new StringBuilder();
        List<CommandInfo> commands = CoreRegistry.get(CommandManager.class).getCommandList();
        for (CommandInfo cmd : commands) {
            if (!msg.toString().isEmpty()) {
                msg.append("\n");
            }
            msg.append(cmd.getUsageMessage()).append(" - ").append(cmd.getShortDescription());
        }
        MessageManager.getInstance().addMessage(msg.toString());
    }

    @Command(shortDescription = "Detailed help on a command")
    public void help(@CommandParam(name = "command") String command) {
        Collection<CommandInfo> cmdCollection = CoreRegistry.get(CommandManager.class).getCommand(command);
        if (cmdCollection.isEmpty()) {
            MessageManager.getInstance().addMessage("No help available for command '" + command + "'. Unknown command.");
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
            MessageManager.getInstance().addMessage(msg.toString());
        }
    }

    @Command(shortDescription = "Exits the game")
    public void exit() {
        CoreRegistry.get(GameEngine.class).shutdown();
    }

    @Command(shortDescription = "Fullscreen mode")
    public void fullscreen() {
        TerasologyEngine te = (TerasologyEngine) CoreRegistry.get(GameEngine.class);

        if (te.isFullscreen()) {
            MessageManager.getInstance().addMessage("returning to desktop size");
        } else {
            MessageManager.getInstance().addMessage("switching to fullscreen mode");
        }
        te.setFullscreen(te.isFullscreen());
    }
}
