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
package org.terasology.logic.actions;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.events.OpenInventoryEvent;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.inventory.ContainerScreen;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class AccessInventoryAction extends BaseComponentSystem {

    @In
    private NetworkSystem networkSystem;

    @In
    private NUIManager nuiManager;

    @ReceiveEvent(components = {AccessInventoryActionComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onActivate(ActivateEvent event, EntityRef entity) {
        InventoryComponent inv = entity.getComponent(InventoryComponent.class);
        if (inv != null) {
            inv.accessors.add(event.getInstigator());
            entity.saveComponent(inv);
            event.getInstigator().send(new OpenInventoryEvent(entity));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onOpenContainer(OpenInventoryEvent event, EntityRef entity) {
        ClientComponent controller = entity.getComponent(CharacterComponent.class).controller.getComponent(ClientComponent.class);
        if (controller.local && event.getContainer().hasComponent(InventoryComponent.class)) {
            ContainerScreen screen = nuiManager.pushScreen("engine:containerScreen", ContainerScreen.class);
            if (screen != null) {
                screen.setContainerEntity(event.getContainer());
            }
        }
    }

}
