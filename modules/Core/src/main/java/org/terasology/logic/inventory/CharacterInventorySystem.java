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

import org.terasology.utilities.Assets;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.binds.inventory.DropItemButton;
import org.terasology.input.binds.inventory.ToolbarNextButton;
import org.terasology.input.binds.inventory.ToolbarPrevButton;
import org.terasology.input.binds.inventory.ToolbarSlotButton;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.events.ChangeHeldItemRequest;
import org.terasology.logic.inventory.events.ChangeSelectedInventorySlotRequest;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.hud.InventoryHud;

@RegisterSystem
public class CharacterInventorySystem extends BaseComponentSystem {

    @In
    private LocalPlayer localPlayer;

    @In
    private Time time;

    @In
    private NUIManager nuiManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    private long lastInteraction;
    private long lastTimeThrowInteraction;

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void ensureTransferSlotIsCreated(OnAddedComponent event, EntityRef entityRef, CharacterComponent characterComponent) {
        EntityRef transferSlot = entityManager.create("core:transferSlot");
        characterComponent.movingItem = transferSlot;
        entityRef.saveComponent(characterComponent);
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void syncSelectedSlotWithHeldItem(InventorySlotChangedEvent event, EntityRef entityRef,
                                             SelectedInventorySlotComponent selectedInventorySlotComponent) {
        if (selectedInventorySlotComponent.slot == event.getSlot()) {
            entityRef.send(new ChangeHeldItemRequest(event.getNewItem()));
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onChangeSelectedInventorySlotRequested(ChangeSelectedInventorySlotRequest request, EntityRef character,
                                                       SelectedInventorySlotComponent selectedInventorySlotComponent) {
        if (request.getSlot() >= 0 && request.getSlot() < 10 && request.getSlot() != selectedInventorySlotComponent.slot) {
            EntityRef newItem = InventoryUtils.getItemAt(character, request.getSlot());
            selectedInventorySlotComponent.slot = request.getSlot();
            character.saveComponent(selectedInventorySlotComponent);
            character.send(new ChangeHeldItemRequest(newItem));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onDropItemRequest(DropItemRequest event, EntityRef character) {
        //make sure we own the item and it exists
        if (!event.getItem().exists() || !networkSystem.getOwnerEntity(event.getItem()).equals(networkSystem.getOwnerEntity(character))) {
            return;
        }

        // remove a single item from the stack
        EntityRef pickupItem = event.getItem();
        EntityRef owner = pickupItem.getOwner();
        if (owner.hasComponent(InventoryComponent.class)) {
            final EntityRef removedItem = inventoryManager.removeItem(owner, EntityRef.NULL, pickupItem, false, 1);
            if (removedItem != null) {
                pickupItem = removedItem;
            }
        }

        pickupItem.send(new DropItemEvent(event.getNewPosition()));
        pickupItem.send(new ImpulseEvent(event.getImpulse()));
    }

    @ReceiveEvent(components = {CharacterComponent.class}, netFilter = RegisterMode.CLIENT)
    public void onNextItem(ToolbarNextButton event, EntityRef entity, SelectedInventorySlotComponent selectedInventorySlotComponent) {
        int nextSlot = (selectedInventorySlotComponent.slot + 1) % 10;
        localPlayer.getCharacterEntity().send(new ChangeSelectedInventorySlotRequest(nextSlot));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class}, netFilter = RegisterMode.CLIENT)
    public void onPrevItem(ToolbarPrevButton event, EntityRef entity, SelectedInventorySlotComponent selectedInventorySlotComponent) {
        int prevSlot = (selectedInventorySlotComponent.slot + 9) % 10;
        localPlayer.getCharacterEntity().send(new ChangeSelectedInventorySlotRequest(prevSlot));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class}, netFilter = RegisterMode.CLIENT)
    public void onSlotButton(ToolbarSlotButton event, EntityRef entity) {
        localPlayer.getCharacterEntity().send(new ChangeSelectedInventorySlotRequest(event.getSlot()));
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class}, netFilter = RegisterMode.CLIENT)
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

        InventoryHud toolbar = nuiManager.getHUD().getHUDElement("core:InventoryHud", InventoryHud.class);
        if (toolbar != null) {
            toolbar.setChargeAmount(getDropPower());
        }

        float dropPower = getDropPower();
        //handle when we finally let go
        if (!event.isDown()) {
            // Compute new position
            dropPower *= 150f;

            Vector3f position = localPlayer.getViewPosition();
            Vector3f direction = localPlayer.getViewDirection();

            Vector3f newPosition = new Vector3f(position.x + direction.x * 1.5f,
                    position.y + direction.y * 1.5f,
                    position.z + direction.z * 1.5f
            );

            //send DropItemRequest
            Vector3f impulseVector = new Vector3f(direction);
            impulseVector.scale(dropPower);
            entity.send(new DropItemRequest(selectedItemEntity, entity,
                    impulseVector,
                    newPosition));

            characterHeldItemComponent.lastItemUsedTime = time.getGameTimeInMs();
            entity.saveComponent(characterHeldItemComponent);

            resetDropMark();
        }

        event.consume();
    }

    public void resetDropMark() {
        InventoryHud toolbar = nuiManager.getHUD().getHUDElement("core:InventoryHud", InventoryHud.class);
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


    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onGiveItemToEntity(GiveItemEvent event, EntityRef entity) {
        if (event.getTargetEntity().hasComponent(InventoryComponent.class)) {
            if (inventoryManager.giveItem(event.getTargetEntity(), entity, entity)) {
                event.getTargetEntity().send(new PlaySoundForOwnerEvent(Assets.getSound("engine:Loot").get(), 1.0f));
                event.setHandled(true);
            }
        }
    }

}
