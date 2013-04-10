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

import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.binds.*;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.DropItemRequest;
import org.terasology.logic.characters.events.UseItemRequest;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem
public class PlayerInventorySystem implements ComponentSystem {

    @In
    private LocalPlayer localPlayer;

    @In
    private Timer timer;

    @In
    private CameraTargetSystem cameraTargetSystem;

    @In
    private WorldRenderer worldRenderer;

    @In
    private SlotBasedInventoryManager inventoryManager;

    private long lastInteraction, lastTimeThrowInteraction;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onNextItem(ToolbarNextButton event, EntityRef entity) {
        LocalPlayerComponent localPlayerComp = localPlayer.getCharacterEntity().getComponent(LocalPlayerComponent.class);
        localPlayerComp.selectedTool = (localPlayerComp.selectedTool + 1) % 10;
        localPlayer.getCharacterEntity().saveComponent(localPlayerComp);
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onPrevItem(ToolbarPrevButton event, EntityRef entity) {
        LocalPlayerComponent localPlayerComp = localPlayer.getCharacterEntity().getComponent(LocalPlayerComponent.class);
        localPlayerComp.selectedTool = (localPlayerComp.selectedTool - 1) % 10;
        if (localPlayerComp.selectedTool < 0) {
            localPlayerComp.selectedTool = 10 + localPlayerComp.selectedTool;
        }
        localPlayer.getCharacterEntity().saveComponent(localPlayerComp);
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onSlotButton(ToolbarSlotButton event, EntityRef entity) {
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        localPlayerComp.selectedTool = event.getSlot();
        localPlayer.getCharacterEntity().saveComponent(localPlayerComp);
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, InventoryComponent.class})
    public void onUseItemButton(UseItemButton event, EntityRef entity) {
        if (!event.isDown() || timer.getTimeInMs() - lastInteraction < 200) {
            return;
        }

        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        if (localPlayerComp.isDead) {
            return;
        }

        EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, localPlayerComp.selectedTool);

        entity.send(new UseItemRequest(selectedItemEntity));

        lastInteraction = timer.getTimeInMs();
        localPlayerComp.handAnimation = 0.5f;
        entity.saveComponent(localPlayerComp);
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, InventoryComponent.class})
    public void onAttackRequest(AttackButton event, EntityRef entity) {
        if (!event.isDown() || timer.getTimeInMs() - lastInteraction < 200) {
            return;
        }

        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        if (localPlayerComp.isDead) {
            return;
        }

        EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, localPlayerComp.selectedTool);

        entity.send(new AttackRequest(selectedItemEntity));

        lastInteraction = timer.getTimeInMs();
        localPlayerComp.handAnimation = 0.5f;
        entity.saveComponent(localPlayerComp);
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, InventoryComponent.class})
    public void onDropItemRequest(DropItemButton event, EntityRef entity) {
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, localPlayerComp.selectedTool);

        if (selectedItemEntity.equals(EntityRef.NULL)) {
            return;
        }
        //if this is our first time throwing, set the timer to something sensible, we can return since
        // this is a repeating event.
        if (event.isDown() && lastTimeThrowInteraction == 0) {
            lastTimeThrowInteraction = timer.getTimeInMs();
            return;
        }

        if (localPlayerComp.isDead) {
            return;
        }
        //resize the crosshair
        UIImage crossHair = (UIImage) CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("crosshair");

        crossHair.setTextureSize(new Vector2f(22f / 256f, 22f / 256f));
        // compute drop power
        float dropPower = getDropPower();
        //update crosshair to show progress/power
        crossHair.setTextureOrigin(new Vector2f((46f + 22f * dropPower) / 256f, 23f / 256f));

        //handle when we finally let go
        if (!event.isDown()) {
            // Compute new position
            dropPower *= 25f;
            ItemComponent item = selectedItemEntity.getComponent(ItemComponent.class);

            // TODO: This will change when camera are handled better (via a component)
            Camera playerCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

            Vector3f newPosition = new Vector3f(playerCamera.getPosition().x + playerCamera.getViewingDirection().x * 1.5f,
                    playerCamera.getPosition().y + playerCamera.getViewingDirection().y * 1.5f,
                    playerCamera.getPosition().z + playerCamera.getViewingDirection().z * 1.5f
            );

            //send DropItemRequest
            Vector3f impulseVector = new Vector3f(playerCamera.getViewingDirection());
            impulseVector.scale(dropPower);
            entity.send(new DropItemRequest(selectedItemEntity, entity,
                    impulseVector,
                    newPosition));

            localPlayerComp.handAnimation = 0.5f;

            resetDropMark();
        }

        entity.saveComponent(localPlayerComp);
        event.consume();


    }

    public void resetDropMark() {
        UIImage crossHair = (UIImage) CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("crosshair");
        lastTimeThrowInteraction = 0;
        crossHair.setTextureSize(new Vector2f(20f / 256f, 20f / 256f));
        crossHair.setTextureOrigin(new Vector2f(24f / 256f, 24f / 256f));
    }

    private float getDropPower() {
        if (lastTimeThrowInteraction == 0) {
            return 0;
        }
        float dropPower = (float) Math.floor((timer.getTimeInMs() - lastTimeThrowInteraction) / 200);

        if (dropPower > 6) {
            dropPower = 6;
        }

        return dropPower;
    }


}
