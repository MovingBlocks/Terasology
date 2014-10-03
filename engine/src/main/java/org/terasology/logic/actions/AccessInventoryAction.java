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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterUtil;
import org.terasology.logic.characters.events.InteractionEndEvent;
import org.terasology.logic.characters.events.InteractionStartEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.inventory.ContainerScreen;

/**
 *
 * @author Immortius <immortius@gmail.com>
 * @author Florian <florian@fkoeberle.de>
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class AccessInventoryAction extends BaseComponentSystem {
    private static final String ENGINE_CONTAINER_SCREEN = "engine:containerScreen";
    private static final Logger logger = LoggerFactory.getLogger(AccessInventoryAction.class);

    @In
    private NUIManager nuiManager;

    @ReceiveEvent(components = {AccessInventoryActionComponent.class, InventoryComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EntityRef instigator = event.getInstigator();

        if (instigator.getComponent(CharacterComponent.class) != null
                && instigator.getComponent(InventoryComponent.class) != null) {
            CharacterUtil.setInteractionTarget(instigator, entity);
        }
    }

    @ReceiveEvent(components = {AccessInventoryActionComponent.class, InventoryComponent.class})
    public void onOpenContainer(InteractionStartEvent event, EntityRef container) {
        EntityRef investigator = event.getInstigator();
        CharacterComponent characterComponent = investigator.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Container got opened by entity without character component");
            return;
        }
        if (investigator.getComponent(InventoryComponent.class) == null) {
            logger.error("Container got opened by entity without inventory component");
            return;
        }
        ClientComponent controller = characterComponent.controller.getComponent(ClientComponent.class);
        if (controller != null && controller.local) {
            nuiManager.pushScreen("engine:containerScreen", ContainerScreen.class);
        }
    }

    @ReceiveEvent(components = {AccessInventoryActionComponent.class, InventoryComponent.class})
    public void onCloseContainer(InteractionEndEvent event, EntityRef container) {
        EntityRef investigator = event.getInstigator();
        CharacterComponent characterComponent = investigator.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Container got closed by entity without character component");
            return;
        }
        if (investigator.getComponent(InventoryComponent.class) == null) {
            logger.error("Container got closed by entity without inventory component");
            return;
        }
        ClientComponent controller = characterComponent.controller.getComponent(ClientComponent.class);
        if (controller != null && controller.local) {
            nuiManager.closeScreen(ENGINE_CONTAINER_SCREEN);
        }
    }


}
