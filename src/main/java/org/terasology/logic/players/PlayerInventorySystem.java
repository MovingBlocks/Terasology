/*
 * Copyright 2013 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.players;

import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.Timer;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.binds.AttackButton;
import org.terasology.input.binds.ToolbarNextButton;
import org.terasology.input.binds.ToolbarPrevButton;
import org.terasology.input.binds.ToolbarSlotButton;
import org.terasology.input.binds.UseItemButton;
import org.terasology.logic.characters.AttackInDirectionRequest;
import org.terasology.logic.characters.AttackTargetRequest;
import org.terasology.logic.characters.UseItemInDirectionRequest;
import org.terasology.logic.characters.UseItemOnTargetRequest;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class PlayerInventorySystem implements EventHandlerSystem {

    @In
    private LocalPlayer localPlayer;

    @In
    private Timer timer;

    @In
    private CameraTargetSystem cameraTargetSystem;

    @In
    private WorldRenderer worldRenderer;

    private long lastInteraction;

    @Override
    public void initialise() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
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
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool);

        ItemComponent item = selectedItemEntity.getComponent(ItemComponent.class);
        if (item != null && item.usage != ItemComponent.UsageType.NONE) {
            if (event.getTarget().exists()) {
                entity.send(new UseItemOnTargetRequest(selectedItemEntity, event.getTarget(), event.getHitPosition()));
            } else {
                entity.send(new UseItemInDirectionRequest(selectedItemEntity, worldRenderer.getActiveCamera().getViewingDirection()));
            }
        }
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

        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool);

        if (event.getTarget().exists()) {
            entity.send(new AttackTargetRequest(selectedItemEntity, event.getTarget(), event.getHitPosition()));
        } else {
            entity.send(new AttackInDirectionRequest(selectedItemEntity, worldRenderer.getActiveCamera().getViewingDirection()));
        }

        lastInteraction = timer.getTimeInMs();
        localPlayerComp.handAnimation = 0.5f;
        entity.saveComponent(localPlayerComp);
        event.consume();
    }

}
