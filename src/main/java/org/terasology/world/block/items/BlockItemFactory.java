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
package org.terasology.world.block.items;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.rendering.logic.LightComponent;
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
        return newInstance(blockFamily, 1);
    }

    public EntityRef newInstance(BlockFamily blockFamily, int quantity) {
        if (blockFamily == null) {
            return EntityRef.NULL;
        }

        EntityBuilder builder = entityManager.newBuilder("engine:blockItemBase");
        if (blockFamily.getArchetypeBlock().getLuminance() > 0) {
            builder.addComponent(new LightComponent());
        }

        ItemComponent item = builder.getComponent(ItemComponent.class);
        item.name = blockFamily.getDisplayName();
        if (blockFamily.getArchetypeBlock().isStackable()) {
            item.stackId = "block:" + blockFamily.getURI().toString();
            item.stackCount = (byte) quantity;
        }

        BlockItemComponent blockItem = builder.getComponent(BlockItemComponent.class);
        blockItem.blockFamily = blockFamily;

        return builder.build();
    }

}
