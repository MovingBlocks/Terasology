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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.ComponentContainer;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.common.RetainComponentsComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.world.block.family.BlockFamily;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 *
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
        EntityBuilder builder = newBuilder(blockFamily, quantity);
        return builder.build();
    }

    public EntityRef newInstance(BlockFamily blockFamily, EntityRef blockEntity) {
        if (blockFamily == null) {
            return EntityRef.NULL;
        }

        return createBuilder(blockFamily, blockEntity, (byte) 1).build();
    }

    /**
     * Use this method instead of {@link #newInstance(BlockFamily)} to modify entity properties like the persistence
     * flag before it gets created.
     *
     * @param blockFamily must not be null
     */
    public EntityBuilder newBuilder(BlockFamily blockFamily, int quantity) {
        final Optional<Prefab> prefab = blockFamily.getArchetypeBlock().getPrefab();

        ComponentContainer components = prefab.map(p -> ((ComponentContainer) p)).orElse(EntityRef.NULL);

        return createBuilder(blockFamily, components, (byte) quantity);
    }

    private EntityBuilder createBuilder(BlockFamily blockFamily, ComponentContainer components, byte quantity) {
        EntityBuilder builder = entityManager.newBuilder("engine:blockItemBase");

        final Set<Class<? extends Component>> retainComponents =
                Optional.ofNullable(components.getComponent(RetainComponentsComponent.class))
                        .map(retain -> retain.components)
                        .orElse(Collections.emptySet());
        addComponents(builder, components, retainComponents);

        adjustLightComponent(builder, blockFamily);
        adjustDisplayNameComponent(builder, blockFamily);
        adjustItemComponent(builder, blockFamily, quantity);
        adjustBlockItemComponent(builder, blockFamily);

        return builder;
    }

    private void adjustLightComponent(EntityBuilder builder, BlockFamily blockFamily) {
        if (blockFamily.getArchetypeBlock().getLuminance() > 0) {
            builder.addComponent(new LightComponent());
        }
    }

    private void adjustDisplayNameComponent(EntityBuilder builder, BlockFamily blockFamily) {
        DisplayNameComponent displayNameComponent = builder.getComponent(DisplayNameComponent.class);
        if (displayNameComponent != null) {
            displayNameComponent.name = blockFamily.getDisplayName();
        }
    }

    private void addComponents(EntityBuilder builder, ComponentContainer components,
                               Set<Class<? extends Component>> retainComponents) {

        for (Component component : components.iterateComponents()) {
            if (keepByAnnotation(component) || retainComponents.contains(component.getClass())) {
                builder.addComponent(entityManager.getComponentLibrary().copy(component));
            }
        }
    }

    private void adjustItemComponent(EntityBuilder builder, BlockFamily blockFamily, byte quantity) {
        ItemComponent item = builder.getComponent(ItemComponent.class);
        if (blockFamily.getArchetypeBlock().isStackable()) {
            item.stackId = "block:" + blockFamily.getURI().toString();
            item.stackCount = quantity;
        }
    }

    private void adjustBlockItemComponent(EntityBuilder builder, BlockFamily blockFamily) {
        BlockItemComponent blockItem = builder.getComponent(BlockItemComponent.class);
        blockItem.blockFamily = blockFamily;
    }

    private boolean keepByAnnotation(Component component) {
        return component.getClass().getAnnotation(AddToBlockBasedItem.class) != null;
    }
}
