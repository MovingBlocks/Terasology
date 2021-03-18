// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;

public class CharacterHeldItemComponent implements Component {

    @Replicate
    public EntityRef selectedItem = EntityRef.NULL;

    @Replicate
    public long lastItemUsedTime;

    @Replicate
    public long nextItemUseTime;
}
