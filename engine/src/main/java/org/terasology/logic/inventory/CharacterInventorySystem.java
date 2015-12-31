/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.logic.inventory;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.binds.interaction.AttackButton;
import org.terasology.input.binds.inventory.DropItemButton;
import org.terasology.input.binds.inventory.ToolbarNextButton;
import org.terasology.input.binds.inventory.ToolbarPrevButton;
import org.terasology.input.binds.inventory.ToolbarSlotButton;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.ChangeHeldItemRequest;
import org.terasology.logic.characters.events.DropItemRequest;
import org.terasology.logic.inventory.events.ChangeSelectedInventorySlotRequest;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.hud.HudToolbar;
import org.terasology.rendering.world.WorldRenderer;

/**
 */
@RegisterSystem
public class CharacterInventorySystem extends BaseComponentSystem {

    @In
    private LocalPlayer localPlayer;

    @In
    private Time time;

    @In
    private NUIManager nuiManager;

    private long lastInteraction;
    private long lastTimeThrowInteraction;

    @ReceiveEvent
    public void syncSelectedSlotWithHeldItem(InventorySlotChangedEvent event, EntityRef entityRef,
                                             SelectedInventorySlotComponent selectedInventorySlotComponent) {
        if (selectedInventorySlotComponent.slot == event.getSlot()) {
            entityRef.send(new ChangeHeldItemRequest(event.getNewItem()));
        }
    }

    @ReceiveEvent
    public void onChangeSelectedInventorySlotRequested(ChangeSelectedInventorySlotRequest request, EntityRef character, SelectedInventorySlotComponent selectedInventorySlotComponent) {
        if (request.getSlot() >= 0 && request.getSlot() < 10 && request.getSlot() != selectedInventorySlotComponent.slot) {
            EntityRef newItem = InventoryUtils.getItemAt(character, request.getSlot());
            selectedInventorySlotComponent.slot = request.getSlot();
            character.saveComponent(selectedInventorySlotComponent);
            character.send(new ChangeHeldItemRequest(newItem));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onNextItem(ToolbarNextButton event, EntityRef entity, SelectedInventorySlotComponent selectedInventorySlotComponent) {
        int nextSlot = (selectedInventorySlotComponent.slot + 1) % 10;
        localPlayer.getCharacterEntity().send(new ChangeSelectedInventorySlotRequest(nextSlot));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onPrevItem(ToolbarPrevButton event, EntityRef entity, SelectedInventorySlotComponent selectedInventorySlotComponent) {
        int prevSlot = (selectedInventorySlotComponent.slot + 9) % 10;
        localPlayer.getCharacterEntity().send(new ChangeSelectedInventorySlotRequest(prevSlot));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onSlotButton(ToolbarSlotButton event, EntityRef entity) {
        localPlayer.getCharacterEntity().send(new ChangeSelectedInventorySlotRequest(event.getSlot()));
        event.consume();
    }


    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onAttackRequest(AttackButton event, EntityRef entity) {
        if (!event.isDown() || time.getGameTimeInMs() - lastInteraction < 200) {
            return;
        }

        CharacterHeldItemComponent characterHeldItemComponent = entity.getComponent(CharacterHeldItemComponent.class);
        EntityRef selectedItemEntity = characterHeldItemComponent.selectedItem;

        entity.send(new AttackRequest(selectedItemEntity));

        lastInteraction = time.getGameTimeInMs();
        characterHeldItemComponent.handAnimation = 0.5f;
        entity.saveComponent(characterHeldItemComponent);
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onDropItemRequest(DropItemButton event, EntityRef entity) {
        CharacterHeldItemComponent characterHeldItemComponent = entity.getComponent(CharacterHeldItemComponent.class);
        EntityRef selectedItemEntity = characterHeldItemComponent.selectedItem;

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

        HudToolbar toolbar = nuiManager.getHUD().getHUDElement("engine:toolbar", HudToolbar.class);
        if (toolbar != null) {
            toolbar.setChargeAmount(getDropPower());
        }

        float dropPower = getDropPower();
        //handle when we finally let go
        if (!event.isDown()) {
            // Compute new position
            dropPower *= 150f;

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

            characterHeldItemComponent.handAnimation = 0.5f;

            resetDropMark();
        }

        entity.saveComponent(characterHeldItemComponent);
        event.consume();


    }

    public void resetDropMark() {
        HudToolbar toolbar = nuiManager.getHUD().getHUDElement("engine:toolbar", HudToolbar.class);
        if (toolbar != null) {
            toolbar.setChargeAmount(0);
        }
        lastTimeThrowInteraction = 0;
    }

    private float getDropPower() {
        if (lastTimeThrowInteraction == 0) {
            return 0;
        }
        float dropPower = (time.getGameTimeInMs() - lastTimeThrowInteraction) / 1200f;
        return Math.min(1.0f, dropPower);
    }


}
