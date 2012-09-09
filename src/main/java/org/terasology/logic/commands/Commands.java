/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;
import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.SimpleAIComponent;
import org.terasology.components.rendering.MeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.input.InputSystem;
import org.terasology.input.binds.BackwardsButton;
import org.terasology.input.binds.ForwardsButton;
import org.terasology.input.binds.LeftStrafeButton;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.ChatManager;
import org.terasology.logic.manager.CommandManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.manager.CommandManager.Command;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPickupComponent;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;

/**
 * The controller class for all commands which can be executed through the in-game chat.
 * To add a command there needs to be an entry in the JSON file under "/data/console/commands.json" with a corresponding public method in this class.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class Commands implements CommandController {
    
    public void giveBlock(int blockId) {
        giveBlock(blockId, 16);
    }

    public void giveBlock(int blockId, int quantity) {
        BlockFamily blockFamily = BlockManager.getInstance().getBlock((byte) blockId).getBlockFamily();
        giveBlock(blockFamily, quantity);
    }

    public void giveBlock(String title) {
        giveBlock(title, 16);
    }

    public void giveBlock(String title, int quantity) {
        BlockFamily blockFamily = BlockManager.getInstance().getBlockFamily(title);
        giveBlock(blockFamily, quantity);
    }

    private void giveBlock(BlockFamily blockFamily, int quantity) {
        if (quantity < 1) return;

        BlockItemFactory factory = new BlockItemFactory(CoreRegistry.get(EntityManager.class));
        EntityRef item = factory.newInstance(blockFamily, quantity);

        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        playerEntity.send(new ReceiveItemEvent(item));
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp != null && !itemComp.container.exists()) {
            item.destroy();
        }
    }

    public void giveItem(String itemPrefabName) {
        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(itemPrefabName);
        System.out.println("Found prefab: " + prefab);
        if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
            EntityRef item = CoreRegistry.get(EntityManager.class).create(prefab);
            EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
            playerEntity.send(new ReceiveItemEvent(item));
            ItemComponent itemComp = item.getComponent(ItemComponent.class);
            if (itemComp != null && !itemComp.container.exists()) {
                item.destroy();
            }
        } else {
            giveBlock(itemPrefabName);
        }
    }
    
    public void health() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getEntity().getComponent(HealthComponent.class);
        health.currentHealth = health.maxHealth;
        localPlayer.getEntity().saveComponent(health);
    }
    
    public void health(int amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HealthComponent health = localPlayer.getEntity().getComponent(HealthComponent.class);
        health.currentHealth += amount;
        localPlayer.getEntity().saveComponent(health);
    }

    public void teleport(float x, float y, float z) {
        LocalPlayer player = CoreRegistry.get(LocalPlayer.class);
        if (player != null) {
            LocationComponent location = player.getEntity().getComponent(LocationComponent.class);
            if (location != null) {
                location.setWorldPosition(new Vector3f(x, y, z));
            }
        }
    }
    
    public void loadWorld(String worldName, String seed) {
        
    }

    public void dumpEntities() throws IOException {
        CoreRegistry.get(WorldPersister.class).save(new File(PathManager.getInstance().getDataPath(), "entityDump.txt"), WorldPersister.SaveFormat.JSON);
    }

    public void collision() {
        Config.getInstance().setDebugCollision(!Config.getInstance().isDebugCollision());
    }
    
    public void spawnLoot() {
        
    }

    public void bindKey(String key, String bind) {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.getKeyIndex(key), bind);
    }
    
    public void AZERTY() {
        InputSystem input = CoreRegistry.get(InputSystem.class);
        input.linkBindButtonToKey(Keyboard.KEY_Z, ForwardsButton.ID);
        input.linkBindButtonToKey(Keyboard.KEY_S, BackwardsButton.ID);
        input.linkBindButtonToKey(Keyboard.KEY_Q, LeftStrafeButton.ID);

    }
    
    public void spawnPrefab(String prefabName) {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(prefabName);
        if (prefab != null && prefab.getComponent(LocationComponent.class) != null) {
            CoreRegistry.get(EntityManager.class).create(prefab, spawnPos);
        }
    }
    
    public void destroyAI() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef ref : entityManager.iteratorEntities(SimpleAIComponent.class)) {
            ref.destroy();
        }
    }
    
    public void stepHeight(float amount) {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        CharacterMovementComponent comp = playerEntity.getComponent(CharacterMovementComponent.class);
        comp.stepHeight = amount;
    }
    
    public void spawnBlock(String blockName) {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        Vector3f spawnPos = camera.getPosition();
        Vector3f offset = camera.getViewingDirection();
        offset.scale(3);
        spawnPos.add(offset);

        Block block = BlockManager.getInstance().getBlock(blockName);
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
        }
    }

    public void sleigh() {
        LocalPlayer player = CoreRegistry.get(LocalPlayer.class);
        if (player != null) {
            CharacterMovementComponent moveComp = player.getEntity().getComponent(CharacterMovementComponent.class);
            if (moveComp.slopeFactor > 0.7f) {
                moveComp.slopeFactor = 0.6f;
            } else {
                moveComp.slopeFactor = 0.9f;
            }
        }
    }
    
    public void setSpawn() {
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        PlayerComponent spawn = playerEntity.getComponent(PlayerComponent.class);
        spawn.spawnPosition = playerEntity.getComponent(LocationComponent.class).getWorldPosition();
        playerEntity.saveComponent(spawn);
    }
    
    public void help() {
        List<Command> commands = CommandManager.getInstance().getCommandList();
        for (Command cmd : commands) {
            String msg = cmd.getName() + " - " + cmd.getShortDescription();
            ChatManager.getInstance().addMessage(msg);
        }
    }
    
    public void help(String command) {
        Command cmd = CommandManager.getInstance().getCommand(command);
        if (cmd == null) {
            ChatManager.getInstance().addMessage("No help available. Unknown command '" + command + "'");
        } else {
            String msg = cmd.getName() + " - " + cmd.getShortDescription();
            ChatManager.getInstance().addMessage(msg);
        }
    }
    
    public void exit() {
        CoreRegistry.get(GameEngine.class).shutdown();
    }
}
