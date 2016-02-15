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

package org.terasology.logic.inventory;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.Replicate;
import org.terasology.network.ReplicationCheck;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.world.block.ForceBlockActive;

import java.util.List;

/**
 * Allows an entity to store items.
 *
 */
@ForceBlockActive
public final class InventoryComponent implements Component, ReplicationCheck {

    public boolean privateToOwner = true;

    @Replicate
    @Owns
    public List<EntityRef> itemSlots = Lists.newArrayList();

    public InventoryComponent() {
    }

    public InventoryComponent(int numSlots) {
        for (int i = 0; i < numSlots; ++i) {
            itemSlots.add(EntityRef.NULL);
        }
    }

    @Override
    public boolean shouldReplicate(FieldMetadata<?, ?> field, boolean initial, boolean toOwner) {
        return !privateToOwner || toOwner;
    }
}
