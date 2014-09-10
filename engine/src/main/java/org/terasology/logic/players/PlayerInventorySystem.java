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
import org.terasology.input.binds.inventory.UseItemButton;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.DropItemRequest;
import org.terasology.logic.characters.events.UseItemRequest;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.players.event.SelectItemRequest;
import org.terasology.logic.players.event.SelectedItemChangedEvent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.hud.HudToolbar;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem
public class PlayerInventorySystem extends BaseComponentSystem {

    @In
    private LocalPlayer localPlayer;

    @In
    private Time time;

    @In
    private CameraTargetSystem cameraTargetSystem;

    @In
    private WorldRenderer worldRenderer;

    @In
    private NUIManager nuiManager;

    private long lastInteraction;
    private long lastTimeThrowInteraction;

    @ReceiveEvent
    public void onSlotChangeRequested(SelectItemRequest request, EntityRef character, CharacterComponent characterComp) {
        if (request.getSlot() >= 0 && request.getSlot() < 10 && request.getSlot() != characterComp.selectedItem) {
            EntityRef oldItem = InventoryUtils.getItemAt(character, characterComp.selectedItem);
            EntityRef newItem = InventoryUtils.getItemAt(character, request.getSlot());
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

        EntityRef selectedItemEntity = InventoryUtils.getItemAt(entity, character.selectedItem);

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
        EntityRef selectedItemEntity = InventoryUtils.getItemAt(entity, character.selectedItem);

        entity.send(new AttackRequest(selectedItemEntity));

        lastInteraction = time.getGameTimeInMs();
        character.handAnimation = 0.5f;
        entity.saveComponent(character);
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onDropItemRequest(DropItemButton event, EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        EntityRef selectedItemEntity = InventoryUtils.getItemAt(entity, character.selectedItem);

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

            character.handAnimation = 0.5f;

            resetDropMark();
        }

        entity.saveComponent(character);
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
        float dropPower = (float) (time.getGameTimeInMs() - lastTimeThrowInteraction) / 1200f;
        return Math.min(1.0f, dropPower);
    }


}
