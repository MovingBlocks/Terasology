// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

public class CharacterHeldItemComponent implements Component<CharacterHeldItemComponent> {

    @Replicate
    public EntityRef selectedItem = EntityRef.NULL;

    @Replicate
    public long lastItemUsedTime;

    @Replicate
    public long nextItemUseTime;

    @Override
    public void copyFrom(CharacterHeldItemComponent other) {
        this.selectedItem = other.selectedItem;
        this.lastItemUsedTime = other.lastItemUsedTime;
        this.nextItemUseTime = other.nextItemUseTime;
    }
}
