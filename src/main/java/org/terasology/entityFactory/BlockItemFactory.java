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
import org.terasology.network.NetworkComponent;
import org.terasology.world.block.BlockEntityMode;
import org.terasology.world.block.entity.BlockItemComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockItemFactory {
    private EntityManager entityManager;

    public BlockItemFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef newInstance(BlockFamily blockFamily) {
        return newInstance(blockFamily, 1, EntityRef.NULL);
    }

    public EntityRef newInstance(BlockFamily blockFamily, EntityRef placedEntity) {
        return newInstance(blockFamily, 1, placedEntity);
    }

    public EntityRef newInstance(BlockFamily blockFamily, int quantity) {
        return newInstance(blockFamily, quantity, EntityRef.NULL);
    }

    private EntityRef newInstance(BlockFamily blockFamily, int quantity, EntityRef placedEntity) {
        if (blockFamily == null) {
            return EntityRef.NULL;
        }

        EntityRef entity = entityManager.create("engine:blockItemBase");
        if (blockFamily.getArchetypeBlock().getLuminance() > 0) {
            entity.addComponent(new LightComponent());
        }

        ItemComponent item = entity.getComponent(ItemComponent.class);
        item.name = blockFamily.getDisplayName();
        if (blockFamily.getArchetypeBlock().isStackable()) {
            item.stackId = "block:" + blockFamily.getURI().toString();
            item.stackCount = (byte) quantity;
        }
        entity.saveComponent(item);

        BlockItemComponent blockItem = entity.getComponent(BlockItemComponent.class);
        blockItem.blockFamily = blockFamily;
        if (blockFamily.getArchetypeBlock().getEntityMode() == BlockEntityMode.PERSISTENT) {
            if (!placedEntity.exists()) {
                placedEntity = entityManager.create(blockFamily.getArchetypeBlock().getEntityPrefab());
                placedEntity.addComponent(new NetworkComponent());
            }
            blockItem.placedEntity = placedEntity;
        }
        entity.saveComponent(blockItem);

        return entity;
    }

}
