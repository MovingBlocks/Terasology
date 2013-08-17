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

import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.binds.AttackButton;
import org.terasology.input.binds.DropItemButton;
import org.terasology.input.binds.ToolbarNextButton;
import org.terasology.input.binds.ToolbarPrevButton;
import org.terasology.input.binds.ToolbarSlotButton;
import org.terasology.input.binds.UseItemButton;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.DropItemRequest;
import org.terasology.logic.characters.events.UseItemRequest;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.players.event.SelectItemRequest;
import org.terasology.logic.players.event.SelectedItemChangedEvent;
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
    private Time time;

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

    @ReceiveEvent
    public void onSlotChangeRequested(SelectItemRequest request, EntityRef character, CharacterComponent characterComp) {
        if (request.getSlot() >= 0 && request.getSlot() < 10 && request.getSlot() != characterComp.selectedItem) {
            EntityRef oldItem = inventoryManager.getItemInSlot(character, characterComp.selectedItem);
            EntityRef newItem = inventoryManager.getItemInSlot(character, request.getSlot());
            characterComp.selectedItem = request.getSlot();
            character.saveComponent(characterComp);
            character.send(new SelectedItemChangedEvent(oldItem, newItem));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onNextItem(ToolbarNextButton event, EntityRef entity) {
        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        int nextSlot = (character.selectedItem + 1) % 10;
        localPlayer.getCharacterEntity().send(new SelectItemRequest(nextSlot));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onPrevItem(ToolbarPrevButton event, EntityRef entity) {
        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        int prevSlot = (character.selectedItem + 9) % 10;
        localPlayer.getCharacterEntity().send(new SelectItemRequest(prevSlot));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onSlotButton(ToolbarSlotButton event, EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        localPlayer.getCharacterEntity().send(new SelectItemRequest(event.getSlot()));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onUseItemButton(UseItemButton event, EntityRef entity) {
        if (!event.isDown() || time.getGameTimeInMs() - lastInteraction < 200) {
            return;
        }

        CharacterComponent character = entity.getComponent(CharacterComponent.class);

        EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, character.selectedItem);

        entity.send(new UseItemRequest(selectedItemEntity));

        lastInteraction = time.getGameTimeInMs();
        character.handAnimation = 0.5f;
        entity.saveComponent(character);
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onAttackRequest(AttackButton event, EntityRef entity) {
        if (!event.isDown() || time.getGameTimeInMs() - lastInteraction < 200) {
            return;
        }

        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, character.selectedItem);

        entity.send(new AttackRequest(selectedItemEntity));

        lastInteraction = time.getGameTimeInMs();
        character.handAnimation = 0.5f;
        entity.saveComponent(character);
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onDropItemRequest(DropItemButton event, EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, character.selectedItem);

        if (selectedItemEntity.equals(EntityRef.NULL)) {
            return;
        }
        //if this is our first time throwing, set the timer to something sensible, we can return since
        // this is a repeating event.
        if (event.isDown() && lastTimeThrowInteraction == 0) {
            lastTimeThrowInteraction = time.getGameTimeInMs();
            return;
        }

        //resize the crosshair
        UIImage crossHair = (UIImage) CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("crosshair");

        crossHair.setTextureSize(new Vector2f(22f, 22f));
        // compute drop power
        float dropPower = getDropPower();
        //update crosshair to show progress/power
        crossHair.setTextureOrigin(new Vector2f((46f + 22f * dropPower), 23f));

        //handle when we finally let go
        if (!event.isDown()) {
            // Compute new position
            dropPower *= 25f;

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

            character.handAnimation = 0.5f;

            resetDropMark();
        }

        entity.saveComponent(character);
        event.consume();


    }

    public void resetDropMark() {
        UIImage crossHair = (UIImage) CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("crosshair");
        lastTimeThrowInteraction = 0;
        crossHair.setTextureSize(new Vector2f(20f, 20f));
        crossHair.setTextureOrigin(new Vector2f(24f, 24f));
    }

    private float getDropPower() {
        if (lastTimeThrowInteraction == 0) {
            return 0;
        }
        float dropPower = (float) Math.floor((time.getGameTimeInMs() - lastTimeThrowInteraction) / 200);

        if (dropPower > 6) {
            dropPower = 6;
        }

        return dropPower;
    }


}
