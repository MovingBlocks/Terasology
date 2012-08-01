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
package org.terasology.entityFactory;

import org.terasology.components.ItemComponent;
import org.terasology.components.LightComponent;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.model.blocks.BlockFamily;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockItemFactory {
    private EntityManager entityManager;

    public BlockItemFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef newInstance(BlockFamily blockFamily) {
        return newInstance(blockFamily, 1, null);
    }

    public EntityRef newInstance(BlockFamily blockFamily, EntityRef placedEntity) {
        return newInstance(blockFamily, 1, placedEntity);
    }

    public EntityRef newInstance(BlockFamily blockFamily, int quantity) {
        return newInstance(blockFamily, quantity, null);
    }

    private EntityRef newInstance(BlockFamily blockFamily, int quantity, EntityRef placedEntity) {
        if (blockFamily == null) {
            return EntityRef.NULL;
        }
        EntityRef entity = entityManager.create();
        if (blockFamily.getArchetypeBlock().getLuminance() > 0) {
            entity.addComponent(new LightComponent());
        }
        ItemComponent item = new ItemComponent();
        item.name = blockFamily.getTitle();
        item.consumedOnUse = true;
        if (blockFamily.getArchetypeBlock().isStackable()) {
            item.stackId = blockFamily.getTitle() + "Block";
            item.stackCount = (byte) quantity;
        }
        item.usage = ItemComponent.UsageType.ON_BLOCK;
        entity.addComponent(item);

        BlockItemComponent blockItem = new BlockItemComponent(blockFamily);

        if (!blockFamily.getArchetypeBlock().isEntityTemporary()) {
            if (placedEntity == null || !placedEntity.exists()) {
                placedEntity = entityManager.create(blockFamily.getArchetypeBlock().getEntityPrefab());
            }
            blockItem.placedEntity = placedEntity;
        }

        entity.addComponent(blockItem);

        return entity;
    }

}
