// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.items;

import com.google.common.base.Preconditions;
import com.google.common.primitives.SignedBytes;
import org.terasology.engine.entitySystem.ComponentContainer;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.common.RetainComponentsComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.rendering.logic.LightComponent;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * A factory to create new <em>block items</em> for a {@link BlockFamily}.
 * <p>
 * A <strong>block item</strong> is an entity guaranteed to have the following components.
 * <ul>
 *     <li>{@link BlockItemComponent}. the back-reference to the block family</li>
 *     <li>{@link ItemComponent}. the item component with automatic stack id and quantity if block is stackable</li>
 *     <li>{@link DisplayNameComponent}. the block family archetype display name</li>
 * </ul>
 * Components on the block prefab (or the reference block entity) will only be retained if the component class is
 * annotated with {@link AddToBlockBasedItem} or the component class is listed in {@link RetainComponentsComponent}.
 * <p>
 * The created entity (builder) is based on the block item base prefab ({@code engine:blockItemBase}).
 *
 * @see AddToBlockBasedItem
 * @see RetainComponentsComponent
 */
public class BlockItemFactory {
    private final EntityManager entityManager;

    /**
     * Instantiate new block item factory with the given entity manager.
     *
     * @param entityManager entity manager to create new {@link EntityBuilder}s and copy components from
     *         reference entities or prefabs to the new block items
     */
    public BlockItemFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Create a new block item for the given {@link BlockFamily}.
     * <p>
     * Attempts to resolve the corresponding block prefab to retrieve a list of potential components to add. The item
     * quantity defaults to 1.
     * <p>
     * Use {@link #newBuilder(BlockFamily, int)} if you want to modify the block item entity's properties before it gets
     * created.
     *
     * @param blockFamily block family to create the block item builder for
     * @return the block item entity
     */
    public EntityRef newInstance(BlockFamily blockFamily) {
        return newInstance(blockFamily, 1);
    }

    /**
     * Create a new block item for the given {@link BlockFamily} and item quantity.
     * <p>
     * Attempts to resolve the corresponding block prefab to retrieve a list of potential components to add.
     * <p>
     * Use {@link #newBuilder(BlockFamily, int)} if you want to modify the block item entity's properties before it gets
     * created.
     *
     * @param blockFamily block family to create the block item builder for
     * @param quantity item quantity (see {@link ItemComponent#stackCount}); constrained to [0...128)
     * @return the block item entity
     */
    public EntityRef newInstance(BlockFamily blockFamily, int quantity) {
        if (blockFamily == null) {
            return EntityRef.NULL;
        }
        EntityBuilder builder = newBuilder(blockFamily, quantity);
        return builder.build();
    }

    /**
     * Create a new block item for the given {@link BlockFamily}, with {@code blockEntity} as reference entity to retain
     * components from.
     * <p>
     * The item quantity defaults to 1.
     * <p>
     * Use {@link #newBuilder(BlockFamily, EntityRef, int)} if you want to modify the block item entity's properties
     * before it gets created.
     *
     * @param blockFamily block family to create the block item builder for
     * @param blockEntity reference block entity to retain components from
     * @return the block item entity
     */
    public EntityRef newInstance(BlockFamily blockFamily, EntityRef blockEntity) {
        if (blockFamily == null) {
            return EntityRef.NULL;
        }

        return createBuilder(blockFamily, blockEntity, (byte) 1).build();
    }

    /**
     * Create a new block item builder for the given {@link BlockFamily} and item quantity.
     * <p>
     * Attempts to resolve the corresponding block prefab to retrieve a list of potential components to add.
     * <p>
     * Use this method if you want to modify the block item entity's properties before it gets created.
     *
     * @param blockFamily block family to create the block item builder for
     * @param quantity item quantity (see {@link ItemComponent#stackCount}); constrained to [0...128)
     * @return a pre-populated entity builder for a block item entity
     */
    public EntityBuilder newBuilder(BlockFamily blockFamily, int quantity) {
        final ComponentContainer components =
                blockFamily.getArchetypeBlock().getPrefab()
                        .map(p -> ((ComponentContainer) p))
                        .orElse(EntityRef.NULL);

        return createBuilder(blockFamily, components, SignedBytes.saturatedCast(quantity));
    }

    /**
     * Create a new block item builder for the given {@link BlockFamily} and item quantity, with {@code blockEntity} as
     * reference entity to retain components from.
     * <p>
     * Use this method if you want to modify the block item entity's properties before it gets created.
     *
     * @param blockFamily block family to create the block item builder for
     * @param blockEntity reference block entity to retain components from
     * @param quantity item quantity (see {@link ItemComponent#stackCount}); constrained to [0...128)
     * @return a pre-populated entity builder for a block item entity
     */
    public EntityBuilder newBuilder(BlockFamily blockFamily, EntityRef blockEntity, int quantity) {
        return createBuilder(blockFamily, blockEntity, SignedBytes.saturatedCast(quantity));
    }

    /**
     * Create a new block item builder for the given {@link BlockFamily}.
     *
     * @param blockFamily block family to create the block item builder for
     * @param components potential components to add to the block item entity
     * @param quantity item quantity (see {@link ItemComponent#stackCount})
     * @return a pre-populated entity builder for a block item entity
     */
    private EntityBuilder createBuilder(BlockFamily blockFamily, ComponentContainer components, byte quantity) {
        Preconditions.checkNotNull(blockFamily, "Block family must not be null when creating block item");

        EntityBuilder builder = entityManager.newBuilder("engine:blockItemBase");
        addComponents(builder, components);

        adjustLightComponent(builder, blockFamily);
        adjustDisplayNameComponent(builder, blockFamily);
        adjustItemComponent(builder, blockFamily, quantity);
        adjustBlockItemComponent(builder, blockFamily);

        return builder;
    }

    /**
     * Mutate the builder to add components from the given component container.
     * <p>
     * A component is only added to the builder if at least one of the following conditions holds:
     * <ul>
     *     <li>the component class is annotated with {@link AddToBlockBasedItem}</li>
     *     <li>the container contains a {@link RetainComponentsComponent} and the component class is listed as
     *     retained</li>
     * </ul>
     * <p>
     * Components that should be added to the block item entity are <emph>copied</emph>.
     *
     * @param builder the builder to add the components to
     * @param components the container with potential components to add
     */
    private void addComponents(EntityBuilder builder, ComponentContainer components) {
        final Set<Class<? extends Component>> retainComponents =
                Optional.ofNullable(components.getComponent(RetainComponentsComponent.class))
                        .map(retain -> retain.components)
                        .orElse(Collections.emptySet());

        for (Component component : components.iterateComponents()) {
            if (keepByAnnotation(component) || retainComponents.contains(component.getClass())) {
                builder.addComponent(entityManager.getComponentLibrary().copy(component));
            }
        }
    }

    private boolean keepByAnnotation(Component component) {
        return component.getClass().getAnnotation(AddToBlockBasedItem.class) != null;
    }

    private void adjustLightComponent(EntityBuilder builder, BlockFamily blockFamily) {
        //TODO: set properties of the LightComponent based on the archetype block?
        if (blockFamily.getArchetypeBlock().getLuminance() > 0 && !builder.hasComponent(LightComponent.class)) {
            builder.addComponent(new LightComponent());
        }
    }

    private void adjustDisplayNameComponent(EntityBuilder builder, BlockFamily blockFamily) {
        builder.updateComponent(DisplayNameComponent.class, displayName -> {
            displayName.name = blockFamily.getDisplayName();
            return displayName;
        });
    }

    private void adjustItemComponent(EntityBuilder builder, BlockFamily blockFamily, byte quantity) {
        builder.updateComponent(ItemComponent.class, item -> {
            if (blockFamily.getArchetypeBlock().isStackable()) {
                item.stackId = "block:" + blockFamily.getURI().toString();
                //TODO: quantity may be greater than item.maxStackSize
                item.stackCount = quantity;
            }
            return item;
        });
    }

    private void adjustBlockItemComponent(EntityBuilder builder, BlockFamily blockFamily) {
        builder.updateComponent(BlockItemComponent.class, blockItem -> {
            blockItem.blockFamily = blockFamily;
            return blockItem;
        });
    }
}
