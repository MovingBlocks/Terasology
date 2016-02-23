/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.BuilderSampleGameplay.logic;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.events.ChangeHeldItemRequest;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.GiveItemEvent;

@RegisterSystem
public class BuilderHeldItemAuthoritySystem extends BaseComponentSystem {
    @ReceiveEvent
    public void onGiveItemToCharacterHoldItem(GiveItemEvent event, EntityRef item, ItemComponent itemComponent) {
        if (event.getTargetEntity().hasComponent(CharacterHeldItemComponent.class)) {
            event.getTargetEntity().addOrSaveComponent(new CharacterOwnedItemComponent(item));
            event.getTargetEntity().send(new ChangeHeldItemRequest(item));
            event.setHandled(true);
        }
    }
}
