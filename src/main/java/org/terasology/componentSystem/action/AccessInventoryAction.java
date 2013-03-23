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
package org.terasology.componentSystem.action;

import org.terasology.components.actions.AccessInventoryActionComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.OpenInventoryEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.players.LocalPlayerComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.rendering.gui.windows.UIScreenContainer;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class AccessInventoryAction implements ComponentSystem {

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {AccessInventoryActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        if (networkSystem.getMode().isAuthority()) {
            InventoryComponent inv = entity.getComponent(InventoryComponent.class);
            if (inv != null) {
                inv.accessors.add(event.getInstigator());
                entity.saveComponent(inv);
                event.getInstigator().send(new OpenInventoryEvent(entity));
            }
        }
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, InventoryComponent.class})
    public void onOpenContainer(OpenInventoryEvent event, EntityRef entity) {
        if (event.getContainer().hasComponent(InventoryComponent.class)) {
            UIScreenContainer containerScreen = (UIScreenContainer) CoreRegistry.get(GUIManager.class).openWindow("container");
            containerScreen.openContainer(event.getContainer(), entity);
        }
    }

}
