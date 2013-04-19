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

import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.game.Timer;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.binds.AttackButton;
import org.terasology.input.binds.ToolbarNextButton;
import org.terasology.input.binds.ToolbarPrevButton;
import org.terasology.input.binds.ToolbarSlotButton;
import org.terasology.input.binds.UseItemButton;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.UseItemRequest;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.rendering.world.WorldRenderer;

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

    private long lastInteraction;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onNextItem(ToolbarNextButton event, EntityRef entity) {
        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        character.selectedTool = (character.selectedTool + 1) % 10;
        localPlayer.getCharacterEntity().saveComponent(character);
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onPrevItem(ToolbarPrevButton event, EntityRef entity) {
        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        character.selectedTool = (character.selectedTool - 1) % 10;
        if (character.selectedTool < 0) {
            character.selectedTool = 10 + character.selectedTool;
        }
        localPlayer.getCharacterEntity().saveComponent(character);
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onSlotButton(ToolbarSlotButton event, EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        character.selectedTool = event.getSlot();
        localPlayer.getCharacterEntity().saveComponent(character);
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onUseItemButton(UseItemButton event, EntityRef entity) {
        if (!event.isDown() || timer.getTimeInMs() - lastInteraction < 200) {
            return;
        }

        CharacterComponent character = entity.getComponent(CharacterComponent.class);

        EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, character.selectedTool);

        entity.send(new UseItemRequest(selectedItemEntity));

        lastInteraction = timer.getTimeInMs();
        character.handAnimation = 0.5f;
        entity.saveComponent(character);
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onAttackRequest(AttackButton event, EntityRef entity) {
        if (!event.isDown() || timer.getTimeInMs() - lastInteraction < 200) {
            return;
        }

        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, character.selectedTool);

        entity.send(new AttackRequest(selectedItemEntity));

        lastInteraction = timer.getTimeInMs();
        character.handAnimation = 0.5f;
        entity.saveComponent(character);
        event.consume();
    }

}
