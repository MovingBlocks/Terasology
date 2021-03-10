// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.ChangeHeldItemRequest;

@RegisterSystem(RegisterMode.AUTHORITY)
public class CharacterHeldItemAuthoritySystem extends BaseComponentSystem {

    @ReceiveEvent
    public void onChangeHeldItemRequest(ChangeHeldItemRequest event, EntityRef character,
                                        CharacterHeldItemComponent characterHeldItemComponent) {
        characterHeldItemComponent.selectedItem = event.getItem();
        character.saveComponent(characterHeldItemComponent);
    }
}
